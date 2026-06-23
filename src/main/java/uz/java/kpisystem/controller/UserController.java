package uz.java.kpisystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.java.kpisystem.dto.user.UserInfo;
import uz.java.kpisystem.dto.user.UserRequest;
import uz.java.kpisystem.filter.UserFilter;
import uz.java.kpisystem.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
//    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ORGANIZATION_ADMIN')")
    public ResponseEntity<Long>createUser(@RequestBody @Valid UserRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ORGANIZATION_ADMIN')")
    public ResponseEntity<?> updateUser(@RequestBody UserRequest request, @PathVariable Long id) {
       return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ORGANIZATION_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(userService.delete(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ORGANIZATION_ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ORGANIZATION_ADMIN')")
    public ResponseEntity<List<UserInfo>> getUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long roleId
    ) {

        UserFilter filter = new UserFilter(
                page,
                limit,
                sortBy,
                firstName,
                lastName,
                username,
                roleId
        );

        return ResponseEntity.ok(
                userService.getUsers(filter)
        );
    }

}
