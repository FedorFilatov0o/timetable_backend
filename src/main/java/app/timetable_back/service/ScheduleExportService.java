package app.timetable_back.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.timetable_back.dto.ScheduleExportDto;
import app.timetable_back.entity.*;
import app.timetable_back.repository.LessonRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleExportService {

    private final LessonRepository lessonRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public ScheduleExportDto getScheduleForDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Lesson> lessons = lessonRepository.findByDateRange(startDateTime, endDateTime);

        List<Room> sortedRooms = lessons.stream().flatMap(l -> l.getLessonRooms().stream()).map(LessonRoom::getRoom)
                .filter(Objects::nonNull).distinct()
                .sorted(Comparator.comparing(Room::getRoomNumber, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        List<ScheduleExportDto.RoomInfo> roomInfos = sortedRooms.stream()
                .map(r -> ScheduleExportDto.RoomInfo.builder().id(r.getId()).roomNumber(r.getRoomNumber())
                        .building(r.getBuilding()).capacity(r.getCapacity()).build())
                .collect(Collectors.toList());

        Map<LessonKey, List<StudentGroup>> aggregated = new LinkedHashMap<>();
        for (Lesson lesson : lessons) {
            if (lesson.getLessonRooms().isEmpty())
                continue;

            LocalDate date = lesson.getStartAt().toLocalDate();
            String timeRange = formatTimeRange(lesson);

            for (LessonRoom lr : lesson.getLessonRooms()) {
                if (lr.getRoom() == null)
                    continue;
                LessonKey key = new LessonKey(date, timeRange, lr.getRoom().getId());

                if (lesson.getLessonStudentGroups() != null) {
                    for (LessonStudentGroup lsg : lesson.getLessonStudentGroups()) {
                        aggregated.computeIfAbsent(key, k -> new ArrayList<>()).add(lsg.getGroup());
                    }
                }
            }
        }

        List<ScheduleExportDto.LessonInfo> lessonInfos = new ArrayList<>();
        for (Map.Entry<LessonKey, List<StudentGroup>> entry : aggregated.entrySet()) {
            LessonKey key = entry.getKey();
            List<StudentGroup> groups = entry.getValue();

            Lesson sourceLesson = findSourceLesson(lessons, key);
            if (sourceLesson == null)
                continue;

            String groupsList = groups.stream().map(StudentGroup::getName).distinct().sorted()
                    .collect(Collectors.joining(", "));

            String teacherName = formatTeacherName(sourceLesson.getTeacher());
            String subjectName = sourceLesson.getSubject() != null ? sourceLesson.getSubject().getName() : "";
            String colorKey = subjectName + "|" + groupsList + "|" + teacherName;

            Room room = sourceLesson.getLessonRooms().stream().map(LessonRoom::getRoom)
                    .filter(r -> r != null && r.getId().equals(key.roomId)).findFirst().orElse(null);

            lessonInfos.add(ScheduleExportDto.LessonInfo.builder().roomId(key.roomId)
                    .roomName(room != null ? room.getRoomNumber() : "")
                    .startTime(key.date + " " + key.timeRange.split("-")[0])
                    .endTime(key.date + " " + key.timeRange.split("-")[1]).timeRange(key.timeRange)
                    .subjectName(subjectName).teacherName(teacherName).groupsList(groupsList)
                    .building(room != null ? room.getBuilding() : "").colorKey(colorKey).build());
        }

        Map<LocalDate, List<ScheduleExportDto.LessonInfo>> scheduleByDate = lessonInfos.stream()
                .collect(Collectors.groupingBy(dto -> LocalDate.parse(dto.getStartTime().substring(0, 10)),
                        LinkedHashMap::new, Collectors.toList()));

        List<ScheduleExportDto.TimeSlot> timeSlots = lessonInfos.stream()
                .collect(Collectors.toMap(ScheduleExportDto.LessonInfo::getTimeRange,
                        dto -> ScheduleExportDto.TimeSlot.builder().timeRange(dto.getTimeRange())
                                .sortKey(dto.getStartTime()).build(),
                        (existing, replacement) -> existing, LinkedHashMap::new))
                .values().stream().sorted(Comparator.comparing(ScheduleExportDto.TimeSlot::getSortKey))
                .collect(Collectors.toList());

        for (List<ScheduleExportDto.LessonInfo> dayLessons : scheduleByDate.values()) {
            detectHorizontalMerges(dayLessons, timeSlots, sortedRooms);
        }

        return ScheduleExportDto.builder().scheduleByDate(scheduleByDate).rooms(roomInfos).timeSlots(timeSlots).build();
    }

    private Lesson findSourceLesson(List<Lesson> lessons, LessonKey key) {
        return lessons.stream()
                .filter(l -> l.getLessonRooms().stream()
                        .anyMatch(lr -> lr.getRoom() != null && lr.getRoom().getId().equals(key.roomId)))
                .filter(l -> l.getStartAt().toLocalDate().equals(key.date))
                .filter(l -> formatTimeRange(l).equals(key.timeRange)).findFirst().orElse(null);
    }

    private void detectHorizontalMerges(List<ScheduleExportDto.LessonInfo> dayLessons,
            List<ScheduleExportDto.TimeSlot> timeSlots, List<Room> sortedRooms) {
        for (ScheduleExportDto.TimeSlot slot : timeSlots) {
            Map<Long, ScheduleExportDto.LessonInfo> byRoom = dayLessons.stream()
                    .filter(l -> l.getTimeRange().equals(slot.getTimeRange()))
                    .collect(Collectors.toMap(ScheduleExportDto.LessonInfo::getRoomId, l -> l));

            int i = 0;
            while (i < sortedRooms.size()) {
                Room currentRoom = sortedRooms.get(i);
                ScheduleExportDto.LessonInfo currentLesson = byRoom.get(currentRoom.getId());

                if (currentLesson == null || currentLesson.getSubjectName() == null
                        || currentLesson.getSubjectName().isEmpty()) {
                    i++;
                    continue;
                }

                List<Long> mergeGroup = new ArrayList<>();
                mergeGroup.add(currentRoom.getId());
                int j = i + 1;

                while (j < sortedRooms.size()) {
                    Room nextRoom = sortedRooms.get(j);
                    ScheduleExportDto.LessonInfo nextLesson = byRoom.get(nextRoom.getId());
                    if (nextLesson != null && nextLesson.getCellContent().equals(currentLesson.getCellContent())) {
                        mergeGroup.add(nextRoom.getId());
                        j++;
                    } else {
                        break;
                    }
                }

                if (mergeGroup.size() > 1) {
                    currentLesson.setMergedRoomIds(mergeGroup);
                    for (int k = 1; k < mergeGroup.size(); k++) {
                        ScheduleExportDto.LessonInfo skip = byRoom.get(mergeGroup.get(k));
                        if (skip != null)
                            skip.setMergedRoomIds(Collections.emptyList());
                    }
                }
                i = j;
            }
        }
    }

    private String formatTimeRange(Lesson lesson) {
        return lesson.getStartAt().format(TIME_FORMATTER) + "-" + lesson.getEndAt().format(TIME_FORMATTER);
    }

    private String formatTeacherName(User teacher) {
        if (teacher == null)
            return "";
        String firstName = teacher.getFirstName();
        String lastName = teacher.getLastName();

        if (firstName == null || firstName.isEmpty())
            return lastName != null ? lastName : "";
        if (lastName == null || lastName.isEmpty())
            return firstName;

        return lastName + " "
                + Arrays.stream(firstName.split("\\s+"))
                        .map(p -> p.isEmpty() ? "" : String.valueOf(Character.toUpperCase(p.charAt(0))) + ".")
                        .collect(Collectors.joining());
    }

    private static class LessonKey {
        private final LocalDate date;
        private final String timeRange;
        private final Long roomId;

        public LessonKey(LocalDate date, String timeRange, Long roomId) {
            this.date = date;
            this.timeRange = timeRange;
            this.roomId = roomId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            LessonKey that = (LessonKey) o;
            return Objects.equals(date, that.date) && Objects.equals(timeRange, that.timeRange)
                    && Objects.equals(roomId, that.roomId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, timeRange, roomId);
        }
    }
}