package com.leonardo.bankapi.entity;

import jakarta.persistence.*;

/**
 * Representa um usuário do sistema bancário
 */
@Entity
@Table(name = "users")
public class User {

    @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Nome do usuário
     */
    private String name;

    /**
     * Email do usuário
     */
    private String email;

    // Construtor padrão
    public User() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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