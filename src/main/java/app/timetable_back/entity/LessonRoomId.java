package app.timetable_back.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LessonRoomId implements Serializable {
    private Long lessonId;
    private Long roomId;
}
