package dev.mirodil.testing_system.services;

import dev.mirodil.testing_system.dtos.UserRegisterRequestDTO;
import dev.mirodil.testing_system.dtos.UserResponseDTO;
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
import java.util.Optional;

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

    public Optional<UserResponseDTO> getUserByEmail(String email) {
        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isPresent()) {
            UserResponseDTO userDTO = new UserResponseDTO(user.get());
            return Optional.of(userDTO);
        }

        return Optional.empty();
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findUserByEmail(username);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User with " + username + " username doesn't exist");
        }

        return optionalUser.get();
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
}
