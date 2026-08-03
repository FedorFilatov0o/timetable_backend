package app.timetable_back.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.timetable_back.entity.Subject;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    @Query("SELECT s FROM Subject s WHERE (:search IS NULL OR " + "LOWER(s.name) LIKE :search OR "
            + "LOWER(s.code) LIKE :search OR " + "LOWER(COALESCE(s.faculty, '')) LIKE :search)")
    Page<Subject> findBySearchQuery(@Param("search") String search, Pageable pageable);
}
