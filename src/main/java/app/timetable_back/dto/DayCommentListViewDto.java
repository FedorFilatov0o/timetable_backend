package app.timetable_back.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о комментарии к дню в списке")
public class DayCommentListViewDto {

    @Schema(description = "Дата комментария", example = "2026-03-15")
    private LocalDate date;

    @Schema(description = "Информация о пользователе")
    private UserInfo user;

    @Schema(description = "Текст комментария", example = "Технический перерыв")
    private String commentText;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Информация о пользователе")
    public static class UserInfo {
        @Schema(description = "Имя", example = "Ivan")
        private String firstName;

        @Schema(description = "Фамилия", example = "Ivanov")
        private String lastName;

        @Schema(description = "Email", example = "i@gmail.com")
        private String email;
    }
}
