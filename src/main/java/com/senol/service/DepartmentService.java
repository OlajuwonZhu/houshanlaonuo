package com.senol.service;

import com.senol.entity.Department;
import com.senol.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    public List<Department> getAllDepartments() {
        return departmentRepository.findAllByOrderByDisplayOrder();
    }
    
    public List<Department> getActiveDepartments() {
        return departmentRepository.findByIsActiveTrueOrderByDisplayOrder();
    }
    
    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }
    
    public Optional<Department> getDepartmentByCode(String departmentCode) {
        return departmentRepository.findByDepartmentCode(departmentCode);
    }
    
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }
    
    public Department updateDepartment(Long id, Department department) {
        Optional<Department> existing = departmentRepository.findById(id);
        if (existing.isPresent()) {
            Department existingDept = existing.get();
            existingDept.setDepartmentName(department.getDepartmentName());
            existingDept.setDepartmentCode(department.getDepartmentCode());
            existingDept.setDescription(department.getDescription());
            existingDept.setResponsibilities(department.getResponsibilities());
            existingDept.setCurrentHead(department.getCurrentHead());
            existingDept.setMembers(department.getMembers());
            existingDept.setContactInfo(department.getContactInfo());
            existingDept.setAchievements(department.getAchievements());
            existingDept.setImageUrls(department.getImageUrls());
            existingDept.setDisplayOrder(department.getDisplayOrder());
            existingDept.setIsActive(department.getIsActive());
            existingDept.setUpdatedBy(department.getUpdatedBy());
            return departmentRepository.save(existingDept);
        }
        throw new RuntimeException("部门不存在");
    }
    
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
    
    public Long getActiveDepartmentCount() {
        return departmentRepository.countActiveDepartments();
    }
}
