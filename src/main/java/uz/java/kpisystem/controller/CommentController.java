package uz.java.kpisystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.comment.CommentFilter;
import uz.java.kpisystem.dto.comment.CommentRequest;
import uz.java.kpisystem.dto.comment.CommentResponse;
import uz.java.kpisystem.service.ICommentService;
import uz.java.kpisystem.util.ApiVersion;

import java.util.List;

@RestController
@RequestMapping(ApiVersion.API_VERSION + "/comments")
public class CommentController {

    private final ICommentService service;

    public CommentController(ICommentService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER', 'PROJECT_MANAGER')") // @PreAuthorize default da "ROLE_"
    @GetMapping("/all")
    public ApiResponse<List<CommentResponse>> getAll(@RequestParam Long taskId,
                                                     @RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer limit,
                                                     @RequestParam(required = false) String sortBy,
                                                     @RequestParam(required = false) String text) {
        return service.getAll(new CommentFilter(page, limit, sortBy, text, taskId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER', 'PROJECT_MANAGER')")
    public ApiResponse<CommentResponse> getOne(@PathVariable Long id) {
        return service.getOne(id);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ApiResponse<Long> create(@RequestBody @Valid CommentRequest request) {
        return new ApiResponse<>(service.create(request));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ApiResponse<CommentResponse> update(@PathVariable Long id, @RequestBody CommentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }
}
