package ru.wv3rine.abspringwebapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.wv3rine.abspringwebapp.dao.UsersDAO;
import ru.wv3rine.abspringwebapp.exceptions.ResourceNotFoundException;
import ru.wv3rine.abspringwebapp.models.User;
import ru.wv3rine.abspringwebapp.models.UserIdAndLogin;
import ru.wv3rine.abspringwebapp.other.UserMutableFields;

import java.util.List;

/**
 * Класс (с аннотацией {@link Service}) для работы с
 * базой данных для класса {@link User}
 */
@Service
@RequiredArgsConstructor
public class UsersService {
    private final UsersDAO usersDAO;

    /**
     * Добавление пользователя в базу данных. Пользователь обязательно
     * должен иметь имя и пароль
     * @param user добавляеиый пользователь
     */
    public User addUser(User user) {
        return usersDAO.save(user);
    }

    /**
     * Нахождение пользователя по его id
     * @param id id пользователя
     * @return пользователь с указанным id, если таковой существует
     * (иначе исключение)
     */
    public User getUserById(Integer id) {
        return usersDAO.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    // здесь можно было бы выбрасывать исключение, если login
    // подается, но решил пока сделать так
    /**
     * Обновление пользователя с идентификатором id на notnull поля user. Если пользователя с таким id
     * не существует, выбрасывает исключение. Иначе изменяет поля
     * в исходной базе данных у пользователя с таким же id, как у переданного,
     * на новые значения полей, если они не null (так же не изменяется
     * поле login)
     * @param id id пользователя, у которого нужно поменять поля
     * @param userFields пользователь, поля которого нужно передать исходному
     */
    public User updateUserById(Integer id, UserMutableFields userFields) {
        if (!usersDAO.existsById(id)) {
            throw new ResourceNotFoundException("User not found!");
        }

        // некотороая логика копирования notnull полей
        User userById = usersDAO.findById(id).orElse(null);
        userById = userFields.copyNotNullFields(userById);

        return usersDAO.save(userById);
    }

    // Здесь везде я заменил name на login, потому что это
    // кажется логичнее

    /**
     * Получение списка пользователей в формате класса с методами
     * getId() и getLogin() ({@link UserIdAndLogin}
     * @return список пользователей {@link UserIdAndLogin}
     */
    public List<UserIdAndLogin> getIdAndLogins() {
        return usersDAO.findAllProjectedBy(Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * Получение {@link Slice} пользователей в формате класса
     * {@link UserIdAndLogin} (где хранятся id и login) на указанной странице
     * @param pageSize размер страницы в списке (максимальное число элементов в одной ячейке {@link Slice})
     * @param page номер страницы
     * @return {@link Slice} пользователей {@link UserIdAndLogin}
     */
    public Slice<UserIdAndLogin> findIdAndLogins(int pageSize, int page) {
        return usersDAO.findAllProjectedBy(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "id")));
    }

    /**
     * Получение id пользователя с логином userLogin
     * @param userLogin логин пользователя
     * @return id пользователя, если он был найден, иначе null
     */
    public Integer findIdByLogin(String userLogin) {
        User user = usersDAO.findByLogin(userLogin).orElse(null);
        return user == null ? null : user.getId();
    }

    // стоит ли выносить это в отдельный сервис или в конфиг? Пока думаю, что нет,
    // но если будет больше взаимодействий с UserDetails, то мб
    // (здесь была реализация loadUserbyUsername, но я ее все-таки убрал)

}
