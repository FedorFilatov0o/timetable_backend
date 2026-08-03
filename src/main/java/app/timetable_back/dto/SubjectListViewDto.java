package app.timetable_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO для отображения предмета в списке (без id, createdAt, updatedAt) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о предмете в списке")
public class SubjectListViewDto {

    @Schema(description = "Название", example = "Высшая математика")
    private String name;

    @Schema(description = "Код", example = "MATH-101")
    private String code;

    @Schema(description = "Факультет", example = "Факультет математики")
    private String faculty;

    @Schema(description = "Описание", example = "Курс высшей математики для 1 курса")
    private String description;
}
