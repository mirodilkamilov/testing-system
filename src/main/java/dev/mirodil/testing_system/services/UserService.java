package dev.mirodil.testing_system.services;

import dev.mirodil.testing_system.dtos.UserRegisterRequestDTO;
import dev.mirodil.testing_system.dtos.UserResponseDTO;
import dev.mirodil.testing_system.exceptions.ResourceNotFoundException;
import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.models.UserRole;
import dev.mirodil.testing_system.repositories.UserRepository;
import dev.mirodil.testing_system.utils.AuthUtil;
import dev.mirodil.testing_system.utils.PageWithFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserResponseDTO> getUsers(PageWithFilterRequest pageable) {
        List<User> users = userRepository.findAndSortUsersWithPagination(pageable);
        long totalElements = userRepository.countUsersById();

        List<UserResponseDTO> usersDTO = users.stream()
                .map(UserResponseDTO::new)
                .toList();

        return new PageImpl<>(usersDTO, pageable, totalElements);
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + email)
        );
        return new UserResponseDTO(user);
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User with " + username + " username doesn't exist")
        );
    }

    public UserResponseDTO createUser(UserRegisterRequestDTO request) {
        User user = new User(
                request.email(),
                AuthUtil.encodePassword(request.password()),
                request.fname(),
                request.lname(),
                request.gender()
        );
        user.setUserRole(UserRole.TEST_TAKER);
        user = userRepository.save(user);

        return new UserResponseDTO(user);
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findUserById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + id)
        );
        return new UserResponseDTO(user);
    }
}
