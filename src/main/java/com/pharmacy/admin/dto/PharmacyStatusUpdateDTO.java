package com.pharmacy.admin.dto;

import jakarta.validation.constraints.NotNull;

public class PharmacyStatusUpdateDTO {

    @NotNull(message = "active field is required")
    private Boolean active;

    public PharmacyStatusUpdateDTO() {}

    public PharmacyStatusUpdateDTO(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
