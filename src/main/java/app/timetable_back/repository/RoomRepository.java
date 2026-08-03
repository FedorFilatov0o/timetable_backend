package app.timetable_back.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.timetable_back.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r WHERE (:search IS NULL OR " + "LOWER(r.roomNumber) LIKE :search OR "
            + "LOWER(COALESCE(r.building, '')) LIKE :search)")
    Page<Room> findBySearchQuery(@Param("search") String search, Pageable pageable);
}
