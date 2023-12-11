package ru.wv3rine.abspringwebapp.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.wv3rine.abspringwebapp.controllers.security.AuthenticationRequest;
import ru.wv3rine.abspringwebapp.controllers.security.AuthenticationResponse;
import ru.wv3rine.abspringwebapp.controllers.security.RegisterRequest;
import ru.wv3rine.abspringwebapp.exceptions.LoginHasBeenUsedException;
import ru.wv3rine.abspringwebapp.services.AuthenticationService;

// переделать расположения папок на auth и user. Хотя стоит ли?))

/**
 * Класс контроллера, отвечающего за аутентификацию и регистрацию пользователя
 */
@RestController
@RequestMapping("${application.url.auth-api}")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;

    // ResponseEntity? В оригинальном коде был ResponseEntity, а зачем?
    @PostMapping("/register")
    public AuthenticationResponse register(@RequestBody RegisterRequest request) throws LoginHasBeenUsedException {
        // todo
        // раздражает пока, что пользователю не выдается нормальная ошибка
        if (service.isLoginUsed(request.getLogin())) {
            throw new LoginHasBeenUsedException("Login has been used");
        }
        return service.register(request);
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request) {
        return service.authenticate(request);
    }
}
