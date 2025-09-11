package com.senol.service;

import com.senol.entity.FreshmenTeam;
import com.senol.entity.User;
import com.senol.repository.FreshmenTeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FreshmenTeamService {
    
    @Autowired
    private FreshmenTeamRepository freshmenTeamRepository;
    
    public Page<FreshmenTeam> getTeams(int page, int size, Integer year) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (year != null) {
            return freshmenTeamRepository.findByYearAndIsActiveTrueOrderByCreatedAtDesc(year, pageable);
        } else {
            return freshmenTeamRepository.findByIsActiveTrueOrderByYearDesc(pageable);
        }
    }
    
    public Optional<FreshmenTeam> getTeamById(Long id) {
        return freshmenTeamRepository.findById(id);
    }
    
    public FreshmenTeam createTeam(FreshmenTeam team, User createdBy) {
        team.setCreatedBy(createdBy);
        team.setIsActive(true);
        return freshmenTeamRepository.save(team);
    }
    
    public FreshmenTeam updateTeam(FreshmenTeam team) {
        return freshmenTeamRepository.save(team);
    }
    
    public void deleteTeam(Long id) {
        Optional<FreshmenTeam> team = freshmenTeamRepository.findById(id);
        if (team.isPresent()) {
            FreshmenTeam freshmenTeam = team.get();
            freshmenTeam.setIsActive(false);
            freshmenTeamRepository.save(freshmenTeam);
        }
    }
    
    public Long getActiveTeamCount() {
        return freshmenTeamRepository.countActiveTeams();
    }
    
    public List<Integer> getTeamYears() {
        return freshmenTeamRepository.findDistinctYears();
    }
}
