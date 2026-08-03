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
@Schema(description = "Информация об аудитории")
public class RoomResponseDto {

    @Schema(description = "ID аудитории", example = "1")
    private Long id;

    @Schema(description = "Номер аудитории", example = "101")
    private String roomNumber;

    @Schema(description = "Корпус", example = "Главный корпус")
    private String building;

    @Schema(description = "Вместимость", example = "30")
    private Integer capacity;

    @Schema(description = "Время создания", example = "2026-03-11T14:00:00Z")
    private OffsetDateTime createdAt;

    @Schema(description = "Время обновления", example = "2026-03-11T14:00:00Z")
    private OffsetDateTime updatedAt;
}
