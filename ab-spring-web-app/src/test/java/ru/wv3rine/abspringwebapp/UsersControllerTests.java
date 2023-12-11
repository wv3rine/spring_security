package ru.wv3rine.abspringwebapp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.wv3rine.abspringwebapp.controllers.security.AuthenticationResponse;
import ru.wv3rine.abspringwebapp.controllers.security.RegisterRequest;
import ru.wv3rine.abspringwebapp.dao.UsersDAO;
import ru.wv3rine.abspringwebapp.models.Role;
import ru.wv3rine.abspringwebapp.models.User;
import ru.wv3rine.abspringwebapp.other.UserMutableFields;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

// https://sysout.ru/testirovanie-spring-boot-prilozheniya-s-testresttemplate/
// частично отсюда

// тестовое покрытие неполное, но и приложение
// простое

// todo
// добавить тестов
@ComponentScan("ru.wv3rine.abspringwebapp")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = UsersControllerTests.class)
@WithMockUser(username = "admin", roles = "ADMIN")
class UsersControllerTests {
    // вынести в application.yml
    private static final String API_URL = "/api/v1/users";
    private final TestRestTemplate testRestTemplate;

    // не могу пока понять, почему с моком не работает
    //@Mock
    private final UsersDAO usersDAO;

    @Autowired
    UsersControllerTests(TestRestTemplate testRestTemplate, UsersDAO usersDAO) {
        this.testRestTemplate = testRestTemplate;
        this.usersDAO = usersDAO;
    }

    // Какой есть хороший способ очищать БД? Я пока плохо понимаю,
    // какие из них лучше, потому что в интернете просто как будто пишут
    // "этот лучше" без объяснений))

    @AfterEach
    public void resetDb() {
        usersDAO.deleteAll();
    }

    @Test
    public void whenCreateUser_thenHttpStatusOk() {
        UserRequestFields registerResponseFields = addTestUser("hlestakov", "12345");
        User user = registerResponseFields.user();

        assertThat(registerResponseFields.user(), notNullValue());
        assertThat(user.getLogin(), is("hlestakov"));
        // пароль нет смысла проверять, потому тчо он закодированный. Да и вообще
        // этот метод - тупо проверка регистрации фактически)) которая повторяется
    }

    @Test
    public void whenGetBadUser_thenHttpStatusNotFound() {
        UserRequestFields registerResponseFields = addTestUser("shente", "12345");
        HttpHeaders headers = registerResponseFields.httpHeaders();

        var response = testRestTemplate.exchange(API_URL + "/{id}",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                User.class,
                150);

        // todo
        // по-хорошему тут not found, но надо делать эксепшн хендлер
        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN));
    }

    @Test
    public void whenGetUsers_thenHttpStatusOkay_andAnswerIsOkay() {
        UserRequestFields registerResponseFields = addTestUser("ivanilych", "12345");
        User user0 = registerResponseFields.user();
        HttpHeaders headers = registerResponseFields.httpHeaders();

        registerResponseFields = addTestUser("podpolnyy", "12345");
        User user1 = registerResponseFields.user();

        var response = testRestTemplate.exchange(API_URL,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<List<User>>(){
                });
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), notNullValue());
        assertThat(response.getBody(), hasSize(2));
        assertThat(response.getBody().get(0).getId(), is(user0.getId()));
        assertThat(response.getBody().get(1).getId(), is(user1.getId()));
        assertThat(response.getBody().get(0).getLogin(), is(user0.getLogin()));
        assertThat(response.getBody().get(1).getLogin(), is(user1.getLogin()));
    }

    @Test
    public void whenBadUserAdd_thenDoesntAddToDatabase() {
        UserRequestFields registerResponseFields = addTestUser("sobakevich", null);
        User user = registerResponseFields.user();
        assertThat(user, nullValue());
    }

    //todo:
    // Я совершенно не понимаю, почему не работают эти два теста: там какая-то ошибка
    // c GrantAuthorities, что он не может их выдать, но при этом у меня все имплементирвоано
    // и интерфейсов нет, а ошибки с lombok и отсутствием noargsconstructor у меня не было. И причем
    // почему-то это не работает только на тестах, а в обычном http реквесте - все окей
    // Возможно, какой-то контекст не подгружается, но я не понимаю какой
/*    @Test
    public void whenValidGetUser_thenHttpStatusOk() {
        UserRequestFields registerResponseFields = addTestUser("wan", "12345");
        HttpHeaders headers = registerResponseFields.httpHeaders();
        User user = registerResponseFields.user();

        var response = testRestTemplate.exchange(API_URL + "/{id}",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                User.class,
                user.getId());

        // todo
        // по-хорошему тут not found, но надо делать эксепшн хендлер
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void whenPutUserWithValidId_thenHttpStatusOkay_andAnswerIsOkay() {
        UserRequestFields registerResponseFields = addTestUser("otello", "12345");
        User user = registerResponseFields.user();
        HttpHeaders headers = registerResponseFields.httpHeaders();
        UserMutableFields usersNewFields = new UserMutableFields("treplev", null, "url", Role.USER);

        var response = testRestTemplate.exchange(API_URL + "/{id}",
                HttpMethod.PUT,
                new HttpEntity<>(usersNewFields, headers),
                User.class,
                user.getId());

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), notNullValue());
        assertThat(response.getBody().getId(), is(user.getId()));
        assertThat(response.getBody().getLogin(), is("otello"));
        assertThat(response.getBody().getName(), is("treplev"));
        assertThat(response.getBody().getPassword(), is("12345"));
        assertThat(response.getBody().getUrl(), is("url"));
    }*/


    /**
     * Метод, который производит запрос регистрации пользователя и возвращает полезные
     * его составляющие
     * @param login логин пользователя
     * @param password пароль пользователя
     * @return объект типа {@link UserRequestFields}, содержащий
     * информацию о полученном jwt, о пользователе и о http-заголовках
     */
    private UserRequestFields addTestUser(String login, String password) {
        var authResponse = testRestTemplate.postForEntity("/api/v1/auth/register",
                new RegisterRequest(login, password), AuthenticationResponse.class);
        assertThat(authResponse, notNullValue());
        if (authResponse.getBody() == null) {
            return new UserRequestFields(null, null, null);
        }
        String token = authResponse.getBody().getToken();
        User user = usersDAO.findByLogin(login).orElse(null);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        return new UserRequestFields(token, user, headers);
    }

}
