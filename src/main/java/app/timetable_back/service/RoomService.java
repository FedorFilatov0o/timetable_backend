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
import app.timetable_back.dto.RoomDto;
import app.timetable_back.dto.RoomListViewDto;
import app.timetable_back.dto.RoomResponseDto;
import app.timetable_back.entity.Room;
import app.timetable_back.exception.EntityInUseException;
import app.timetable_back.repository.LessonRepository;
import app.timetable_back.repository.RoomRepository;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final LessonRepository lessonRepository;

    public RoomService(RoomRepository roomRepository, LessonRepository lessonRepository) {
        this.roomRepository = roomRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional(readOnly = true)
    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room with id '" + id + "' not found"));
    }

    @Transactional
    public Room createRoom(RoomDto dto) {
        return roomRepository.save(Room.builder().roomNumber(dto.getRoomNumber()).building(dto.getBuilding())
                .capacity(dto.getCapacity()).build());
    }

    @Transactional
    public Room updateRoom(Long id, RoomDto dto) {
        Room existing = findById(id);
        existing.setRoomNumber(dto.getRoomNumber());
        existing.setBuilding(dto.getBuilding());
        existing.setCapacity(dto.getCapacity());
        return roomRepository.save(existing);
    }

    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new IllegalArgumentException("Room with id '" + id + "' not found");
        }
        Pageable firstResult = PageRequest.of(0, 1);
        lessonRepository.findFirstByRoomId(id, firstResult).stream().findFirst().ifPresent(lesson -> {
            String date = lesson.getStartAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            throw new EntityInUseException("Нельзя удалять аудиторию. Используется в уроке " + date);
        });
        roomRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Transactional
    public RoomResponseDto createRoomDto(RoomDto dto) {
        return toDto(createRoom(dto));
    }

    @Transactional
    public RoomResponseDto updateRoomDto(Long id, RoomDto dto) {
        return toDto(updateRoom(id, dto));
    }

    @Transactional(readOnly = true)
    public RoomResponseDto findByIdDto(Long id) {
        return toDto(findById(id));
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDto> findAllDto() {
        return roomRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<RoomListViewDto> findAllListView(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        String searchPattern = (search != null && !search.trim().isEmpty())
                ? "%" + search.trim().toLowerCase() + "%"
                : null;
        Page<Room> roomPage = roomRepository.findBySearchQuery(searchPattern, pageable);
        List<RoomListViewDto> content = roomPage.getContent().stream().map(this::toListViewDto)
                .collect(Collectors.toList());
        return PageResponse.<RoomListViewDto>builder().content(content).page(page).size(size)
                .totalElements(roomPage.getTotalElements()).totalPages(roomPage.getTotalPages()).build();
    }

    private RoomResponseDto toDto(Room r) {
        return RoomResponseDto.builder().id(r.getId()).roomNumber(r.getRoomNumber()).building(r.getBuilding())
                .capacity(r.getCapacity()).createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt()).build();
    }

    private RoomListViewDto toListViewDto(Room r) {
        return RoomListViewDto.builder().roomNumber(r.getRoomNumber()).building(r.getBuilding())
                .capacity(r.getCapacity()).build();
    }
}
