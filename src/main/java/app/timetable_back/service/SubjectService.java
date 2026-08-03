package app.timetable_back.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.timetable_back.dto.PageResponse;
import app.timetable_back.dto.SubjectDto;
import app.timetable_back.dto.SubjectListViewDto;
import app.timetable_back.dto.SubjectResponseDto;
import app.timetable_back.entity.Subject;
import app.timetable_back.exception.EntityInUseException;
import app.timetable_back.repository.LessonRepository;
import app.timetable_back.repository.SubjectRepository;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;

    public SubjectService(SubjectRepository subjectRepository, LessonRepository lessonRepository) {
        this.subjectRepository = subjectRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional(readOnly = true)
    public Subject findById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject with id '" + id + "' not found"));
    }

    @Transactional
    public Subject createSubject(SubjectDto dto) {
        return subjectRepository.save(Subject.builder().name(dto.getName()).code(dto.getCode())
                .faculty(dto.getFaculty()).description(dto.getDescription()).build());
    }

    @Transactional
    public Subject updateSubject(Long id, SubjectDto dto) {
        Subject existing = findById(id);
        existing.setName(dto.getName());
        existing.setCode(dto.getCode());
        existing.setFaculty(dto.getFaculty());
        existing.setDescription(dto.getDescription());
        return subjectRepository.save(existing);
    }

    @Transactional
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new IllegalArgumentException("Subject with id '" + id + "' not found");
        }

        Pageable firstResult = PageRequest.of(0, 1);

        lessonRepository.findFirstBySubjectId(id, firstResult).stream().findFirst().ifPresent(lesson -> {
            String date = lesson.getStartAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            throw new EntityInUseException("Нельзя удалять предмет. Используется в уроке " + date);
        });

        subjectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    @Transactional
    public SubjectResponseDto createSubjectDto(SubjectDto dto) {
        return toDto(createSubject(dto));
    }

    @Transactional
    public SubjectResponseDto updateSubjectDto(Long id, SubjectDto dto) {
        return toDto(updateSubject(id, dto));
    }

    @Transactional(readOnly = true)
    public SubjectResponseDto findByIdDto(Long id) {
        return toDto(findById(id));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponseDto> findAllDto() {
        return subjectRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<SubjectListViewDto> findAllListView(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        String searchPattern = (search != null && !search.trim().isEmpty())
                ? "%" + search.trim().toLowerCase() + "%"
                : null;
        Page<Subject> subjectPage = subjectRepository.findBySearchQuery(searchPattern, pageable);
        List<SubjectListViewDto> content = subjectPage.getContent().stream().map(this::toListViewDto)
                .collect(Collectors.toList());
        return PageResponse.<SubjectListViewDto>builder().content(content).page(page).size(size)
                .totalElements(subjectPage.getTotalElements()).totalPages(subjectPage.getTotalPages()).build();
    }

    private SubjectResponseDto toDto(Subject s) {
        return SubjectResponseDto.builder().id(s.getId()).name(s.getName()).code(s.getCode()).faculty(s.getFaculty())
                .description(s.getDescription()).createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt()).build();
    }

    private SubjectListViewDto toListViewDto(Subject s) {
        return SubjectListViewDto.builder().name(s.getName()).code(s.getCode()).faculty(s.getFaculty())
                .description(s.getDescription()).build();
    }
}
