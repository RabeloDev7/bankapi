package com.leonardo.bankapi.dto;

public class UserResponse {
    private Long Id;
    private String name;
    private String email;

    public UserResponse(Long id, String name, String email) {
        this.Id = id;
        this.name = name;
        this.email = email;
    }

    public long getId() {
        return Id;
    }
    public void setId(long id) {
        this.Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
