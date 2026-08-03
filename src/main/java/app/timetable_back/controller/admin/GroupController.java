package app.timetable_back.controller.admin;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import app.timetable_back.dto.GroupDto;
import app.timetable_back.dto.GroupListViewDto;
import app.timetable_back.dto.GroupResponseDto;
import app.timetable_back.dto.PageResponse;
import app.timetable_back.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Group Management", description = "API для управления группами")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create-group")
    @PreAuthorize("hasRole('ADMIN')") // ТОЛЬКО Admin может создавать
    @Operation(summary = "Create new group", description = "Создание новой группы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Group created successfully", content = @Content(schema = @Schema(implementation = GroupResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<GroupResponseDto> createGroup(@Valid @RequestBody GroupDto groupDto) {
        GroupResponseDto createdGroup = groupService.createGroupDto(groupDto);
        return ResponseEntity.created(URI.create("/admin/groups/" + createdGroup.getId())).body(createdGroup);
    }

    @PutMapping("/update-group/{groupId}")
    @PreAuthorize("hasRole('ADMIN')") // ТОЛЬКО Admin может обновлять
    @Operation(summary = "Update group", description = "Обновление данных группы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully", content = @Content(schema = @Schema(implementation = GroupResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Group not found")})
    public ResponseEntity<GroupResponseDto> updateGroup(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @Valid @RequestBody GroupDto groupDto) {
        GroupResponseDto updatedGroup = groupService.updateGroupDto(groupId, groupDto);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/delete-group/{groupId}")
    @PreAuthorize("hasRole('ADMIN')") // ТОЛЬКО Admin может удалять
    @Operation(summary = "Delete group", description = "Удаление группы")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")})
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    // GET методы наследуют hasAnyRole('ADMIN', 'DISPATCHER') с уровня класса
    @GetMapping("/group/{groupId}")
    @Operation(summary = "Get group by ID", description = "Получение данных группы по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group found", content = @Content(schema = @Schema(implementation = GroupResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Group not found")})
    public ResponseEntity<GroupResponseDto> getGroup(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId) {
        GroupResponseDto group = groupService.findByIdDto(groupId);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/groups")
    @Operation(summary = "Get all groups", description = "Получение списка всех групп")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups found", content = @Content(schema = @Schema(implementation = GroupResponseDto.class)))})
    public ResponseEntity<List<GroupResponseDto>> getAllGroups() {
        List<GroupResponseDto> groups = groupService.findAllDto();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/groups/list")
    @Operation(summary = "Get paginated groups list with search", description = "Получение пагинированного списка групп с поиском")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups found", content = @Content(schema = @Schema(implementation = PageResponse.class)))})
    public ResponseEntity<PageResponse<GroupListViewDto>> getGroupsList(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поисковый запрос (частичное совпадение)") @RequestParam(required = false) String search) {
        PageResponse<GroupListViewDto> response = groupService.findAllListView(page, size, search);
        return ResponseEntity.ok(response);
    }
}
