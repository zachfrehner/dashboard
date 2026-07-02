package com.burnmetrix.dashboard.settings;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<SettingEntity, Long> {

    Optional<SettingEntity> findByKey(String key);
}

