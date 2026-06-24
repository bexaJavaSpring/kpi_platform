package uz.java.kpisystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.checkList.CheckListFilter;
import uz.java.kpisystem.dto.checkList.CheckListRequest;
import uz.java.kpisystem.dto.checkList.CheckListResponse;
import uz.java.kpisystem.dto.group.GroupFilter;
import uz.java.kpisystem.dto.group.GroupResponse;
import uz.java.kpisystem.service.ICheckListService;

import java.util.List;

@RestController
@RequestMapping("/checklists")
@RequiredArgsConstructor
public class CheckListController {
    private final ICheckListService service;


    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @GetMapping("/all")
    public ResponseEntity<?> getAll(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit,
                                    @RequestParam(required = false) String sortBy,@RequestParam(required = false) String name) {

        ApiResponse<List<CheckListResponse>> data = this.service.getAll(new CheckListFilter(page, limit, sortBy,name));
        return ResponseEntity.ok(data);
    };


    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @PostMapping("/create")
    public ResponseEntity<Long> create(@RequestBody @Valid CheckListRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<CheckListResponse> update(@PathVariable Long id, @RequestBody CheckListRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @GetMapping("/{id}")
    public ApiResponse<CheckListResponse> getOne(@PathVariable Long id) {
        return new ApiResponse<>(service.getOne(id));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id){
        return ResponseEntity.ok(service.delete(id));
    }
}
