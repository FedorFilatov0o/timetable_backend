package app.timetable_back.controller.admin;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import app.timetable_back.dto.PageResponse;
import app.timetable_back.dto.RoomDto;
import app.timetable_back.dto.RoomListViewDto;
import app.timetable_back.dto.RoomResponseDto;
import app.timetable_back.service.RoomService;
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
@Tag(name = "Room Management", description = "API для управления аудиториями")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/create-room")
    @PreAuthorize("hasRole('ADMIN')") // ИЗМЕНЕНИЕ: Только ADMIN может создавать
    @Operation(summary = "Create new room", description = "Создание новой аудитории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Room created successfully", content = @Content(schema = @Schema(implementation = RoomResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<RoomResponseDto> createRoom(@Valid @RequestBody RoomDto roomDto) {
        RoomResponseDto createdRoom = roomService.createRoomDto(roomDto);
        return ResponseEntity.created(URI.create("/admin/rooms/" + createdRoom.getId())).body(createdRoom);
    }

    @PutMapping("/update-room/{roomId}")
    @PreAuthorize("hasRole('ADMIN')") // ИЗМЕНЕНИЕ: Только ADMIN может обновлять
    @Operation(summary = "Update room", description = "Обновление данных аудитории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room updated successfully", content = @Content(schema = @Schema(implementation = RoomResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Room not found")})
    public ResponseEntity<RoomResponseDto> updateRoom(
            @Parameter(description = "Room ID", required = true) @PathVariable Long roomId,
            @Valid @RequestBody RoomDto roomDto) {
        RoomResponseDto updatedRoom = roomService.updateRoomDto(roomId, roomDto);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/delete-room/{roomId}")
    @PreAuthorize("hasRole('ADMIN')") // ИЗМЕНЕНИЕ: Только ADMIN может удалять
    @Operation(summary = "Delete room", description = "Удаление аудитории")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Room deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Room not found")})
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "Room ID", required = true) @PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    // GET методы наследуют hasAnyRole('ADMIN', 'DISPATCHER') с уровня класса
    @GetMapping("/room/{roomId}")
    @Operation(summary = "Get room by ID", description = "Получение данных аудитории по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room found", content = @Content(schema = @Schema(implementation = RoomResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Room not found")})
    public ResponseEntity<RoomResponseDto> getRoom(
            @Parameter(description = "Room ID", required = true) @PathVariable Long roomId) {
        RoomResponseDto room = roomService.findByIdDto(roomId);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/rooms")
    @Operation(summary = "Get all rooms", description = "Получение списка всех аудиторий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rooms found", content = @Content(schema = @Schema(implementation = RoomResponseDto.class)))})
    public ResponseEntity<List<RoomResponseDto>> getAllRooms() {
        List<RoomResponseDto> rooms = roomService.findAllDto();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/rooms/list")
    @Operation(summary = "Get paginated rooms list with search", description = "Получение пагинированного списка аудиторий с поиском")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rooms found", content = @Content(schema = @Schema(implementation = PageResponse.class)))})
    public ResponseEntity<PageResponse<RoomListViewDto>> getRoomsList(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поисковый запрос (частичное совпадение)") @RequestParam(required = false) String search) {
        PageResponse<RoomListViewDto> response = roomService.findAllListView(page, size, search);
        return ResponseEntity.ok(response);
    }
}
