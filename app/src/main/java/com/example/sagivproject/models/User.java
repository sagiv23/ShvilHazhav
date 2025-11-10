package com.example.sagivproject.models;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean isAdmin;

    public User() { }

    public User(String id, String firstName, String lastName, String email, String password, boolean isAdmin) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getId() { return this.id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public boolean getIsAdmin() { return this.isAdmin; }
    public void setIsAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }

    public void toggleAdminStatus() { this.isAdmin = !this.isAdmin; }

    // פעולה שמחזירה את השם המלא של המשתמש
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
