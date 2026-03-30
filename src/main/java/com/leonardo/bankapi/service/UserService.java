package com.leonardo.bankapi.service;

import com.leonardo.bankapi.entity.User;
import com.leonardo.bankapi.repository.UserRepository;
import com.leonardo.bankapi.dto.UserRequest;
import com.leonardo.bankapi.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // CREATE
    public UserResponse createUser(UserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail()
        );
    }

    // READ
    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                ))
                .collect(Collectors.toList());
    }

    // UPDATE
    public UserResponse updateUser(Long id, UserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        existingUser.setName(request.getName());
        existingUser.setEmail(request.getEmail());

        User updated = userRepository.save(existingUser);

        return new UserResponse(
                updated.getId(),
                updated.getName(),
                updated.getEmail()
        );
    }

    // DELETE
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}