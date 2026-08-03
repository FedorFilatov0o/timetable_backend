package app.timetable_back.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.timetable_back.entity.StudentGroup;

@Repository
public interface GroupRepository extends JpaRepository<StudentGroup, Long> {
    @Query("SELECT g FROM StudentGroup g WHERE (:search IS NULL OR LOWER(g.name) LIKE :search)")
    Page<StudentGroup> findBySearchQuery(@Param("search") String search, Pageable pageable);
}
