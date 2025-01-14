package dev.mirodil.testing_system.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Table("users")
public class User implements UserDetails {
    @Id
    private Long id;
    private String email;
    private String password;
    private UserRole role;
    private String fname;
    private String lname;
    private UserGender gender;
    private UserStatus status;
    private Date created_at;

    public User() {
    }

    public User(String email, String password, String fname, String lname, UserGender gender) {
        this.email = email;
        this.password = password;
        this.role = UserRole.TEST_TAKER;
        this.fname = fname;
        this.lname = lname;
        this.gender = gender;
        this.status = UserStatus.ACTIVE;
        created_at = new Date();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return fname;
    }

    public String getLastName() {
        return lname;
    }

    public UserRole getUserRole() {
        return role;
    }

    public void setUserRole(UserRole role) {
        this.role = role;
    }

    public UserGender getGender() {
        return gender;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (UserRole role : UserRole.values()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.name().toUpperCase()));
        }
        return grantedAuthorities;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.BLOCKED;
    }

    @Override
    public boolean isEnabled() {
        return isAccountNonLocked();
    }
}
