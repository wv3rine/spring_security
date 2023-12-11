package ru.wv3rine.abspringwebapp;

import org.springframework.http.HttpHeaders;
import ru.wv3rine.abspringwebapp.models.User;

public record UserRequestFields(
        String token,
        User user,
        HttpHeaders httpHeaders
) {
}
