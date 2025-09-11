package com.senol.controller;

import com.senol.entity.Department;
import com.senol.service.DepartmentService;
import com.senol.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/department")
@CrossOrigin(origins = "*")
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        try {
            List<Department> departments = departmentService.getActiveDepartments();
            return ResponseEntity.ok(ApiResponse.success(departments));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取部门列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartmentsIncludeInactive() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            return ResponseEntity.ok(ApiResponse.success(departments));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取部门列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> getDepartmentById(@PathVariable Long id) {
        try {
            Optional<Department> department = departmentService.getDepartmentById(id);
            if (department.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(department.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取部门信息失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Department>> getDepartmentByCode(@PathVariable String code) {
        try {
            Optional<Department> department = departmentService.getDepartmentByCode(code);
            if (department.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(department.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取部门信息失败: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Department>> createDepartment(
            @RequestBody Department department, 
            Authentication authentication) {
        try {
            // TODO: 从authentication中获取用户ID并设置createdBy
            department.setCreatedBy(1L); // 临时使用系统用户
            Department created = departmentService.createDepartment(department);
            return ResponseEntity.ok(ApiResponse.success(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("创建部门失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(
            @PathVariable Long id, 
            @RequestBody Department department,
            Authentication authentication) {
        try {
            // TODO: 从authentication中获取用户ID并设置updatedBy
            department.setUpdatedBy(1L); // 临时使用系统用户
            Department updated = departmentService.updateDepartment(id, department);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新部门失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除部门失败: " + e.getMessage()));
        }
    }
}
