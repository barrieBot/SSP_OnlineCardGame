package game.CardGame.services;

import game.CardGame.dtos.LoginUserDto;
import game.CardGame.dtos.RegisterUserDto;
import game.CardGame.models.UserModel;
import game.CardGame.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserModel signup(RegisterUserDto input) {
        UserModel user = new UserModel();
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setUserPassword(passwordEncoder.encode(input.getPassword()));
        return userRepository.save(user);
    }

    public UserModel authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()
                )
        );

        return userRepository.findByUsername(input.getUsername())
                .orElseThrow();
    }
}