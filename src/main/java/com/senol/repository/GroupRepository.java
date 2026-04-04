package com.senol.repository;

import com.senol.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Page<Group> findByNameContainingIgnoreCaseAndCityContainingIgnoreCase(String name, String city, Pageable pageable);
    Page<Group> findByIdIn(java.util.List<Long> ids, Pageable pageable);
    boolean existsBySlug(String slug);
}
