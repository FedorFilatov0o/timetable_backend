package app.timetable_back.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Универсальный DTO для пагинированного ответа */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Пагинированный ответ")
public class PageResponse<T> {

    @Schema(description = "Список элементов на текущей странице")
    private List<T> content;

    @Schema(description = "Номер страницы (начиная с 0)", example = "0")
    private int page;

    @Schema(description = "Размер страницы", example = "20")
    private int size;

    @Schema(description = "Общее количество элементов", example = "150")
    private long totalElements;

    @Schema(description = "Общее количество страниц", example = "8")
    private int totalPages;
}
