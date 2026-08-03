package app.timetable_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO для отображения группы в списке (без id, createdAt, updatedAt) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о группе в списке")
public class GroupListViewDto {

    @Schema(description = "Название группы", example = "ИВТ-11")
    private String name;

    @Schema(description = "Курс", example = "1")
    private Integer courseYear;

    @Schema(description = "Количество студентов", example = "25")
    private Integer studentCount;
}
