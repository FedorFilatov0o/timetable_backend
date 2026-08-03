package app.timetable_back.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.timetable_back.dto.DayCommentDto;
import app.timetable_back.dto.DayCommentListViewDto;
import app.timetable_back.dto.DayCommentResponseDto;
import app.timetable_back.dto.PageResponse;
import app.timetable_back.entity.DayComment;
import app.timetable_back.entity.User;
import app.timetable_back.repository.DayCommentRepository;

@Service
public class DayCommentService {

    private final DayCommentRepository dayCommentRepository;
    private final UserService userService;

    public DayCommentService(DayCommentRepository dayCommentRepository, UserService userService) {
        this.dayCommentRepository = dayCommentRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public DayComment findById(Long id) {
        return dayCommentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Day comment with id '" + id + "' not found"));
    }

    @Transactional
    public DayComment createDayComment(DayCommentDto dayCommentDto) {
        User user = userService.findById(dayCommentDto.getUserId());

        DayComment dayComment = DayComment.builder().date(dayCommentDto.getDate()).user(user)
                .commentText(dayCommentDto.getCommentText()).build();

        return dayCommentRepository.save(dayComment);
    }

    @Transactional
    public DayComment updateDayComment(Long id, DayCommentDto dayCommentDto) {
        DayComment existingComment = findById(id);

        User user = userService.findById(dayCommentDto.getUserId());

        existingComment.setDate(dayCommentDto.getDate());
        existingComment.setUser(user);
        existingComment.setCommentText(dayCommentDto.getCommentText());

        return dayCommentRepository.save(existingComment);
    }

    @Transactional
    public void deleteDayComment(Long id) {
        if (!dayCommentRepository.existsById(id)) {
            throw new IllegalArgumentException("Day comment with id '" + id + "' not found");
        }
        dayCommentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DayComment> findAll() {
        return dayCommentRepository.findAll();
    }

    /** Create day comment and return DTO */
    @Transactional
    public DayCommentResponseDto createDayCommentDto(DayCommentDto dayCommentDto) {
        DayComment dayComment = createDayComment(dayCommentDto);
        return toDto(dayComment);
    }

    /** Update day comment and return DTO */
    @Transactional
    public DayCommentResponseDto updateDayCommentDto(Long id, DayCommentDto dayCommentDto) {
        DayComment dayComment = updateDayComment(id, dayCommentDto);
        return toDto(dayComment);
    }

    /** Get day comment by ID as DTO */
    @Transactional(readOnly = true)
    public DayCommentResponseDto findByIdDto(Long id) {
        DayComment dayComment = findById(id);
        return toDto(dayComment);
    }

    /** Get all day comments as DTOs */
    @Transactional(readOnly = true)
    public List<DayCommentResponseDto> findAllDto() {
        return dayCommentRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Get paginated day comments as ListView DTOs (без id, createdAt) */
    @Transactional(readOnly = true)
    public PageResponse<DayCommentListViewDto> findAllListView(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DayComment> commentPage = dayCommentRepository.findAll(pageable);

        List<DayCommentListViewDto> content = commentPage.getContent().stream().map(this::toListViewDto)
                .collect(Collectors.toList());

        return PageResponse.<DayCommentListViewDto>builder().content(content).page(page).size(size)
                .totalElements(commentPage.getTotalElements()).totalPages(commentPage.getTotalPages()).build();
    }

    /** Map DayComment entity to DayCommentResponseDto */
    private DayCommentResponseDto toDto(DayComment dayComment) {
        DayCommentResponseDto.UserInfo userInfo = null;
        if (dayComment.getUser() != null) {
            User user = dayComment.getUser();
            userInfo = DayCommentResponseDto.UserInfo.builder().id(user.getId()).firstName(user.getFirstName())
                    .lastName(user.getLastName()).email(user.getEmail()).build();
        }

        return DayCommentResponseDto.builder().id(dayComment.getId()).date(dayComment.getDate()).user(userInfo)
                .commentText(dayComment.getCommentText()).createdAt(dayComment.getCreatedAt()).build();
    }

    /** Map DayComment entity to DayCommentListViewDto (без id, createdAt) */
    private DayCommentListViewDto toListViewDto(DayComment dayComment) {
        DayCommentListViewDto.UserInfo userInfo = null;
        if (dayComment.getUser() != null) {
            User user = dayComment.getUser();
            userInfo = DayCommentListViewDto.UserInfo.builder().firstName(user.getFirstName())
                    .lastName(user.getLastName()).email(user.getEmail()).build();
        }

        return DayCommentListViewDto.builder().date(dayComment.getDate()).user(userInfo)
                .commentText(dayComment.getCommentText()).build();
    }

    /**
     * Get day comments by specific date as DTOs Returns empty list if no comments
     * found (not null)
     */
    @Transactional(readOnly = true)
    public List<DayCommentResponseDto> findByDateDto(LocalDate date) {
        List<DayComment> comments = dayCommentRepository.findByDate(date);
        return comments.stream().map(this::toDto).collect(Collectors.toList());
    }
}
