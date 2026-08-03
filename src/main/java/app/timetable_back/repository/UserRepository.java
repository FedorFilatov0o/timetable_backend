package app.timetable_back.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.timetable_back.entity.User;
import app.timetable_back.entity.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    @Query("SELECT u FROM User u WHERE (:search IS NULL OR " + "LOWER(u.firstName) LIKE :search OR "
            + "LOWER(u.lastName) LIKE :search OR " + "LOWER(u.email) LIKE :search OR "
            + "LOWER(COALESCE(u.phone, '')) LIKE :search)")
    Page<User> findBySearchQuery(@Param("search") String search, Pageable pageable);
}
