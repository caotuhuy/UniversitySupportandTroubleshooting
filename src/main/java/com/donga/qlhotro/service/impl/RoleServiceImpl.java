package com.donga.qlhotro.service.impl;

import com.donga.qlhotro.dto.RoleResponseDTO;
import com.donga.qlhotro.entity.Role;
import com.donga.qlhotro.enums.RoleName;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.RoleRepository;
import com.donga.qlhotro.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò", "id", id));
        return mapToResponseDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleByName(RoleName name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò", "name", name));
        return mapToResponseDTO(role);
    }

    private RoleResponseDTO mapToResponseDTO(Role role) {
        return new RoleResponseDTO(role.getId(), role.getName(), role.getDescription());
    }
}
