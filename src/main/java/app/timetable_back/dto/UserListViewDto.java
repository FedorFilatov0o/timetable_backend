package app.timetable_back.dto;

import app.timetable_back.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO для отображения пользователя в списке (без id, createdAt, updatedAt) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о пользователе в списке")
public class UserListViewDto {

    @Schema(description = "Имя", example = "Ivan")
    private String firstName;

    @Schema(description = "Фамилия", example = "Ivanov")
    private String lastName;

    @Schema(description = "Email", example = "i@gmail.com")
    private String email;

    @Schema(description = "Роль", example = "ADMIN")
    private UserRole role;

    @Schema(description = "Телефон", example = "+7-921-111-11-11")
    private String phone;
}
