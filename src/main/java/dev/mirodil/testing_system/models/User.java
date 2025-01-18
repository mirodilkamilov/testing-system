package dev.mirodil.testing_system.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

@Table("users")
public class User implements UserDetails {
    private static final Set<String> ALLOWED_SORT_ATTRIBUTES = Set.of(
            "id", "email", "role", "fname", "lname", "status", "created_at"
    );
    private static final Set<String> ALLOWED_FILTER_ATTRIBUTES = Set.of(
            "id", "email", "role", "fname", "lname", "status", "created_at"
    );

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

    public static Set<String> getAllowedSortAttributes() {
        return ALLOWED_SORT_ATTRIBUTES;
    }

    public static Set<String> getAllowedFilterAttributes() {
        return ALLOWED_FILTER_ATTRIBUTES;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setUserStatus(UserStatus status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
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
