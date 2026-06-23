package uz.java.kpisystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.organization.OrganizationFilter;
import uz.java.kpisystem.dto.organization.OrganizationInfo;
import uz.java.kpisystem.dto.organization.OrganizationRequest;
import uz.java.kpisystem.service.IOrganizationService;

import java.util.List;

@RestController // bean qilib beradi
@RequestMapping("/organizations") // url boshi(domain dan kn yoziladigan url)
@RequiredArgsConstructor // DI(Dependency Injection ni 4-usuli
public class OrganizationController {

    private final IOrganizationService service;

    //    @Autowired  // field-based DI

    // setter-based
//    public void setService(IOrganizationService service) {
//        this.service = service;
//    }

    //    public OrganizationController(IOrganizationService service) {
//        this.service = service;
//    }  // constructor-based DI

    //    ResponseEntity --->>> statusCode, success, body
    @GetMapping // get method lar uchun
    public ResponseEntity<?> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                         @RequestParam(required = false, defaultValue = "10") Integer limit,
                                                         @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                                                         @RequestParam(required = false) String name) {
        ApiResponse<List<OrganizationInfo>> all = service.getAll(new OrganizationFilter(page, limit, sortBy, name));
        return ResponseEntity.ok(all);
//        @RequestParam -->> bu Front yuboradigan single field uchun ishlatiladi(yani Frontdan kelgan zaprosni hanldle qiladi)
    }

    //HttpStatus
    @PostMapping("/create")  // RequestBody bu body da keladigan zaproslarni ushlaydi
    public ResponseEntity<Long> create(@RequestBody @Valid OrganizationRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<OrganizationInfo> update(@PathVariable Long id, @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationInfo> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id){
        return ResponseEntity.ok(service.delete(id));
    }

}
