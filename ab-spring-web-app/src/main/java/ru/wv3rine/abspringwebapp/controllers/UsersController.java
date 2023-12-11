package ru.wv3rine.abspringwebapp.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.wv3rine.abspringwebapp.exceptions.NotEnoughArgumentsException;
import ru.wv3rine.abspringwebapp.models.User;
import ru.wv3rine.abspringwebapp.models.UserIdAndLogin;
import ru.wv3rine.abspringwebapp.other.UserMutableFields;
import ru.wv3rine.abspringwebapp.services.UsersService;

import java.util.List;

// В исходной статье, которую кинули для тестов, формат ответа был
// в виде ResponseEntity. Но кажется это оверкилл, поэтому я попробовал
// без него (прав ли я? И вообще, когда стоит использовать ResponseEntity? Вроде
// это полезно, но пишут, что не стоит злоупотреблять. Почему?)

// todo
// По-хорошему createUser должен уметь делать только admin,
// updatePerson можно делать только с юзера с таким же id

/**
 * Класс CRUD (без Delete) контроллера для взаимодействия с пользователями
 * (класс {@link User}
 */
@RestController
@RequestMapping("${application.url.users-api}")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;

    // Здесь был createNewUser, но он заменился по смыслу на регистрацию

    @GetMapping
    public List<UserIdAndLogin> getUsersNames() {
        return usersService.getIdAndLogins();
    }

    // Я пока совершенно не понимаю, почему message не отображаются,
    // а вместо них Validation Error. Его даже в jakarta constraints нет
    // хотя походу нужен специфический ExceptionHandler
    @GetMapping(params = {"page", "pageSize"})
    public Slice<UserIdAndLogin> getUsersNames(@Min(value = 1, message = "Page size must be greater than one!")
                                               @Max(value = 100, message = "Page size must be less than 100!")
                                               @RequestParam
                                               Integer pageSize,
                                               @Min(value = 0, message = "Page must be greater than zero!")
                                               @RequestParam
                                               Integer page) {
        return usersService.findIdAndLogins(pageSize, page);
    }


    /**
     * Получение пользователя по id. Запрос возможен только для
     * пользователя, id которого совпадает с id в пути
     * @param id id пользователя
     * @return пользователь с указанным id
     */
    @GetMapping(path = "/{id}")
    public User getUser(@NotNull @PathVariable("id") Integer id) {
        // Возможно, этот функционал с доступом стоит вынести в отдельный слой, но
        // пока этого делать не хочется))
        String userLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!id.equals(usersService.findIdByLogin(userLogin))) {
            throw new AccessDeniedException("You don't have access to get other user");
        }
        return usersService.getUserById(id);
    }

    // У этого метода есть проблема: если менять логин, то
    // токен становится невалидным (и это логично с точки зрения
    // бизнес-логики: логин должен оставаться таким же, если на то
    // не вызвана специальная отдельная операция). Поэтому в запросе
    // можно изменить все, кроме логина (и id соответственно)
    /**
     * Изменение пользователя по id. Запрос возможен только для
     * пользователя, id которого совпадает с id в пути
     * @param id id пользователя
     * @param user пользователь, у которого нужно взять not null поля
     * @return пользователь с указанным id
     */
    @PutMapping(path = "/{id}")
    public User updatePerson(@PathVariable("id") Integer id,
                             @NotNull @RequestBody UserMutableFields user,
                             BindingResult bindingResult) {
        String userLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!id.equals(usersService.findIdByLogin(userLogin))) {
            throw new AccessDeniedException("You don't have access to get other user");
        }
        if (bindingResult.hasErrors()) {
            throw new NotEnoughArgumentsException("User must have login and password");
        }
        return usersService.updateUserById(id, user);
    }
}
