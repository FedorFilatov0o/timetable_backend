package app.timetable_back.dto;

import java.time.LocalDateTime;
import java.util.List;

import app.timetable_back.entity.RecurrenceRuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Краткая информация о занятии для списков")
public class LessonListViewDto {

    @Schema(description = "Время начала", example = "2026-03-15T10:00:00")
    private LocalDateTime startAt;

    @Schema(description = "Время окончания", example = "2026-03-15T11:30:00")
    private LocalDateTime endAt;

    @Schema(description = "Тип повторения", example = "WEEKLY")
    private RecurrenceRuleType ruleType;

    @Schema(description = "Список аудиторий")
    private List<RoomListViewDto> rooms;

    @Schema(description = "Предмет")
    private SubjectListViewDto subject;

    @Schema(description = "Преподаватель")
    private UserListViewDto teacher;

    @Schema(description = "Список ID групп", example = "[1, 2]")
    private List<Long> groupIds;
}
