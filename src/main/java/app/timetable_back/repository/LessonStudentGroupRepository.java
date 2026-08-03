package app.timetable_back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.timetable_back.entity.LessonStudentGroup;
import app.timetable_back.entity.LessonStudentGroupId;

@Repository
public interface LessonStudentGroupRepository extends JpaRepository<LessonStudentGroup, LessonStudentGroupId> {
}
