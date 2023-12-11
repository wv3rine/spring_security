package ru.wv3rine.abspringwebapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.wv3rine.abspringwebapp.controllers.security.AuthenticationRequest;
import ru.wv3rine.abspringwebapp.controllers.security.AuthenticationResponse;
import ru.wv3rine.abspringwebapp.controllers.security.RegisterRequest;
import ru.wv3rine.abspringwebapp.models.Role;
import ru.wv3rine.abspringwebapp.models.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

// можно ли как-то нормально очищать пользователей?

@ComponentScan("ru.wv3rine.abspringwebapp")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AuthenticationControllerTests.class)
public class AuthenticationControllerTests {
    // вынести в application.yml
    private static final String API_URL = "/api/v1/auth";
    private final TestRestTemplate testRestTemplate;

    @Autowired
    public AuthenticationControllerTests(TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate;
    }

    @Test
    public void whenRegisterUser_thanHttpStatusOk() {
        User user = createTestUser("peergynt", "12345");
        var response = getRegisterRequest(new RegisterRequest(user.getLogin(), user.getPassword()));
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void whenLoginValidUser_thanHttpStatusOkAndTokenIsValid() {
        User user = createTestUser("hamlet", "12345");
        var registerResponse = getRegisterRequest(new RegisterRequest(user.getLogin(), user.getPassword()));
        assertThat(registerResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(registerResponse.getBody(), notNullValue());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + registerResponse.getBody().getToken());

        var authResponse = testRestTemplate.exchange(API_URL + "/authenticate",
                HttpMethod.POST,
                new HttpEntity<>(new AuthenticationRequest(user.getLogin(), user.getPassword()), headers),
                AuthenticationResponse.class);

        assertThat(authResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(authResponse.getBody(), notNullValue());
    }

    @Test
    public void whenLoginInvalidUser_thanHttpStatusForbidden() {
        var authResponse = getAuthRequest(new AuthenticationRequest("bobchinsky", "dobchinsky"));
        assertThat(authResponse.getStatusCode(), is(HttpStatus.FORBIDDEN));
    }

    @Test
    public void whenRegisterUserWithoutPassword_thanHttpStatusForbidden() {
        var authResponse = getAuthRequest(new AuthenticationRequest("kinglir", null));
        assertThat(authResponse.getStatusCode(), is(HttpStatus.FORBIDDEN));
    }

    @Test
    public void whenLoginUserWithInvalidPassword_thanHttpStatusForbidden() {
        User user = createTestUser("macduff", "12345");
        var registerResponse = getRegisterRequest(new RegisterRequest(user.getLogin(), user.getPassword()));
        assertThat(registerResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(registerResponse.getBody(), notNullValue());

        var authResponse = getAuthRequest(new AuthenticationRequest(user.getLogin(), "23451"));
        assertThat(authResponse.getStatusCode(), is(HttpStatus.FORBIDDEN));
    }


    // Здесь такая штука: пользователь получает 403 в любом случае, но
    // сервер выдает разные эксепшены (когда jwt token вообще не соответствует
    // структуре, одно; если соответствует, то другое). И вопрос: я должен
    // как-то их хэндлить? По идее должен быть какой-то отдельный эксепшн
    // хэндлер для логов, но пока думаю, его не нужно реализовывать (сервер
    // же не падает и ведет себя предсказуемо)
    @Test
    public void whenLoginUserWithInvalidToken_thanHttpStatusForbidden() {

        User user = createTestUser("shoyda", "12345");
        var registerResponse = getRegisterRequest(new RegisterRequest(user.getLogin(), user.getPassword()));
        assertThat(registerResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(registerResponse.getBody(), notNullValue());

        HttpHeaders headers = new HttpHeaders();
        StringBuilder invalidToken = new StringBuilder(registerResponse.getBody().getToken());
        invalidToken.setCharAt(25, invalidToken.charAt(25) == '1' ? '2' : '1');
        headers.add("Authorization", "Bearer " + invalidToken.toString());

        var authResponse = testRestTemplate.exchange(API_URL + "/authenticate",
                HttpMethod.POST,
                new HttpEntity<>(new AuthenticationRequest(user.getLogin(), user.getPassword()), headers),
                AuthenticationResponse.class);
        assertThat(authResponse.getStatusCode(), is(HttpStatus.FORBIDDEN));
    }

    private ResponseEntity<AuthenticationResponse> getRegisterRequest(RegisterRequest request) {
        return testRestTemplate.postForEntity(API_URL + "/register", request, AuthenticationResponse.class);
    }

    private ResponseEntity<AuthenticationResponse> getAuthRequest(AuthenticationRequest request) {
        return testRestTemplate.postForEntity(API_URL + "/authenticate", request, AuthenticationResponse.class);
    }

    private User createTestUser(String login, String password) {
        return new User(null, null, login, password, "", Role.USER);
    }
}
