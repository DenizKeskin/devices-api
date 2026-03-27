package com.devices.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "devices")
@SQLDelete(sql = "UPDATE devices SET delete_time = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("delete_time IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceState state;

    @CreationTimestamp
    @Column(name = "creation_time", nullable = false, updatable = false)
    private OffsetDateTime creationTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private OffsetDateTime updateTime;

    @Column(name = "delete_time")
    private OffsetDateTime deleteTime;

    @Version
    private Long version;
}
