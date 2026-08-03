package app.timetable_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {

    private Long id;

    @NotBlank(message = "Subject name is required")
    @Size(max = 255, message = "Subject name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Subject code is required")
    @Size(max = 20, message = "Subject code must be less than 20 characters")
    private String code;

    @Size(max = 100, message = "Faculty must be less than 100 characters")
    private String faculty;

    private String description;
}
