package com.senol.service;

import com.senol.entity.AssociationInfo;
import com.senol.entity.User;
import com.senol.repository.AssociationInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssociationInfoService {
    
    @Autowired
    private AssociationInfoRepository associationInfoRepository;
    
    public List<AssociationInfo> getAllInfo() {
        return associationInfoRepository.findByIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc();
    }
    
    public List<AssociationInfo> getInfoByType(AssociationInfo.InfoType infoType) {
        return associationInfoRepository.findByInfoTypeAndIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc(infoType);
    }
    
    public Optional<AssociationInfo> getInfoById(Long id) {
        return associationInfoRepository.findById(id);
    }
    
    public AssociationInfo createInfo(AssociationInfo info, User updatedBy) {
        info.setUpdatedBy(updatedBy);
        info.setIsActive(true);
        return associationInfoRepository.save(info);
    }
    
    public AssociationInfo updateInfo(AssociationInfo info) {
        return associationInfoRepository.save(info);
    }
    
    public void deleteInfo(Long id) {
        Optional<AssociationInfo> info = associationInfoRepository.findById(id);
        if (info.isPresent()) {
            AssociationInfo associationInfo = info.get();
            associationInfo.setIsActive(false);
            associationInfoRepository.save(associationInfo);
        }
    }
}
