package app.timetable_back.dto;

import java.time.LocalDateTime;
import java.util.List;

import app.timetable_back.entity.RecurrenceRuleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {

    private Long id;

    @NotNull(message = "Время начала занятия обязательно для указания")
    private LocalDateTime startAt;

    @NotNull(message = "Время окончания занятия обязательно для указания")
    private LocalDateTime endAt;

    private List<Long> roomIds;

    @Positive(message = "ID предмета должен быть положительным числом")
    private Long subjectId;

    @Positive(message = "ID преподавателя должен быть положительным числом")
    private Long teacherId;

    private RecurrenceRuleType ruleType;

    private List<Long> groupIds;
}
