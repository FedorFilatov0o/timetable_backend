package app.timetable_back.dto;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о группе студентов")
public class GroupResponseDto {

    @Schema(description = "ID группы", example = "1")
    private Long id;

    @Schema(description = "Название группы", example = "ИВТ-11")
    private String name;

    @Schema(description = "Курс", example = "1")
    private Integer courseYear;

    @Schema(description = "Количество студентов", example = "25")
    private Integer studentCount;

    @Schema(description = "Время создания", example = "2026-03-11T14:00:00Z")
    private OffsetDateTime createdAt;

    @Schema(description = "Время обновления", example = "2026-03-11T14:00:00Z")
    private OffsetDateTime updatedAt;
}
