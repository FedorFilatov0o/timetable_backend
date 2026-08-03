package app.timetable_back.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.timetable_back.entity.DayComment;

@Repository
public interface DayCommentRepository extends JpaRepository<DayComment, Long> {

    List<DayComment> findByDate(LocalDate date);
}
