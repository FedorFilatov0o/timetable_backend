package app.timetable_back.service;

import java.util.Date;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.timetable_back.config.jwt.JwtTokenProvider;
import app.timetable_back.config.security.MyUserDetails;
import app.timetable_back.dto.AuthResponse;
import app.timetable_back.dto.LoginRequest;
import app.timetable_back.dto.RegisterRequest;
import app.timetable_back.dto.RegistrationResponse;
import app.timetable_back.entity.User;
import app.timetable_back.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenStorageService tokenStorageService;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository, PasswordEncoder passwordEncoder, TokenStorageService tokenStorageService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenStorageService = tokenStorageService;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            tokenStorageService.saveToken(request.getEmail(), refreshToken);

            User user = userDetails.getUser();

            return AuthResponse.builder().userId(user.getId()).accessToken(accessToken).refreshToken(refreshToken)
                    .tokenType("Bearer").role(user.getRole().name()).build();
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Неверный email или пароль");
        }
    }

    public RegistrationResponse registerNewUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
                .email(request.getEmail()).passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole()).phone(request.getPhone()).build();

        userRepository.save(user);

        return RegistrationResponse.builder().message("Пользователь успешно зарегистрирован").build();
    }

    public AuthResponse refreshTokens(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Неверный refresh токен");
        }

        String email = jwtTokenProvider.extractUsername(refreshToken);

        if (!tokenStorageService.isTokenValid(email, refreshToken)) {
            throw new BadCredentialsException("Refresh токен устарел или недействителен");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Пользователь не найден"));

        MyUserDetails userDetails = new MyUserDetails(user);

        if (jwtTokenProvider.extractExpiration(refreshToken).before(new Date())) {
            throw new BadCredentialsException("Refresh токен истек");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        tokenStorageService.saveToken(email, newRefreshToken);

        return AuthResponse.builder().userId(user.getId()).accessToken(newAccessToken).refreshToken(newRefreshToken)
                .tokenType("Bearer").role(user.getRole().name()).build();
    }

    public void logout(Authentication authentication) {
        String email = authentication.getName();
        log.info("Logout requested for user: {}", email);
        tokenStorageService.revokeToken(email);
        SecurityContextHolder.clearContext();
        log.info("User {} logged out successfully", email);
    }
}
