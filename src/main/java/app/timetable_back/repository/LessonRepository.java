package app.timetable_back.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.timetable_back.entity.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    @Query("SELECT DISTINCT l FROM Lesson l " + "LEFT JOIN FETCH l.subject " + "LEFT JOIN FETCH l.teacher "
            + "LEFT JOIN FETCH l.lessonStudentGroups lsg " + "LEFT JOIN FETCH lsg.group "
            + "LEFT JOIN FETCH l.lessonRooms lr " + "LEFT JOIN FETCH lr.room " + "WHERE l.id = :id")
    Optional<Lesson> findByIdWithDetails(Long id);

    @Query("SELECT DISTINCT l FROM Lesson l " + "LEFT JOIN FETCH l.subject " + "LEFT JOIN FETCH l.teacher "
            + "LEFT JOIN FETCH l.lessonStudentGroups lsg " + "LEFT JOIN FETCH lsg.group "
            + "LEFT JOIN FETCH l.lessonRooms lr " + "LEFT JOIN FETCH lr.room")
    List<Lesson> findAllWithDetails();

    @Query("SELECT DISTINCT l FROM Lesson l " + "LEFT JOIN FETCH l.subject " + "LEFT JOIN FETCH l.teacher "
            + "LEFT JOIN FETCH l.lessonStudentGroups lsg " + "LEFT JOIN FETCH lsg.group "
            + "LEFT JOIN FETCH l.lessonRooms lr " + "LEFT JOIN FETCH lr.room "
            + "WHERE l.startAt >= :startDate AND l.startAt <= :endDate " + "ORDER BY l.startAt")
    List<Lesson> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // Методы для проверки связей перед удалением (возвращают List, не Optional!)
    @Query("SELECT DISTINCT l FROM Lesson l JOIN l.teacher t WHERE t.id = :id ORDER BY l.startAt ASC")
    List<Lesson> findFirstByTeacherId(@Param("id") Long teacherId, Pageable pageable);

    @Query("SELECT DISTINCT l FROM Lesson l WHERE l.subject.id = :id ORDER BY l.startAt ASC")
    List<Lesson> findFirstBySubjectId(@Param("id") Long subjectId, Pageable pageable);

    @Query("SELECT DISTINCT l FROM Lesson l JOIN l.lessonRooms lr JOIN lr.room r WHERE r.id = :id ORDER BY l.startAt ASC")
    List<Lesson> findFirstByRoomId(@Param("id") Long roomId, Pageable pageable);

    @Query("SELECT DISTINCT l FROM Lesson l JOIN l.lessonStudentGroups lsg JOIN lsg.group g WHERE g.id = :id ORDER BY l.startAt ASC")
    List<Lesson> findFirstByGroupId(@Param("id") Long groupId, Pageable pageable);
}
