package app.timetable_back.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO для экспорта расписания в Excel (структура по аудиториям) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExportDto {

    private Map<LocalDate, List<LessonInfo>> scheduleByDate;
    private List<RoomInfo> rooms;
    private List<TimeSlot> timeSlots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomInfo {
        private Long id;
        private String roomNumber;
        private String building;
        private Integer capacity;

        public String getDisplayName() {
            return roomNumber + " (" + capacity + " мест)";
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String timeRange;
        private String sortKey;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonInfo {
        private Long roomId;
        private String roomName;
        private String startTime;
        private String endTime;
        private String timeRange;
        private String subjectName;
        private String teacherName;
        private String groupsList;
        private String building;
        private String colorKey;
        private List<Long> mergedRoomIds;

        /** Формирует текст для ячейки Excel. */
        public String getCellContent() {
            if (subjectName == null || subjectName.isEmpty()) {
                return "Свободно";
            }
            StringBuilder sb = new StringBuilder(subjectName);
            if (groupsList != null && !groupsList.isEmpty()) {
                sb.append("\nГруппы: ").append(groupsList);
            }
            if (teacherName != null && !teacherName.isEmpty()) {
                sb.append("\n").append(teacherName);
            }
            return sb.toString();
        }
    }
}
