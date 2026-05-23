package com.pharmacy.patient.entity;
import com.pharmacy.shared.entity.Prescription;
import jakarta.persistence.*;

@Entity
@Table(name = "patients")

public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullname;

    private String email;

    private String password;

    private String phone;

    // getters setters
}