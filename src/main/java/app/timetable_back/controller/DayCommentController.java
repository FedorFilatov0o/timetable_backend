package app.timetable_back.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.timetable_back.dto.DayCommentDto;
import app.timetable_back.dto.DayCommentListViewDto;
import app.timetable_back.dto.DayCommentResponseDto;
import app.timetable_back.dto.PageResponse;
import app.timetable_back.service.DayCommentService;
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
@RequestMapping("/day-comments")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Day Comment Management", description = "API для управления комментариями к дням")
public class DayCommentController {

    private final DayCommentService dayCommentService;

    public DayCommentController(DayCommentService dayCommentService) {
        this.dayCommentService = dayCommentService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create new day comment", description = "Создание нового комментария к дню")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Day comment created successfully", content = @Content(schema = @Schema(implementation = DayCommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<DayCommentResponseDto> createDayComment(@Valid @RequestBody DayCommentDto dayCommentDto) {
        DayCommentResponseDto createdDayComment = dayCommentService.createDayCommentDto(dayCommentDto);
        return ResponseEntity.created(URI.create("/admin/day-comments/" + createdDayComment.getId()))
                .body(createdDayComment);
    }

    @PutMapping("/update/{commentId}")
    @Operation(summary = "Update day comment", description = "Обновление комментария к дню")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Day comment updated successfully", content = @Content(schema = @Schema(implementation = DayCommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Day comment or User not found")})
    public ResponseEntity<DayCommentResponseDto> updateDayComment(
            @Parameter(description = "Day Comment ID", required = true) @PathVariable Long commentId,
            @Valid @RequestBody DayCommentDto dayCommentDto) {
        DayCommentResponseDto updatedDayComment = dayCommentService.updateDayCommentDto(commentId, dayCommentDto);
        return ResponseEntity.ok(updatedDayComment);
    }

    @DeleteMapping("/delete/{commentId}")
    @Operation(summary = "Delete day comment", description = "Удаление комментария к дню")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Day comment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Day comment not found")})
    public ResponseEntity<Void> deleteDayComment(
            @Parameter(description = "Day Comment ID", required = true) @PathVariable Long commentId) {
        dayCommentService.deleteDayComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Get day comment by ID", description = "Получение комментария по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Day comment found", content = @Content(schema = @Schema(implementation = DayCommentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Day comment not found")})
    public ResponseEntity<DayCommentResponseDto> getDayComment(
            @Parameter(description = "Day Comment ID", required = true) @PathVariable Long commentId) {
        DayCommentResponseDto dayComment = dayCommentService.findByIdDto(commentId);
        return ResponseEntity.ok(dayComment);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all day comments", description = "Получение списка всех комментариев")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Day comments found", content = @Content(schema = @Schema(implementation = DayCommentResponseDto.class)))})
    public ResponseEntity<List<DayCommentResponseDto>> getAllDayComments() {
        List<DayCommentResponseDto> dayComments = dayCommentService.findAllDto();
        return ResponseEntity.ok(dayComments);
    }

    @GetMapping("/list")
    @Operation(summary = "Get paginated day comments list", description = "Получение пагинированного списка комментариев (без id, createdAt)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Day comments found", content = @Content(schema = @Schema(implementation = PageResponse.class)))})
    public ResponseEntity<PageResponse<DayCommentListViewDto>> getDayCommentsList(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size) {
        PageResponse<DayCommentListViewDto> response = dayCommentService.findAllListView(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-date")
    @Operation(summary = "Get day comments by date", description = "Получение всех комментариев, привязанных к конкретной дате")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments found (may be empty list)", content = @Content(schema = @Schema(implementation = DayCommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date format. Expected: YYYY-MM-DD"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<List<DayCommentResponseDto>> getDayCommentsByDate(
            @Parameter(description = "Дата в формате ISO (YYYY-MM-DD)", example = "2026-03-15", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<DayCommentResponseDto> comments = dayCommentService.findByDateDto(date);
        return ResponseEntity.ok(comments);
    }
}
