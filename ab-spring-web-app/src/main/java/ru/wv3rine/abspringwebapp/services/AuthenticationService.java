package ru.wv3rine.abspringwebapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.wv3rine.abspringwebapp.controllers.security.AuthenticationRequest;
import ru.wv3rine.abspringwebapp.controllers.security.AuthenticationResponse;
import ru.wv3rine.abspringwebapp.controllers.security.RegisterRequest;
import ru.wv3rine.abspringwebapp.dao.UsersDAO;
import ru.wv3rine.abspringwebapp.exceptions.ResourceNotFoundException;
import ru.wv3rine.abspringwebapp.models.Role;
import ru.wv3rine.abspringwebapp.models.User;

import javax.naming.NameNotFoundException;

/**
 * Сервис для взаимодействия с аутентификацией
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UsersDAO usersDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    /**
     * Проверка, занят ли логин
     * @param login логин пользователя
     * @return true, если логин уже используется, false иначе
     */
    public boolean isLoginUsed(String login) {
        try {
            userDetailsService.loadUserByUsername(login);
            return true;
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    /**
     * Метод, производящий регистрацию пользователя и сохранения
     * его в базу данных (если логин уже присутствует в базе данных,
     * то пароль просто заменяется)
     * @param request параметры запроса регистрации
     * @return ответ (jwt)
     */
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .login(request.getLogin())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        usersDAO.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Метод, отвечающий за аутентификацию пользователя и проверку
     * наличия его в базе данных
     * @param request параметры запроса аутентификации
     * @return ответ (jwt)
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLogin(),
                        request.getPassword()
                )
        );
        var user = usersDAO.findByLogin(request.getLogin())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
