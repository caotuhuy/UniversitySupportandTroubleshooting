package com.donga.qlhotro.service;

import com.donga.qlhotro.dto.RoleResponseDTO;
import com.donga.qlhotro.enums.RoleName;

import java.util.List;

public interface RoleService {
    List<RoleResponseDTO> getAllRoles();
    RoleResponseDTO getRoleById(Long id);
    RoleResponseDTO getRoleByName(RoleName name);
}
