package app.timetable_back.dto;

import java.time.LocalDate;
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
@Schema(description = "Информация о комментарии к дню")
public class DayCommentResponseDto {

    @Schema(description = "ID комментария", example = "1")
    private Long id;

    @Schema(description = "Дата комментария", example = "2026-03-15")
    private LocalDate date;

    @Schema(description = "Информация о пользователе")
    private UserInfo user;

    @Schema(description = "Текст комментария", example = "Технический перерыв")
    private String commentText;

    @Schema(description = "Время создания", example = "2026-03-11T14:00:00Z")
    private OffsetDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Информация о пользователе")
    public static class UserInfo {
        @Schema(description = "ID пользователя", example = "1")
        private Long id;

        @Schema(description = "Имя", example = "Ivan")
        private String firstName;

        @Schema(description = "Фамилия", example = "Ivanov")
        private String lastName;

        @Schema(description = "Email", example = "i@gmail.com")
        private String email;
    }
}
