package com.pharmacy.shared.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    @Column(nullable = false)
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Prescription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }

    public Pharmacy getPharmacy() { return pharmacy; }
    public void setPharmacy(Pharmacy pharmacy) { this.pharmacy = pharmacy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
