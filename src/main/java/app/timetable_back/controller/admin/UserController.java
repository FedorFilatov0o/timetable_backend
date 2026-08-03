package app.timetable_back.controller.admin;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import app.timetable_back.dto.PageResponse;
import app.timetable_back.dto.UserDto;
import app.timetable_back.dto.UserListViewDto;
import app.timetable_back.dto.UserResponseDto;
import app.timetable_back.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
// ИЗМЕНЕНИЕ: Разрешаем доступ ADMIN и DISPATCHER (для чтения справочника при
// составлении
// расписания)
@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')") // ИЗМЕНЕНИЕ: Только ADMIN может создавать
    @Operation(summary = "Create new user", description = "Создание нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserResponseDto createdUser = userService.createUserDto(userDto);
        return ResponseEntity.created(URI.create("/admin/users/" + createdUser.getId())).body(createdUser);
    }

    @PutMapping("/update-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // ИЗМЕНЕНИЕ: Только ADMIN может обновлять
    @Operation(summary = "Update user", description = "Обновление данных пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody UserDto userDto) {
        UserResponseDto updatedUser = userService.updateUserDto(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/delete-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // ИЗМЕНЕНИЕ: Только ADMIN может удалять
    @Operation(summary = "Delete user", description = "Удаление пользователя")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // GET методы наследуют hasAnyRole('ADMIN', 'DISPATCHER') с уровня класса
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user by ID", description = "Получение данных пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<UserResponseDto> getUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        UserResponseDto user = userService.findByIdDto(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Получение списка всех пользователей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found", content = @Content(schema = @Schema(implementation = UserResponseDto.class)))})
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.findAllDto();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/list")
    @Operation(summary = "Get paginated users list with search", description = "Получение пагинированного списка пользователей с поиском")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found", content = @Content(schema = @Schema(implementation = PageResponse.class)))})
    public ResponseEntity<PageResponse<UserListViewDto>> getUsersList(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поисковый запрос (частичное совпадение)") @RequestParam(required = false) String search) {
        PageResponse<UserListViewDto> response = userService.findAllListView(page, size, search);
        return ResponseEntity.ok(response);
    }
}
