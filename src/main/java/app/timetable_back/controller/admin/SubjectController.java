package app.timetable_back.controller.admin;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import app.timetable_back.dto.PageResponse;
import app.timetable_back.dto.SubjectDto;
import app.timetable_back.dto.SubjectListViewDto;
import app.timetable_back.dto.SubjectResponseDto;
import app.timetable_back.service.SubjectService;
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
@Tag(name = "Subject Management", description = "API для управления предметами")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping("/create-subject")
    @PreAuthorize("hasRole('ADMIN')") // ИЗМЕНЕНИЕ: Только ADMIN может создавать
    @Operation(summary = "Create new subject", description = "Создание нового предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subject created successfully", content = @Content(schema = @Schema(implementation = SubjectResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<SubjectResponseDto> createSubject(@Valid @RequestBody SubjectDto subjectDto) {
        SubjectResponseDto createdSubject = subjectService.createSubjectDto(subjectDto);
        return ResponseEntity.created(URI.create("/admin/subjects/" + createdSubject.getId())).body(createdSubject);
    }

    @PutMapping("/update-subject/{subjectId}")
    @PreAuthorize("hasRole('ADMIN')") // ИЗМЕНЕНИЕ: Только ADMIN может обновлять
    @Operation(summary = "Update subject", description = "Обновление данных предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subject updated successfully", content = @Content(schema = @Schema(implementation = SubjectResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Subject not found")})
    public ResponseEntity<SubjectResponseDto> updateSubject(
            @Parameter(description = "Subject ID", required = true) @PathVariable Long subjectId,
            @Valid @RequestBody SubjectDto subjectDto) {
        SubjectResponseDto updatedSubject = subjectService.updateSubjectDto(subjectId, subjectDto);
        return ResponseEntity.ok(updatedSubject);
    }

    @DeleteMapping("/delete-subject/{subjectId}")
    @PreAuthorize("hasRole('ADMIN')") // ИЗМЕНЕНИЕ: Только ADMIN может удалять
    @Operation(summary = "Delete subject", description = "Удаление предмета")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Subject deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Subject not found")})
    public ResponseEntity<Void> deleteSubject(
            @Parameter(description = "Subject ID", required = true) @PathVariable Long subjectId) {
        subjectService.deleteSubject(subjectId);
        return ResponseEntity.noContent().build();
    }

    // GET методы наследуют hasAnyRole('ADMIN', 'DISPATCHER') с уровня класса
    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Get subject by ID", description = "Получение данных предмета по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subject found", content = @Content(schema = @Schema(implementation = SubjectResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Subject not found")})
    public ResponseEntity<SubjectResponseDto> getSubject(
            @Parameter(description = "Subject ID", required = true) @PathVariable Long subjectId) {
        SubjectResponseDto subject = subjectService.findByIdDto(subjectId);
        return ResponseEntity.ok(subject);
    }

    @GetMapping("/subjects")
    @Operation(summary = "Get all subjects", description = "Получение списка всех предметов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subjects found", content = @Content(schema = @Schema(implementation = SubjectResponseDto.class)))})
    public ResponseEntity<List<SubjectResponseDto>> getAllSubjects() {
        List<SubjectResponseDto> subjects = subjectService.findAllDto();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/subjects/list")
    @Operation(summary = "Get paginated subjects list with search", description = "Получение пагинированного списка предметов с поиском")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subjects found", content = @Content(schema = @Schema(implementation = PageResponse.class)))})
    public ResponseEntity<PageResponse<SubjectListViewDto>> getSubjectsList(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поисковый запрос (частичное совпадение)") @RequestParam(required = false) String search) {
        PageResponse<SubjectListViewDto> response = subjectService.findAllListView(page, size, search);
        return ResponseEntity.ok(response);
    }
}
