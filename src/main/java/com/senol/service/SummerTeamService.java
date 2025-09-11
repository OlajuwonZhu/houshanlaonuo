package com.senol.service;

import com.senol.entity.SummerTeam;
import com.senol.entity.User;
import com.senol.repository.SummerTeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SummerTeamService {
    
    @Autowired
    private SummerTeamRepository summerTeamRepository;
    
    public Page<SummerTeam> getTeams(int page, int size, Integer year) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (year != null) {
            return summerTeamRepository.findByYearAndIsActiveTrueOrderByCreatedAtDesc(year, pageable);
        } else {
            return summerTeamRepository.findByIsActiveTrueOrderByYearDesc(pageable);
        }
    }
    
    public Optional<SummerTeam> getTeamById(Long id) {
        return summerTeamRepository.findById(id);
    }
    
    public SummerTeam createTeam(SummerTeam team, User createdBy) {
        team.setCreatedBy(createdBy);
        team.setIsActive(true);
        return summerTeamRepository.save(team);
    }
    
    public SummerTeam updateTeam(SummerTeam team) {
        return summerTeamRepository.save(team);
    }
    
    public void deleteTeam(Long id) {
        Optional<SummerTeam> team = summerTeamRepository.findById(id);
        if (team.isPresent()) {
            SummerTeam summerTeam = team.get();
            summerTeam.setIsActive(false);
            summerTeamRepository.save(summerTeam);
        }
    }
    
    public Long getActiveTeamCount() {
        return summerTeamRepository.countActiveTeams();
    }
    
    public List<Integer> getTeamYears() {
        return summerTeamRepository.findDistinctYears();
    }
}
