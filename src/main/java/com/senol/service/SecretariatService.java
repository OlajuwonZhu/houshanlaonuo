package com.senol.service;

import com.senol.entity.Secretariat;
import com.senol.repository.SecretariatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SecretariatService {
    
    @Autowired
    private SecretariatRepository secretariatRepository;
    
    public List<Secretariat> getAllSecretariats() {
        return secretariatRepository.findAllByOrderByYearDesc();
    }
    
    public List<Secretariat> getSecretariatsByYear(Integer year) {
        return secretariatRepository.findByYear(year);
    }
    
    public Optional<Secretariat> getSecretariatById(Long id) {
        return secretariatRepository.findById(id);
    }
    
    public List<Secretariat> getSecretariatsByYearRange(Integer startYear, Integer endYear) {
        return secretariatRepository.findByYearBetweenOrderByYearDesc(startYear, endYear);
    }
    
    public List<Integer> getAvailableYears() {
        return secretariatRepository.findDistinctYears();
    }
    
    public Secretariat createSecretariat(Secretariat secretariat) {
        return secretariatRepository.save(secretariat);
    }
    
    public Secretariat updateSecretariat(Long id, Secretariat secretariat) {
        Optional<Secretariat> existing = secretariatRepository.findById(id);
        if (existing.isPresent()) {
            Secretariat existingSecretariat = existing.get();
            existingSecretariat.setYear(secretariat.getYear());
            existingSecretariat.setSecretaryGeneral(secretariat.getSecretaryGeneral());
            existingSecretariat.setDeputySecretary(secretariat.getDeputySecretary());
            existingSecretariat.setMembers(secretariat.getMembers());
            existingSecretariat.setAchievements(secretariat.getAchievements());
            existingSecretariat.setActivities(secretariat.getActivities());
            existingSecretariat.setDescription(secretariat.getDescription());
            existingSecretariat.setImageUrls(secretariat.getImageUrls());
            existingSecretariat.setUpdatedBy(secretariat.getUpdatedBy());
            return secretariatRepository.save(existingSecretariat);
        }
        throw new RuntimeException("秘书处记录不存在");
    }
    
    public void deleteSecretariat(Long id) {
        secretariatRepository.deleteById(id);
    }
    
    public Long getSecretariatCount() {
        return secretariatRepository.countAll();
    }
}
