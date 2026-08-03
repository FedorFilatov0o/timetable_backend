package app.timetable_back.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.timetable_back.dto.*;
import app.timetable_back.entity.*;
import app.timetable_back.repository.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonRoomRepository lessonRoomRepository;
    private final LessonStudentGroupRepository lessonStudentGroupRepository;
    private final GroupRepository groupRepository;
    private final RoomRepository roomRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ScheduleValidationService validationService;

    public Lesson createLesson(LessonDto dto) {
        validateTimeRange(dto.getStartAt(), dto.getEndAt());

        Lesson lesson = Lesson.builder().startAt(dto.getStartAt()).endAt(dto.getEndAt()).ruleType(dto.getRuleType())
                .subject(findSubject(dto.getSubjectId())).teacher(findTeacher(dto.getTeacherId())).build();

        Lesson savedLesson = lessonRepository.save(lesson);

        saveRooms(savedLesson, dto.getRoomIds(), dto.getGroupIds());
        saveGroups(savedLesson, dto.getGroupIds(), null);

        return savedLesson;
    }

    public Lesson updateLesson(Long lessonId, LessonDto dto) {
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Занятие с ID '" + lessonId + "' не найдено"));

        validateTimeRange(dto.getStartAt(), dto.getEndAt());

        existingLesson.setStartAt(dto.getStartAt());
        existingLesson.setEndAt(dto.getEndAt());
        existingLesson.setRuleType(dto.getRuleType());

        if (dto.getSubjectId() != null)
            existingLesson.setSubject(findSubject(dto.getSubjectId()));
        if (dto.getTeacherId() != null)
            existingLesson.setTeacher(findTeacher(dto.getTeacherId()));

        Lesson savedLesson = lessonRepository.save(existingLesson);

        if (dto.getRoomIds() != null) {
            lessonRoomRepository.deleteByLessonId(lessonId);
            saveRooms(savedLesson, dto.getRoomIds(), dto.getGroupIds());
        }

        if (dto.getGroupIds() != null) {
            lessonStudentGroupRepository.deleteAll(existingLesson.getLessonStudentGroups());
            saveGroups(savedLesson, dto.getGroupIds(), lessonId);
        }

        return savedLesson;
    }

    public void deleteLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Занятие с ID '" + lessonId + "' не найдено");
        }
        lessonRepository.deleteById(lessonId);
    }

    @Transactional(readOnly = true)
    public Lesson findById(Long lessonId) {
        return lessonRepository.findByIdWithDetails(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Занятие с ID '" + lessonId + "' не найдено"));
    }

    @Transactional(readOnly = true)
    public List<Lesson> findAll() {
        return new ArrayList<>(lessonRepository.findAllWithDetails());
    }

    public LessonResponseDto createLessonDto(LessonDto dto) {
        return toDto(createLesson(dto));
    }

    public LessonResponseDto updateLessonDto(Long lessonId, LessonDto dto) {
        return toDto(updateLesson(lessonId, dto));
    }

    @Transactional(readOnly = true)
    public List<LessonResponseDto> findAllDto() {
        return lessonRepository.findAllWithDetails().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<LessonListViewDto> findAllListView(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Lesson> lessonPage = lessonRepository.findAll(pageable);

        List<LessonListViewDto> content = lessonPage.getContent().stream().map(this::toListViewDto)
                .collect(Collectors.toList());

        return PageResponse.<LessonListViewDto>builder().content(content).page(page).size(size)
                .totalElements(lessonPage.getTotalElements()).totalPages(lessonPage.getTotalPages()).build();
    }

    @Transactional(readOnly = true)
    public LessonResponseDto findByIdDto(Long lessonId) {
        return toDto(findById(lessonId));
    }

    @Transactional(readOnly = true)
    public List<LessonResponseDto> findLessonsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return lessonRepository.findByDateRange(startOfDay, endOfDay).stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LessonResponseDto> findLessonsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Дата окончания периода не может быть раньше даты начала");
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return lessonRepository.findByDateRange(startDateTime, endDateTime).stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new IllegalArgumentException("Время окончания занятия должно быть строго позже времени его начала");
        }
    }

    private Subject findSubject(Long subjectId) {
        if (subjectId == null)
            return null;
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Предмет с ID '" + subjectId + "' не найден"));
    }

    private User findTeacher(Long teacherId) {
        if (teacherId == null)
            return null;
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель с ID '" + teacherId + "' не найден"));
        validationService.validateTeacherRole(teacher.getId());
        return teacher;
    }

    private void saveRooms(Lesson lesson, List<Long> roomIds, List<Long> groupIds) {
        if (roomIds == null || roomIds.isEmpty())
            return;

        for (Long roomId : roomIds) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Аудитория с ID '" + roomId + "' не найдена"));

            if (groupIds != null && !groupIds.isEmpty()) {
                if (!validationService.isRoomCapacitySufficientForGroups(roomId, groupIds)) {
                    throw new IllegalArgumentException("Вместимости аудитории '" + room.getRoomNumber()
                            + "' недостаточно для размещения всех студентов");
                }
            }

            LessonRoomId lrId = new LessonRoomId(lesson.getId(), roomId);
            LessonRoom lessonRoom = LessonRoom.builder().id(lrId).lesson(lesson).room(room).build();
            lessonRoomRepository.save(lessonRoom);
        }
    }

    private void saveGroups(Lesson lesson, List<Long> groupIds, Long excludeLessonId) {
        if (groupIds == null || groupIds.isEmpty())
            return;

        for (Long groupId : groupIds) {
            StudentGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Группа с ID '" + groupId + "' не найдена"));

            if (validationService.hasGroupConflict(groupId, lesson.getStartAt(), lesson.getEndAt(), excludeLessonId)) {
                throw new IllegalArgumentException(
                        "Обнаружен конфликт расписания: группа '" + group.getName() + "' уже занята в это время");
            }

            LessonStudentGroupId id = new LessonStudentGroupId(lesson.getId(), groupId);
            LessonStudentGroup lsg = LessonStudentGroup.builder().id(id).lesson(lesson).group(group).build();
            lessonStudentGroupRepository.save(lsg);
        }
    }

    // --- Маппинг в DTO ---

    private LessonResponseDto toDto(Lesson lesson) {
        List<Long> groupIds = lesson.getLessonStudentGroups() != null
                ? lesson.getLessonStudentGroups().stream().map(lsg -> lsg.getGroup().getId())
                        .collect(Collectors.toList())
                : List.of();

        List<RoomResponseDto> rooms = lesson.getLessonRooms() != null
                ? lesson.getLessonRooms().stream().map(lr -> mapRoomResponse(lr.getRoom())).collect(Collectors.toList())
                : List.of();

        return LessonResponseDto.builder().id(lesson.getId()).startAt(lesson.getStartAt()).endAt(lesson.getEndAt())
                .ruleType(lesson.getRuleType())
                .createdAt(lesson.getCreatedAt() != null ? lesson.getCreatedAt().toLocalDateTime() : null)
                .updatedAt(lesson.getUpdatedAt() != null ? lesson.getUpdatedAt().toLocalDateTime() : null).rooms(rooms)
                .subject(lesson.getSubject() != null ? mapSubjectResponse(lesson.getSubject()) : null)
                .teacher(lesson.getTeacher() != null ? mapUserResponse(lesson.getTeacher()) : null).groupIds(groupIds)
                .build();
    }

    private LessonListViewDto toListViewDto(Lesson lesson) {
        List<Long> groupIds = lesson.getLessonStudentGroups() != null
                ? lesson.getLessonStudentGroups().stream().map(lsg -> lsg.getGroup().getId())
                        .collect(Collectors.toList())
                : List.of();

        List<RoomListViewDto> rooms = lesson.getLessonRooms() != null
                ? lesson.getLessonRooms().stream().map(lr -> mapRoomListView(lr.getRoom())).collect(Collectors.toList())
                : List.of();

        return LessonListViewDto.builder().startAt(lesson.getStartAt()).endAt(lesson.getEndAt())
                .ruleType(lesson.getRuleType()).rooms(rooms)
                .subject(lesson.getSubject() != null ? mapSubjectListView(lesson.getSubject()) : null)
                .teacher(lesson.getTeacher() != null ? mapUserListView(lesson.getTeacher()) : null).groupIds(groupIds)
                .build();
    }

    private RoomResponseDto mapRoomResponse(Room room) {
        if (room == null)
            return null;
        return RoomResponseDto.builder().id(room.getId()).roomNumber(room.getRoomNumber()).building(room.getBuilding())
                .capacity(room.getCapacity()).createdAt(room.getCreatedAt()).updatedAt(room.getUpdatedAt()).build();
    }

    private RoomListViewDto mapRoomListView(Room room) {
        if (room == null)
            return null;
        return RoomListViewDto.builder().roomNumber(room.getRoomNumber()).building(room.getBuilding())
                .capacity(room.getCapacity()).build();
    }

    private SubjectResponseDto mapSubjectResponse(Subject subject) {
        if (subject == null)
            return null;
        return SubjectResponseDto.builder().id(subject.getId()).name(subject.getName()).code(subject.getCode())
                .faculty(subject.getFaculty()).description(subject.getDescription()).createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt()).build();
    }

    private SubjectListViewDto mapSubjectListView(Subject subject) {
        if (subject == null)
            return null;
        return SubjectListViewDto.builder().name(subject.getName()).code(subject.getCode())
                .faculty(subject.getFaculty()).description(subject.getDescription()).build();
    }

    private UserResponseDto mapUserResponse(User user) {
        if (user == null)
            return null;
        return UserResponseDto.builder().id(user.getId()).firstName(user.getFirstName()).lastName(user.getLastName())
                .email(user.getEmail()).role(user.getRole()).phone(user.getPhone()).createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt()).build();
    }

    private UserListViewDto mapUserListView(User user) {
        if (user == null)
            return null;
        return UserListViewDto.builder().firstName(user.getFirstName()).lastName(user.getLastName())
                .email(user.getEmail()).role(user.getRole()).phone(user.getPhone()).build();
    }
}