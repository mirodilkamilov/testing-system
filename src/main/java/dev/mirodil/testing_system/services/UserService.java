package dev.mirodil.testing_system.services;

import dev.mirodil.testing_system.dtos.UserRegisterRequestDTO;
import dev.mirodil.testing_system.dtos.UserResponseDTO;
import dev.mirodil.testing_system.exceptions.ResourceNotFoundException;
import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.models.UserRole;
import dev.mirodil.testing_system.repositories.UserRepository;
import dev.mirodil.testing_system.utils.AuthUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponseDTO> getAllUsers() {
        Iterable<User> users = userRepository.findAll();
        List<UserResponseDTO> usersDTO = new ArrayList<>();
        users.forEach(
                user -> usersDTO.add(new UserResponseDTO(user))
        );

        return usersDTO;
    }

    public Optional<UserResponseDTO> getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            UserResponseDTO userDTO = new UserResponseDTO(user.get());
            return Optional.of(userDTO);
        }

        return Optional.empty();
    }

    public Optional<UserResponseDTO> getUserByEmail(String email) {
        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isPresent()) {
            UserResponseDTO userDTO = new UserResponseDTO(user.get());
            return Optional.of(userDTO);
        }

        return Optional.empty();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username).orElseThrow(ResourceNotFoundException::new);
    }

    public UserResponseDTO createUser(UserRegisterRequestDTO request) {
        User user = new User(
                request.getEmail(),
                AuthUtil.encodePassword(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                request.getGender()
        );
        user.setUserRole(UserRole.TEST_TAKER);
        user = userRepository.save(user);

        return new UserResponseDTO(user);
    }
}
