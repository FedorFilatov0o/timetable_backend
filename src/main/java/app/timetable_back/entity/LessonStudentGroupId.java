package app.timetable_back.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonStudentGroupId implements Serializable {

    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(name = "group_id")
    private Long groupId;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LessonStudentGroupId that = (LessonStudentGroupId) o;
        return Objects.equals(lessonId, that.lessonId) && Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lessonId, groupId);
    }
}
