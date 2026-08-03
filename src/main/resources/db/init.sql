-- ============================================
-- СКРИПТ СОЗДАНИЯ ТАБЛИЦ И ИНДЕКСОВ
-- Для проекта Timetable Back (Связь Many-to-Many для аудиторий)
-- ============================================

-- Удаление таблиц в обратном порядке (из-за внешних ключей)
DROP TABLE IF EXISTS lesson_student_groups CASCADE;
DROP TABLE IF EXISTS lessons_room CASCADE;
DROP TABLE IF EXISTS day_comments CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS student_groups CASCADE;
DROP TABLE IF EXISTS rooms CASCADE;
DROP TABLE IF EXISTS subjects CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Удаление триггеров и функций, если они существуют
DROP TRIGGER IF EXISTS trg_check_teacher_role ON lessons;
DROP TRIGGER IF EXISTS trg_validate_room_conflict ON lessons_room;
DROP FUNCTION IF EXISTS check_teacher_role();
DROP FUNCTION IF EXISTS validate_room_conflict();
DROP FUNCTION IF EXISTS has_group_conflict(BIGINT, TIMESTAMP, TIMESTAMP, BIGINT);
DROP FUNCTION IF EXISTS is_room_capacity_sufficient(BIGINT, BIGINT);

-- ============================================
-- ТАБЛИЦА: users
-- ============================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- ТАБЛИЦА: student_groups
-- ============================================
CREATE TABLE student_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    course_year INTEGER NOT NULL CHECK (course_year >= 1 AND course_year <= 6),
    student_count INTEGER NOT NULL CHECK (student_count >= 0),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- ТАБЛИЦА: subjects
-- ============================================
CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(20) NOT NULL,
    faculty VARCHAR(100),
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- ТАБЛИЦА: rooms
-- ============================================
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL,
    building VARCHAR(100),
    capacity INTEGER NOT NULL CHECK (capacity >= 1),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- ТАБЛИЦА: lessons
-- ============================================
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    subject_id BIGINT REFERENCES subjects(id),
    teacher_id BIGINT REFERENCES users(id),
    rule_type VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_lessons_time_range CHECK (end_at > start_at)
);

-- ============================================
-- ТАБЛИЦА: lessons_room (Связь Many-to-Many)
-- ============================================
CREATE TABLE lessons_room (
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (lesson_id, room_id)
);

-- ============================================
-- ТАБЛИЦА: day_comments
-- ============================================
CREATE TABLE day_comments (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    user_id BIGINT REFERENCES users(id),
    comment_text TEXT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- ТАБЛИЦА: lesson_student_groups (Составной первичный ключ)
-- ============================================
CREATE TABLE lesson_student_groups (
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES student_groups(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (lesson_id, group_id)
);

-- ============================================
-- ИНДЕКСЫ
-- ============================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_student_groups_name ON student_groups(name);
CREATE INDEX idx_subjects_code ON subjects(code);
CREATE INDEX idx_rooms_number ON rooms(room_number);
CREATE UNIQUE INDEX idx_rooms_unique ON rooms(room_number, building);
CREATE INDEX idx_lessons_time ON lessons(start_at, end_at);
CREATE INDEX idx_lessons_teacher ON lessons(teacher_id);
CREATE INDEX idx_lessons_date ON lessons((start_at::date));
CREATE INDEX idx_lessons_room_lesson ON lessons_room(lesson_id);
CREATE INDEX idx_lessons_room_room ON lessons_room(room_id);
CREATE INDEX idx_lesson_student_groups_group ON lesson_student_groups(group_id);
CREATE INDEX idx_lesson_student_groups_lesson ON lesson_student_groups(lesson_id);
CREATE INDEX idx_day_comments_date ON day_comments(date);
CREATE INDEX idx_day_comments_user ON day_comments(user_id);

-- ============================================
-- УНИКАЛЬНЫЕ ОГРАНИЧЕНИЯ
-- ============================================
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE subjects ADD CONSTRAINT uk_subjects_code UNIQUE (code);

-- ============================================
-- ОГРАНИЧЕНИЯ ИСКЛЮЧЕНИЯ (требует расширение btree_gist)
-- ============================================
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Запрет пересечения по преподавателю и времени
ALTER TABLE lessons ADD CONSTRAINT no_teacher_overlap EXCLUDE USING GIST (
    teacher_id WITH =,
    tsrange(start_at, end_at) WITH &&
) WHERE (teacher_id IS NOT NULL);

-- ============================================
-- ТРИГГЕРЫ И ФУНКЦИИ
-- ============================================

-- Проверка роли преподавателя
CREATE OR REPLACE FUNCTION check_teacher_role()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.teacher_id IS NULL THEN
        RETURN NEW;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM users WHERE id = NEW.teacher_id AND role = 'TEACHER'
    ) THEN
        RAISE EXCEPTION 'Пользователь с ID % должен иметь роль TEACHER', NEW.teacher_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_check_teacher_role
AFTER INSERT OR UPDATE ON lessons
DEFERRABLE INITIALLY IMMEDIATE
FOR EACH ROW
EXECUTE FUNCTION check_teacher_role();

-- Проверка конфликта аудиторий
CREATE OR REPLACE FUNCTION validate_room_conflict()
RETURNS TRIGGER AS $$
DECLARE
    v_start_at TIMESTAMP;
    v_end_at TIMESTAMP;
    v_conflict_id BIGINT;
BEGIN
    SELECT start_at, end_at INTO v_start_at, v_end_at
    FROM lessons WHERE id = NEW.lesson_id;

    -- Ищем пересечения по времени в той же аудитории
    SELECT l.id INTO v_conflict_id
    FROM lessons_room lr
    JOIN lessons l ON lr.lesson_id = l.id
    WHERE lr.room_id = NEW.room_id
      AND l.id != NEW.lesson_id
      AND l.start_at < v_end_at
      AND l.end_at > v_start_at
    LIMIT 1;

    IF FOUND THEN
        RAISE EXCEPTION 'Аудитория % уже занята другим занятием (ID: %) в это время', NEW.room_id, v_conflict_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_room_conflict
BEFORE INSERT OR UPDATE ON lessons_room
FOR EACH ROW
EXECUTE FUNCTION validate_room_conflict();

-- Проверка конфликта групп
CREATE OR REPLACE FUNCTION has_group_conflict(
    p_group_id BIGINT,
    p_start_at TIMESTAMP,
    p_end_at TIMESTAMP,
    p_exclude_lesson_id BIGINT DEFAULT NULL
)
RETURNS BOOLEAN AS $$
DECLARE
    conflict_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO conflict_count
    FROM lesson_student_groups lg
    JOIN lessons l ON lg.lesson_id = l.id
    WHERE lg.group_id = p_group_id
      AND l.start_at < p_end_at
      AND l.end_at > p_start_at
      AND (p_exclude_lesson_id IS NULL OR l.id != p_exclude_lesson_id);
      
    RETURN conflict_count > 0;
END;
$$ LANGUAGE plpgsql;

-- Проверка вместимости аудитории
CREATE OR REPLACE FUNCTION is_room_capacity_sufficient(
    p_lesson_id BIGINT,
    p_room_id BIGINT
)
RETURNS BOOLEAN AS $$
DECLARE
    total_students INTEGER;
    room_capacity INTEGER;
BEGIN
    SELECT COALESCE(SUM(g.student_count), 0) INTO total_students
    FROM lesson_student_groups lg
    JOIN student_groups g ON lg.group_id = g.id
    WHERE lg.lesson_id = p_lesson_id;

    SELECT capacity INTO room_capacity FROM rooms WHERE id = p_room_id;

    IF room_capacity IS NULL THEN
        RAISE EXCEPTION 'Аудитория с ID % не найдена', p_room_id;
    END IF;

    RETURN total_students <= room_capacity;
END;
$$ LANGUAGE plpgsql;