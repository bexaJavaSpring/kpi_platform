package uz.java.kpisystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.java.kpisystem.dto.group.GroupFilter;
import uz.java.kpisystem.dto.group.GroupRequest;
import uz.java.kpisystem.dto.group.GroupResponse;
import uz.java.kpisystem.service.GroupService;

import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService service;

    public GroupController(GroupService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')") // @PreAuthorize default da "ROLE_"
    @GetMapping("/all")
    public ResponseEntity<?> getAll(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit,
                                    @RequestParam(required = false) String sortBy,
                                    @RequestParam(required = false) String name, @RequestParam(required = false) Integer taskCount) {
        List<GroupResponse> groups = this.service.getAll(new GroupFilter(page, limit, sortBy, name, taskCount));
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody GroupRequest body) {
        return ResponseEntity.ok(service.create(body));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Long> update(@PathVariable Long id, @RequestBody GroupRequest body) {
        return ResponseEntity.ok(service.update(id, body));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }
}
