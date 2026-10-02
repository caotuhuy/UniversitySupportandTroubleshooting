package com.donga.qlhotro.dto;

import com.donga.qlhotro.enums.RoleName;

public class RoleResponseDTO {

    private Long id;
    private RoleName name;
    private String description;

    public RoleResponseDTO() {
    }

    public RoleResponseDTO(Long id, RoleName name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
