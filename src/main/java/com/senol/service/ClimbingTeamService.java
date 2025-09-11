package com.senol.service;

import com.senol.entity.ClimbingTeam;
import com.senol.repository.ClimbingTeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClimbingTeamService {
    
    @Autowired
    private ClimbingTeamRepository climbingTeamRepository;
    
    public List<ClimbingTeam> getAllClimbingTeams() {
        return climbingTeamRepository.findAllByOrderByYearDesc();
    }
    
    public List<ClimbingTeam> getClimbingTeamsByYear(Integer year) {
        return climbingTeamRepository.findByYear(year);
    }
    
    public Optional<ClimbingTeam> getClimbingTeamById(Long id) {
        return climbingTeamRepository.findById(id);
    }
    
    public List<ClimbingTeam> getClimbingTeamsByYearRange(Integer startYear, Integer endYear) {
        return climbingTeamRepository.findByYearBetweenOrderByYearDesc(startYear, endYear);
    }
    
    public List<Integer> getAvailableYears() {
        return climbingTeamRepository.findDistinctYears();
    }
    
    public ClimbingTeam createClimbingTeam(ClimbingTeam climbingTeam) {
        return climbingTeamRepository.save(climbingTeam);
    }
    
    public ClimbingTeam updateClimbingTeam(Long id, ClimbingTeam climbingTeam) {
        Optional<ClimbingTeam> existing = climbingTeamRepository.findById(id);
        if (existing.isPresent()) {
            ClimbingTeam existingTeam = existing.get();
            existingTeam.setYear(climbingTeam.getYear());
            existingTeam.setTeamLeader(climbingTeam.getTeamLeader());
            existingTeam.setViceLeader(climbingTeam.getViceLeader());
            existingTeam.setMembers(climbingTeam.getMembers());
            existingTeam.setClimbingRoutes(climbingTeam.getClimbingRoutes());
            existingTeam.setAchievements(climbingTeam.getAchievements());
            existingTeam.setTrainingActivities(climbingTeam.getTrainingActivities());
            existingTeam.setCompetitions(climbingTeam.getCompetitions());
            existingTeam.setEquipment(climbingTeam.getEquipment());
            existingTeam.setDescription(climbingTeam.getDescription());
            existingTeam.setImageUrls(climbingTeam.getImageUrls());
            existingTeam.setUpdatedBy(climbingTeam.getUpdatedBy());
            return climbingTeamRepository.save(existingTeam);
        }
        throw new RuntimeException("攀岩队记录不存在");
    }
    
    public void deleteClimbingTeam(Long id) {
        climbingTeamRepository.deleteById(id);
    }
    
    public Long getClimbingTeamCount() {
        return climbingTeamRepository.countAll();
    }
}
