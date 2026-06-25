package uz.java.kpisystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsFilter;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsRequest;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsResponse;
import uz.java.kpisystem.service.ICheckListItemsService;

import java.util.List;

@RestController
@RequestMapping("/checklist-items")
@RequiredArgsConstructor
public class CheckListItemsController {

    private final ICheckListItemsService service;

    @GetMapping("/all")
    public  ResponseEntity<?>  getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long checkListId) {ApiResponse<List<CheckListItemsResponse>> data = service.getAll(new CheckListItemsFilter(page, limit, sortBy, name, checkListId));
      return   ResponseEntity.ok(data);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @PostMapping("/create")
    public ResponseEntity<Long> create(@RequestBody @Valid CheckListItemsRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<CheckListItemsResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid CheckListItemsRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @GetMapping("/{id}")
    public ResponseEntity<CheckListItemsResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }
}
