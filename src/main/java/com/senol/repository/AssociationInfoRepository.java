package com.senol.repository;

import com.senol.entity.AssociationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssociationInfoRepository extends JpaRepository<AssociationInfo, Long> {
    
    List<AssociationInfo> findByIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc();
    
    List<AssociationInfo> findByInfoTypeAndIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc(AssociationInfo.InfoType infoType);
}
