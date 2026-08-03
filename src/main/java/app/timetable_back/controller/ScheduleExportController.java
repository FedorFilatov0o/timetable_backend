package app.timetable_back.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import app.timetable_back.dto.ScheduleExportDto;
import app.timetable_back.service.ExcelExportService;
import app.timetable_back.service.ScheduleExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/export")
// ИЗМЕНЕНИЕ: Добавлена роль DISPATCHER для экспорта расписания
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'DISPATCHER')")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Schedule Export", description = "API для экспорта расписания в Excel")
public class ScheduleExportController {
    private final ScheduleExportService scheduleExportService;
    private final ExcelExportService excelExportService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/schedule")
    @Operation(summary = "Export schedule to Excel", description = "Выгрузка расписания в Excel формате по диапазону дат")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл успешно сгенерирован", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "400", description = "Некорректный диапазон дат"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<byte[]> exportSchedule(
            @Parameter(description = "Дата начала (YYYY-MM-DD)", required = true) @RequestParam("startDate") String startDate,
            @Parameter(description = "Дата окончания (YYYY-MM-DD)", required = true) @RequestParam("endDate") String endDate) {

        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

        // Валидация диапазона
        if (start.isAfter(end)) {
            return ResponseEntity.badRequest().build();
        }

        // Получаем данные расписания
        ScheduleExportDto scheduleDto = scheduleExportService.getScheduleForDateRange(start, end);

        // Генерируем Excel
        byte[] excelData;
        try {
            excelData = excelExportService.generateExcel(scheduleDto);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при генерации Excel файла", e);
        }

        // Формируем имя файла с версией
        String fileName = "schedule_v2_" + startDate + "_" + endDate + ".xlsx";

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }
}
