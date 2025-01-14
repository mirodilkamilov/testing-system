package dev.mirodil.testing_system.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.models.UserGender;
import dev.mirodil.testing_system.models.UserRole;
import dev.mirodil.testing_system.models.UserStatus;
import org.springframework.security.core.GrantedAuthority;

import java.net.URI;
import java.util.Collection;
import java.util.Date;


public class UserResponseDTO {
    private final Long userId;
    private final UserRole userRole;
    private final String email;
    private final UserGender gender;
    private final UserStatus status;
    private final String fname;
    private final String lname;
    private final Boolean isAccountNonLocked;
    private final Date createdAt;
    private final URI path;
    @JsonIgnore
    private final Collection<? extends GrantedAuthority> grantedAuthorities;
    @JsonIgnore
    private final String password;

    public UserResponseDTO(User user) {
        userId = user.getId();
        userRole = user.getUserRole();
        email = user.getEmail();
        gender = user.getGender();
        status = user.getStatus();
        fname = user.getFirstName();
        lname = user.getLastName();
        isAccountNonLocked = user.isAccountNonLocked();
        createdAt = user.getCreatedAt();
        this.path = URI.create("/api/users/" + user.getId());
        this.grantedAuthorities = user.getAuthorities();
        this.password = user.getPassword();
    }

    public URI getPath() {
        return path;
    }

    public Long getUserId() {
        return userId;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public String getEmail() {
        return email;
    }

    public UserGender getGender() {
        return gender;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public Boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    public Collection<? extends GrantedAuthority> getGrantedAuthorities() {
        return grantedAuthorities;
    }

    public String getPassword() {
        return password;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
