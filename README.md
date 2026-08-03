# Timetable Back

Серверная часть системы автоматизированного составления расписания учебных занятий.
Предоставляет REST API для управления занятиями, аудиториями, группами, преподавателями и экспорта расписания.

## Требования

- **Java** 25
- **Maven** 3.9+ (в проекте используется Maven Wrapper — `mvnw`)
- **PostgreSQL** 15+ с расширением `btree_gist`
- **Redis** 7+
- **psql** (консольный клиент PostgreSQL) для инициализации базы данных

### Зависимости

Проект основан на Spring Boot 4.0.3. Основные зависимости:

| Группа                          | Назначение                                        |
|---------------------------------|---------------------------------------------------|
| Spring Boot Starter Web         | REST-контроллеры                                  |
| Spring Boot Starter Data JPA    | Работа с БД через Hibernate                       |
| Spring Boot Starter Data Redis  | Хранение токенов и кеширование                    |
| Spring Boot Starter Security    | Аутентификация и авторизация                      |
| Spring Boot Starter Validation  | Валидация входных данных                          |
| Spring Boot Starter Actuator    | Мониторинг и health-check                         |
| PostgreSQL JDBC Driver          | Подключение к PostgreSQL                          |
| JJWT 0.12.3                     | Генерация и проверка JWT-токенов                  |
| Springdoc OpenAPI 2.8.9         | Документация API (Swagger UI)                     |
| Apache POI 5.2.5                | Экспорт расписания в Excel                        |
| Lombok                          | Сокращение шаблонного кода                        |

### Переменные окружения и конфигурация

Файл настроек: `src/main/resources/application.properties`.

Обязательные параметры для заполнения:

```properties
# База данных PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/timetable
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT — секретный ключ (минимум 256 бит, Base64)
jwt.secret=your_jwt_secret_key_base64
jwt.access-token-validity=3600000
jwt.refresh-token-validity=604800000

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
```

Секретные значения (пароли, JWT-ключ) запрещено коммитить в репозиторий.
Для локальной разработки поместите их в `application.properties` и добавьте файл в `.gitignore`,
либо используйте переменные окружения и внешний конфигурационный файл при деплое.

## Настройка проекта

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd timetable-back
```

### 2. Создание базы данных

Подключитесь к PostgreSQL и создайте базу данных:

```sql
CREATE DATABASE timetable;
```

Убедитесь, что в PostgreSQL доступно расширение `btree_gist` (требуется для ограничений исключения по времени занятий).

### 3. Инициализация таблиц и начальных данных

**Windows:**
```cmd
start.cmd
```

**Linux / macOS:**
```bash
chmod +x start.sh
./start.sh
```

Скрипты выполняют:
- `src/main/resources/db/init.sql` — создание таблиц, индексов, триггеров и функций
- `src/main/resources/db/data.sql` — заполнение тестовыми данными (при наличии файла)

Параметры подключения к базе данных заданы внутри скриптов `start.cmd` / `start.sh`.
При необходимости измените переменные `DB_HOST`, `DB_USER`, `DB_PASS`, `DB_NAME` в начале скрипта.

### 4. Настройка application.properties

Заполните обязательные параметры в `src/main/resources/application.properties`
(URL базы данных, учётные данные, JWT-секрет, параметры Redis) согласно разделу «Переменные окружения и конфигурация».

## Запуск проекта

### Установка зависимостей и сборка

```bash
./mvnw clean package -DskipTests
```

### Запуск приложения

```bash
./mvnw spring-boot:run
```

Приложение запустится на порту **8080** (по умолчанию).

### Swagger UI

После запуска документация API доступна по адресу:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI-спецификация: `http://localhost:8080/v3/api-docs`

## Структура проекта

```
timetable-back/
├── .gitattributes
├── .gitignore
├── eclipse-java-formatter.xml    # Настройки форматирования кода для Eclipse/VS Code
├── mvnw                          # Maven Wrapper (Linux/macOS)
├── mvnw.cmd                      # Maven Wrapper (Windows)
├── pom.xml                       # Конфигурация Maven (зависимости, плагины)
├── start.cmd                     # Скрипт инициализации БД (Windows)
├── start.sh                      # Скрипт инициализации БД (Linux/macOS)
├── src/
│   ├── main/
│   │   ├── java/app/timetable_back/
│   │   │   ├── TimetableBackApplication.java   # Точка входа
│   │   │   ├── config/                         # Конфигурации (JWT, Redis, Security, OpenAPI, ExceptionHandler)
│   │   │   ├── controller/                     # REST-контроллеры
│   │   │   │   ├── admin/                      # Административные эндпоинты
│   │   │   │   └── dispatcher/                 # Эндпоинты диспетчера
│   │   │   ├── dto/                            # Объекты передачи данных (Data Transfer Objects)
│   │   │   ├── entity/                         # JPA-сущности
│   │   │   ├── exception/                      # Пользовательские исключения
│   │   │   ├── repository/                     # Spring Data репозитории
│   │   │   └── service/                        # Сервисный слой (бизнес-логика)
│   │   └── resources/
│   │       ├── application.properties          # Основной конфигурационный файл
│   │       └── db/
│   │           ├── init.sql                     # DDL: таблицы, индексы, триггеры, функции
│   │           └── data.sql                     # Начальные данные (тестовый набор)
│   └── test/
│       └── java/app/                           # Модульные и интеграционные тесты
```

### Основные сущности

| Сущность         | Таблица                | Назначение                                   |
|------------------|------------------------|----------------------------------------------|
| User             | users                  | Пользователи (администраторы, диспетчеры, преподаватели) |
| StudentGroup     | student_groups         | Учебные группы                               |
| Subject          | subjects               | Дисциплины                                   |
| Room             | rooms                  | Аудитории                                    |
| Lesson           | lessons                | Занятия (связь с предметом, преподавателем, аудиториями) |
| DayComment       | day_comments           | Комментарии к дням расписания                |

Связи Many-to-Many:
- `lessons_room` — связь занятий с аудиториями
- `lesson_student_groups` — связь занятий с учебными группами

## Дополнительно

### Тестирование

```bash
./mvnw test
```

### Сборка исполняемого JAR

```bash
./mvnw clean package
```

Собранный JAR-файл находится в `target/timetable-back-0.0.1-SNAPSHOT.jar`.
Запуск:

```bash
java -jar target/timetable-back-0.0.1-SNAPSHOT.jar
```

### Проверки целостности на уровне БД

В DDL-скрипте реализованы следующие ограничения:

- **Запрет пересечения занятий преподавателя** — ограничение исключения `no_teacher_overlap` (требует `btree_gist`)
- **Проверка роли TEACHER** — триггер `trg_check_teacher_role` не позволяет назначить преподавателем пользователя с другой ролью
- **Проверка конфликта аудиторий** — триггер `trg_validate_room_conflict` запрещает назначение одной аудитории на пересекающиеся интервалы времени
- **Проверка вместимости аудитории** — функция `is_room_capacity_sufficient` проверяет, что вместимость аудитории достаточна для всех назначенных групп