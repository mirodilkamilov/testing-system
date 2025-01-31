package dev.mirodil.testing_system.models;

import dev.mirodil.testing_system.models.enums.PermissionType;
import dev.mirodil.testing_system.models.enums.UserGender;
import dev.mirodil.testing_system.models.enums.UserRole;
import dev.mirodil.testing_system.models.enums.UserStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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
            "userId", "email", "userRole", "fname", "lname", "status", "createdAt"
    );
    private static final Set<String> ALLOWED_FILTER_ATTRIBUTES = Set.of(
            "userId", "email", "userRole", "fname", "lname", "status", "createdAt"
    );

    @Id
    private Long id;
    private String email;
    private String password;
    private Long roleId;
    private String fname;
    private String lname;
    private UserGender gender;
    private UserStatus status;
    private Date createdAt;

    @Transient
    private Role role;
    @Transient
    private Set<PermissionType> permissionNames;

    public User() {
    }

    public User(String email, String password, Long roleId, String fname, String lname, UserGender gender) {
        this.email = email;
        this.password = password;
        this.roleId = roleId;
        this.fname = fname;
        this.lname = lname;
        this.gender = gender;
        this.status = UserStatus.ACTIVE;
        createdAt = new Date();
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

    public Role getUserRole() {
        return role;
    }

    public void setUserRole(Role role) {
        this.role = role;
    }

    public UserRole getUserRoleName() {
        return role.getName();
    }

    public Long getRoleId() {
        return roleId;
    }

    public boolean isAdmin() {
        return role.getName() == UserRole.ADMIN;
    }

    public Set<PermissionType> getPermissionNames() {
        return permissionNames;
    }

    public void setPermissionNames(Set<PermissionType> permissionNames) {
        this.permissionNames = permissionNames;
    }

    public String getFirstName() {
        return fname;
    }

    public String getLastName() {
        return lname;
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
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
        // Add the user's role as a GrantedAuthority
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toString().toUpperCase()));
        // Add permissions associated with the role
        permissionNames.forEach(permission ->
                grantedAuthorities.add(new SimpleGrantedAuthority(permission.name()))
        );

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
