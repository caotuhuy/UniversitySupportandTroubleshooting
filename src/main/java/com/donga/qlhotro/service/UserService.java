package com.donga.qlhotro.service;

import com.donga.qlhotro.dto.UserRequestDTO;
import com.donga.qlhotro.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO getUserByUsername(String username);
    UserResponseDTO createUser(UserRequestDTO request);
    UserResponseDTO updateUser(Long id, UserRequestDTO request);
    void deleteUser(Long id);
    UserResponseDTO lockUser(Long id);
    UserResponseDTO unlockUser(Long id);
}
