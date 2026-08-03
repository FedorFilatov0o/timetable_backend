package app.timetable_back.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import app.timetable_back.dto.LessonResponseDto;
import app.timetable_back.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/schedule")
// ИЗМЕНЕНИЕ: Добавлена роль DISPATCHER для просмотра расписания
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'DISPATCHER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Schedule View", description = "API для просмотра расписания")
public class ScheduleController {
    private final LessonService lessonService;

    public ScheduleController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get lessons by specific date", description = "Возвращает все уроки за указанную дату")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lessons found", content = @Content(schema = @Schema(implementation = LessonResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date format")})
    public ResponseEntity<List<LessonResponseDto>> getLessonsByDate(
            @Parameter(description = "Дата в формате YYYY-MM-DD", example = "2026-05-07") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(lessonService.findLessonsByDate(date));
    }

    @GetMapping("/range")
    @Operation(summary = "Get lessons by date range", description = "Возвращает уроки за указанный промежуток дат (время не требуется)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lessons found", content = @Content(schema = @Schema(implementation = LessonResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range or format")})
    public ResponseEntity<List<LessonResponseDto>> getLessonsByRange(
            @Parameter(description = "Начало периода (YYYY-MM-DD)", example = "2026-05-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Конец периода (YYYY-MM-DD)", example = "2026-05-31") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(lessonService.findLessonsByDateRange(start, end));
    }
}
