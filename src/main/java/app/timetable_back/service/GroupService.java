package app.timetable_back.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.timetable_back.dto.GroupDto;
import app.timetable_back.dto.GroupListViewDto;
import app.timetable_back.dto.GroupResponseDto;
import app.timetable_back.dto.PageResponse;
import app.timetable_back.entity.StudentGroup;
import app.timetable_back.exception.EntityInUseException;
import app.timetable_back.repository.GroupRepository;
import app.timetable_back.repository.LessonRepository;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final LessonRepository lessonRepository;

    public GroupService(GroupRepository groupRepository, LessonRepository lessonRepository) {
        this.groupRepository = groupRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional(readOnly = true)
    public StudentGroup findById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group with id '" + id + "' not found"));
    }

    @Transactional
    public StudentGroup createGroup(GroupDto groupDto) {
        return groupRepository.save(StudentGroup.builder().name(groupDto.getName()).courseYear(groupDto.getCourseYear())
                .studentCount(groupDto.getStudentCount()).build());
    }

    @Transactional
    public StudentGroup updateGroup(Long id, GroupDto groupDto) {
        StudentGroup existing = findById(id);
        existing.setName(groupDto.getName());
        existing.setCourseYear(groupDto.getCourseYear());
        existing.setStudentCount(groupDto.getStudentCount());
        return groupRepository.save(existing);
    }

    @Transactional
    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new IllegalArgumentException("Group with id '" + id + "' not found");
        }

        Pageable firstResult = PageRequest.of(0, 1);

        lessonRepository.findFirstByGroupId(id, firstResult).stream().findFirst().ifPresent(lesson -> {
            String date = lesson.getStartAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            throw new EntityInUseException("Нельзя удалять группу. Используется в уроке " + date);
        });

        groupRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<StudentGroup> findAll() {
        return groupRepository.findAll();
    }

    @Transactional
    public GroupResponseDto createGroupDto(GroupDto dto) {
        return toDto(createGroup(dto));
    }

    @Transactional
    public GroupResponseDto updateGroupDto(Long id, GroupDto dto) {
        return toDto(updateGroup(id, dto));
    }

    @Transactional(readOnly = true)
    public GroupResponseDto findByIdDto(Long id) {
        return toDto(findById(id));
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> findAllDto() {
        return groupRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<GroupListViewDto> findAllListView(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        String searchPattern = (search != null && !search.trim().isEmpty())
                ? "%" + search.trim().toLowerCase() + "%"
                : null;
        Page<StudentGroup> groupPage = groupRepository.findBySearchQuery(searchPattern, pageable);
        List<GroupListViewDto> content = groupPage.getContent().stream().map(this::toListViewDto)
                .collect(Collectors.toList());
        return PageResponse.<GroupListViewDto>builder().content(content).page(page).size(size)
                .totalElements(groupPage.getTotalElements()).totalPages(groupPage.getTotalPages()).build();
    }

    private GroupResponseDto toDto(StudentGroup g) {
        return GroupResponseDto.builder().id(g.getId()).name(g.getName()).courseYear(g.getCourseYear())
                .studentCount(g.getStudentCount()).createdAt(g.getCreatedAt()).updatedAt(g.getUpdatedAt()).build();
    }

    private GroupListViewDto toListViewDto(StudentGroup g) {
        return GroupListViewDto.builder().name(g.getName()).courseYear(g.getCourseYear())
                .studentCount(g.getStudentCount()).build();
    }
}
