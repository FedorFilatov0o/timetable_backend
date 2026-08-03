package app.timetable_back.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import app.timetable_back.dto.ScheduleExportDto;

@Service
public class ExcelExportService {

    private static final String DAY_NAME_FORMAT = "EEEE, dd.MM.yyyy";
    private static final Locale RUSSIAN_LOCALE = Locale.of("ru", "RU");

    private static final short FONT_SIZE_LESSON = 14;
    private static final short FONT_SIZE_HEADER = 22;
    private static final short FONT_SIZE_TIME = 14;
    private static final short FONT_SIZE_ROOM_HEADER = 14;

    private static final int COLUMN_WIDTH_TIME = 20 * 256;
    private static final int COLUMN_WIDTH_ROOM = 30 * 256;

    private static final float ROW_HEIGHT_LESSON = 70f;
    private static final float ROW_HEIGHT_HEADER = 50f;

    public byte[] generateExcel(ScheduleExportDto scheduleDto) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            List<LocalDate> sortedDates = new ArrayList<>(scheduleDto.getScheduleByDate().keySet());
            sortedDates.sort(LocalDate::compareTo);

            for (LocalDate date : sortedDates) {
                List<ScheduleExportDto.LessonInfo> lessons = scheduleDto.getScheduleByDate().get(date);
                createSheet((XSSFWorkbook) workbook, date, lessons, scheduleDto.getRooms(), scheduleDto.getTimeSlots());
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createSheet(XSSFWorkbook wb, LocalDate date, List<ScheduleExportDto.LessonInfo> lessons,
            List<ScheduleExportDto.RoomInfo> rooms, List<ScheduleExportDto.TimeSlot> timeSlots) {

        String sheetName = date.format(DateTimeFormatter.ofPattern("dd.MM", RUSSIAN_LOCALE));
        if (sheetName.length() > 31)
            sheetName = sheetName.substring(0, 31);
        Sheet sheet = wb.createSheet(sheetName);

        CellStyle dateHeaderStyle = createDateHeaderStyle(wb);
        CellStyle timeHeaderStyle = createTimeHeaderStyle(wb);
        CellStyle timeValueStyle = createTimeValueStyle(wb);
        CellStyle availableStyle = createAvailableStyle(wb);

        Map<String, CellStyle> lessonColorCache = new HashMap<>();

        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(ROW_HEIGHT_HEADER);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue(
                capitalizeFirst(date.format(DateTimeFormatter.ofPattern(DAY_NAME_FORMAT, RUSSIAN_LOCALE))));
        headerCell.setCellStyle(dateHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, rooms.size()));

        Row roomHeaderRow = sheet.createRow(1);
        roomHeaderRow.setHeightInPoints(ROW_HEIGHT_HEADER);

        Cell timeHeader = roomHeaderRow.createCell(0);
        timeHeader.setCellValue("Время");
        timeHeader.setCellStyle(timeHeaderStyle);

        for (int i = 0; i < rooms.size(); i++) {
            Cell cell = roomHeaderRow.createCell(i + 1);
            cell.setCellValue(rooms.get(i).getDisplayName());
            cell.setCellStyle(createRoomHeaderStyle(wb, i));
        }

        int rowNum = 2;
        Set<String> processedMerges = new HashSet<>();

        for (ScheduleExportDto.TimeSlot slot : timeSlots) {
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(ROW_HEIGHT_LESSON);

            Cell timeCell = row.createCell(0);
            timeCell.setCellValue(slot.getTimeRange());
            timeCell.setCellStyle(timeValueStyle);

            for (int colIdx = 0; colIdx < rooms.size(); colIdx++) {
                Long roomId = rooms.get(colIdx).getId();

                if (shouldSkipCell(lessons, roomId, slot.getTimeRange()))
                    continue;

                Cell lessonCell = row.createCell(colIdx + 1);
                ScheduleExportDto.LessonInfo lesson = findLesson(lessons, roomId, slot.getTimeRange());

                if (lesson != null && lesson.getSubjectName() != null && !lesson.getSubjectName().isEmpty()) {
                    lessonCell.setCellValue(lesson.getCellContent());

                    CellStyle colorStyle = lessonColorCache.computeIfAbsent(lesson.getCellContent(),
                            key -> createLessonStyleWithColor(wb, lesson.getCellContent()));
                    lessonCell.setCellStyle(colorStyle);

                    if (lesson.getMergedRoomIds() != null && lesson.getMergedRoomIds().size() > 1) {
                        int firstCol = colIdx + 1;
                        int lastCol = firstCol + lesson.getMergedRoomIds().size() - 1;
                        String mergeKey = date + "_" + slot.getTimeRange() + "_" + roomId;

                        if (!processedMerges.contains(mergeKey)) {
                            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, firstCol, lastCol));
                            processedMerges.add(mergeKey);
                        }
                    }
                } else {
                    lessonCell.setCellValue("Свободно");
                    lessonCell.setCellStyle(availableStyle);
                }
            }
        }

        sheet.setColumnWidth(0, COLUMN_WIDTH_TIME);
        for (int i = 0; i < rooms.size(); i++) {
            sheet.setColumnWidth(i + 1, COLUMN_WIDTH_ROOM);
        }
    }

    private boolean shouldSkipCell(List<ScheduleExportDto.LessonInfo> lessons, Long roomId, String timeRange) {
        ScheduleExportDto.LessonInfo lesson = findLesson(lessons, roomId, timeRange);
        return lesson != null && lesson.getMergedRoomIds() != null && !lesson.getMergedRoomIds().isEmpty()
                && !lesson.getMergedRoomIds().get(0).equals(roomId);
    }

    private ScheduleExportDto.LessonInfo findLesson(List<ScheduleExportDto.LessonInfo> lessons, Long roomId,
            String timeRange) {
        return lessons.stream()
                .filter(l -> Objects.equals(l.getRoomId(), roomId) && Objects.equals(l.getTimeRange(), timeRange))
                .findFirst().orElse(null);
    }

    private static int[] generatePastelColor(int seed) {
        int r = 215 + (Math.abs(seed * 13) % 40);
        int g = 220 + (Math.abs(seed * 29) % 35);
        int b = 225 + (Math.abs(seed * 41) % 30);
        return new int[]{r, g, b};
    }

    private XSSFCellStyle createBaseStyle(XSSFWorkbook wb, short fontSize, int[] rgb, boolean bold,
            HorizontalAlignment align, VerticalAlignment valign, boolean wrap) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints(fontSize);
        if (bold)
            font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);

        if (rgb != null) {
            XSSFColor color = new XSSFColor(new byte[]{(byte) rgb[0], (byte) rgb[1], (byte) rgb[2]}, null);
            style.setFillForegroundColor(color);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        applyBorders(style);
        style.setAlignment(align);
        style.setVerticalAlignment(valign);
        if (wrap)
            style.setWrapText(true);
        return style;
    }

    private CellStyle createDateHeaderStyle(XSSFWorkbook wb) {
        return createBaseStyle(wb, FONT_SIZE_HEADER, new int[]{255, 255, 255}, true, HorizontalAlignment.CENTER,
                VerticalAlignment.CENTER, false);
    }

    private CellStyle createTimeHeaderStyle(XSSFWorkbook wb) {
        return createBaseStyle(wb, FONT_SIZE_TIME, new int[]{255, 255, 255}, true, HorizontalAlignment.CENTER,
                VerticalAlignment.CENTER, false);
    }

    private CellStyle createTimeValueStyle(XSSFWorkbook wb) {
        return createBaseStyle(wb, FONT_SIZE_TIME, new int[]{255, 255, 255}, false, HorizontalAlignment.CENTER,
                VerticalAlignment.CENTER, false);
    }

    private CellStyle createRoomHeaderStyle(XSSFWorkbook wb, int idx) {
        int[] rgb = generatePastelColor(idx * 79 + 300);
        return createBaseStyle(wb, FONT_SIZE_ROOM_HEADER, rgb, true, HorizontalAlignment.CENTER,
                VerticalAlignment.CENTER, true);
    }

    private CellStyle createLessonStyleWithColor(XSSFWorkbook wb, String contentKey) {
        int[] rgb = generatePastelColor(contentKey.hashCode());
        return createBaseStyle(wb, FONT_SIZE_LESSON, rgb, false, HorizontalAlignment.CENTER, VerticalAlignment.CENTER,
                true);
    }

    private CellStyle createAvailableStyle(XSSFWorkbook wb) {
        return createBaseStyle(wb, FONT_SIZE_LESSON, new int[]{255, 255, 255}, false, HorizontalAlignment.CENTER,
                VerticalAlignment.CENTER, false);
    }

    private void applyBorders(CellStyle s) {
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}