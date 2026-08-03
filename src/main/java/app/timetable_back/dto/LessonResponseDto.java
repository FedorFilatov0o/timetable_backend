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
@Schema(description = "Информация о занятии")
public class LessonResponseDto {

    @Schema(description = "ID занятия", example = "1")
    private Long id;

    @Schema(description = "Время начала", example = "2026-03-15T10:00:00")
    private LocalDateTime startAt;

    @Schema(description = "Время окончания", example = "2026-03-15T11:30:00")
    private LocalDateTime endAt;

    @Schema(description = "Тип повторения", example = "WEEKLY")
    private RecurrenceRuleType ruleType;

    @Schema(description = "Список аудиторий")
    private List<RoomResponseDto> rooms;

    @Schema(description = "Предмет")
    private SubjectResponseDto subject;

    @Schema(description = "Преподаватель")
    private UserResponseDto teacher;

    @Schema(description = "Список ID групп", example = "[1, 2]")
    private List<Long> groupIds;

    @Schema(description = "Время создания", example = "2026-03-14T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Время обновления", example = "2026-03-14T12:00:00")
    private LocalDateTime updatedAt;
}
