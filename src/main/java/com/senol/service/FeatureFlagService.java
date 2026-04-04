package com.senol.service;

import com.senol.entity.FeatureFlag;
import com.senol.repository.FeatureFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureFlagService {

    @Autowired
    private FeatureFlagRepository featureFlagRepository;

    public boolean isEnabled(String key) {
        return featureFlagRepository.findByFlagKey(key)
                .map(FeatureFlag::getEnabled)
                .orElse(true); // default enabled when not explicitly set
    }

    public FeatureFlag setFlag(String key, Boolean enabled, String description) {
        FeatureFlag flag = featureFlagRepository.findByFlagKey(key).orElseGet(FeatureFlag::new);
        flag.setFlagKey(key);
        if (enabled != null) {
            flag.setEnabled(enabled);
        }
        if (description != null) {
            flag.setDescription(description);
        }
        return featureFlagRepository.save(flag);
    }

    public List<FeatureFlag> listAll() {
        return featureFlagRepository.findAll();
    }
}
