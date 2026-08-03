package app.timetable_back.controller.dispatcher;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import app.timetable_back.dto.LessonDto;
import app.timetable_back.dto.LessonListViewDto;
import app.timetable_back.dto.LessonResponseDto;
import app.timetable_back.dto.PageResponse;
import app.timetable_back.service.LessonService;
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
@RequestMapping("/dispatcher")
@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')") // Только диспетчер может формировать расписание
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dispatcher Lesson Management", description = "API для формирования расписания (Диспетчер)")
public class LessonController {
    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("/create-lesson")
    @Operation(summary = "Create new lesson", description = "Создание нового занятия с проверкой конфликтов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lesson created successfully", content = @Content(schema = @Schema(implementation = LessonResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or schedule conflict"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Related entity not found (room, subject, teacher, group)")})
    public ResponseEntity<LessonResponseDto> createLesson(@Valid @RequestBody LessonDto lessonDto) {
        LessonResponseDto createdLesson = lessonService.createLessonDto(lessonDto);
        return ResponseEntity.created(URI.create("/dispatcher/lessons/" + createdLesson.getId())).body(createdLesson);
    }

    @PutMapping("/update-lesson/{lessonId}")
    @Operation(summary = "Update lesson", description = "Обновление занятия с проверкой конфликтов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lesson updated successfully", content = @Content(schema = @Schema(implementation = LessonResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or schedule conflict"),
            @ApiResponse(responseCode = "404", description = "Lesson or related entity not found")})
    public ResponseEntity<LessonResponseDto> updateLesson(
            @Parameter(description = "Lesson ID", required = true) @PathVariable Long lessonId,
            @Valid @RequestBody LessonDto lessonDto) {
        return ResponseEntity.ok(lessonService.updateLessonDto(lessonId, lessonDto));
    }

    @DeleteMapping("/delete-lesson/{lessonId}")
    @Operation(summary = "Delete lesson", description = "Удаление занятия")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Lesson deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Lesson not found")})
    public ResponseEntity<Void> deleteLesson(
            @Parameter(description = "Lesson ID", required = true) @PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lesson/{lessonId}")
    @Operation(summary = "Get lesson by ID", description = "Получение данных занятия по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lesson found", content = @Content(schema = @Schema(implementation = LessonResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Lesson not found")})
    public ResponseEntity<LessonResponseDto> getLesson(
            @Parameter(description = "Lesson ID", required = true) @PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonService.findByIdDto(lessonId));
    }

    @GetMapping("/lessons")
    @Operation(summary = "Get all lessons", description = "Получение списка всех занятий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lessons found", content = @Content(schema = @Schema(implementation = LessonResponseDto.class)))})
    public ResponseEntity<List<LessonResponseDto>> getAllLessons() {
        return ResponseEntity.ok(lessonService.findAllDto());
    }

    @GetMapping("/lessons/list")
    @Operation(summary = "Get paginated lessons list", description = "Получение пагинированного списка занятий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lessons found", content = @Content(schema = @Schema(implementation = PageResponse.class)))})
    public ResponseEntity<PageResponse<LessonListViewDto>> getLessonsList(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(lessonService.findAllListView(page, size));
    }
}
