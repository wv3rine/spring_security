package ru.wv3rine.abspringwebapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class LoginHasBeenUsedException extends Throwable {
    public LoginHasBeenUsedException(String message) {
        super(message);
    }
}
