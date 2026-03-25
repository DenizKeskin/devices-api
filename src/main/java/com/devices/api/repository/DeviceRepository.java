package com.devices.api.repository;

import com.devices.api.model.Device;
import com.devices.api.model.DeviceState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Page<Device> findByBrand(String brand, Pageable pageable);

    Page<Device> findByState(DeviceState state, Pageable pageable);
}
