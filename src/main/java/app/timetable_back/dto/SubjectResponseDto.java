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
@Schema(description = "Информация о предмете")
public class SubjectResponseDto {

    @Schema(description = "ID предмета", example = "1")
    private Long id;

    @Schema(description = "Название", example = "Высшая математика")
    private String name;

    @Schema(description = "Код", example = "MATH-101")
    private String code;

    @Schema(description = "Факультет", example = "Факультет математики")
    private String faculty;

    @Schema(description = "Описание", example = "Курс высшей математики для 1 курса")
    private String description;

    @Schema(description = "Время создания", example = "2026-03-11T14:00:00Z")
    private OffsetDateTime createdAt;

    @Schema(description = "Время обновления", example = "2026-03-11T14:00:00Z")
    private OffsetDateTime updatedAt;
}
