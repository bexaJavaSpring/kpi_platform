package uz.java.kpisystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.task.TaskFilter;
import uz.java.kpisystem.dto.task.TaskRequest;
import uz.java.kpisystem.dto.task.TaskResponse;
import uz.java.kpisystem.service.ITaskService;
import uz.java.kpisystem.util.ApiVersion;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final ITaskService service;

    public TaskController(ITaskService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER', 'PROJECT_MANAGER')") // @PreAuthorize default da "ROLE_"
    @GetMapping("/all")
    public ApiResponse<List<TaskResponse>> getAll(@RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer limit,
                                                  @RequestParam(required = false) String sortBy,
                                                  @RequestParam(required = false) String name) {
        return service.getAll(new TaskFilter(page, limit, sortBy, name));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER', 'PROJECT_MANAGER')")
    public ApiResponse<TaskResponse> getOne(@PathVariable Long id) {
        return service.getOne(id);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ApiResponse<Long> create(@RequestBody @Valid TaskRequest request) {
        return new ApiResponse<>(service.create(request));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ApiResponse<TaskResponse> update(@PathVariable Long id, @RequestBody TaskRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ApiResponse<Long> copy(@PathVariable Long id) {
        return new ApiResponse<>( service.copy(id));
    }

    @PostMapping("/{taskId}/move")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ApiResponse<Boolean> moveToAnotherProject(
            @PathVariable Long taskId,
            @RequestParam Long projectId
    ) {
        return new ApiResponse<>(service.moveToAnotherProject(taskId,projectId));
    }

}
