package app.timetable_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO для отображения аудитории в списке (без id, createdAt, updatedAt) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация об аудитории в списке")
public class RoomListViewDto {

    @Schema(description = "Номер аудитории", example = "101")
    private String roomNumber;

    @Schema(description = "Корпус", example = "Главный корпус")
    private String building;

    @Schema(description = "Вместимость", example = "30")
    private Integer capacity;
}
