package ru.rbpo.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Пользователь системы. Таблица users. */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 50)
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(min = 2, max = 50)
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    @Column
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public User() {
    }

    public User(String firstName, String lastName, String email, String username, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
