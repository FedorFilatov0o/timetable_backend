package app.timetable_back.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {

    private Long id;

    @NotBlank(message = "Group name is required")
    @Size(max = 255, message = "Group name must be less than 255 characters")
    private String name;

    @NotNull(message = "Course year is required")
    @Min(value = 1, message = "Course year must be between 1 and 6")
    @Max(value = 6, message = "Course year must be between 1 and 6")
    private Integer courseYear;

    @NotNull(message = "Student count is required")
    @Min(value = 0, message = "Student count must be non-negative")
    private Integer studentCount;
}
