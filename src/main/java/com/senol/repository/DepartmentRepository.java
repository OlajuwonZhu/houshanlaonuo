package com.senol.repository;

import com.senol.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByDepartmentCode(String departmentCode);
    
    List<Department> findByIsActiveTrueOrderByDisplayOrder();
    
    List<Department> findAllByOrderByDisplayOrder();
    
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.createdBy WHERE d.isActive = true ORDER BY d.displayOrder")
    List<Department> findByIsActiveTrueOrderByDisplayOrderWithCreatedBy();
    
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.createdBy ORDER BY d.displayOrder")
    List<Department> findAllByOrderByDisplayOrderWithCreatedBy();
    
    @Query("SELECT COUNT(d) FROM Department d WHERE d.isActive = true")
    Long countActiveDepartments();
    
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.createdBy WHERE d.isActive = true AND d.year = :year ORDER BY d.displayOrder")
    List<Department> findByIsActiveTrueAndYearOrderByDisplayOrderWithCreatedBy(Integer year);
}
