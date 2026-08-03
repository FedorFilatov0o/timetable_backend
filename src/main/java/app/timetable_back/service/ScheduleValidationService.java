package app.timetable_back.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.timetable_back.entity.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;

@Service
public class ScheduleValidationService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public void validateTeacherRole(Long teacherId) {
        if (teacherId == null)
            return;

        String query = "SELECT u.role FROM User u WHERE u.id = :teacherId";
        UserRole role = entityManager.createQuery(query, UserRole.class).setParameter("teacherId", teacherId)
                .getResultStream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Преподаватель с ID " + teacherId + " не найден"));

        if (role != UserRole.TEACHER) {
            throw new IllegalArgumentException(
                    "Пользователь с ID " + teacherId + " имеет роль " + role + ", а не TEACHER");
        }
    }

    @Transactional(readOnly = true)
    public boolean hasGroupConflict(Long groupId, LocalDateTime startAt, LocalDateTime endAt, Long excludeLessonId) {
        String baseQuery = """
                SELECT COUNT(*)
                FROM LessonStudentGroup lsg
                JOIN lsg.lesson l
                WHERE lsg.id.groupId = :groupId
                  AND l.startAt < :endAt
                  AND l.endAt > :startAt
                """;

        Long result;
        if (excludeLessonId != null) {
            String queryWithExclude = baseQuery + " AND l.id != :excludeLessonId";
            result = entityManager.createQuery(queryWithExclude, Long.class).setParameter("groupId", groupId)
                    .setParameter("endAt", endAt).setParameter("startAt", startAt)
                    .setParameter("excludeLessonId", excludeLessonId).getSingleResult();
        } else {
            result = entityManager.createQuery(baseQuery, Long.class).setParameter("groupId", groupId)
                    .setParameter("endAt", endAt).setParameter("startAt", startAt).getSingleResult();
        }
        return result > 0;
    }

    @Transactional(readOnly = true)
    public boolean hasGroupConflict(Long groupId, LocalDateTime startAt, LocalDateTime endAt) {
        return hasGroupConflict(groupId, startAt, endAt, null);
    }

    @Transactional(readOnly = true)
    public boolean isRoomCapacitySufficientForGroups(Long roomId, List<Long> groupIds) {
        String totalStudentsQuery = """
                SELECT COALESCE(SUM(g.studentCount), 0)
                FROM StudentGroup g
                WHERE g.id IN :groupIds
                """;

        Long totalStudents = entityManager.createQuery(totalStudentsQuery, Long.class)
                .setParameter("groupIds", groupIds).getSingleResult();

        Optional<Integer> capacity = entityManager
                .createQuery("SELECT r.capacity FROM Room r WHERE r.id = :roomId", Integer.class)
                .setParameter("roomId", roomId).getResultStream().findFirst();

        return capacity.map(c -> totalStudents <= c).orElse(false);
    }
}