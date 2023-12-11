package ru.wv3rine.abspringwebapp.other;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.wv3rine.abspringwebapp.models.Role;
import ru.wv3rine.abspringwebapp.models.User;

// todo
// По-хорошему это интерфейс и его должен реализовывать
// user для удобства, но ладно

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserMutableFields {
    private String name;
    @NotNull
    @NotBlank
    private String password;
    private String url;
    @Enumerated(EnumType.STRING)
    private Role role;

    // Возможно, это лучше в отдельный класс)) а то
    // принцип единственной ответственности не соблюден, это
    // и сервис, и модель


    // Вместо этого метода должен был быть BeansUtil из
    // other, но там, во-первых, рефлексия, во-вторых
    // класс User - уже внутри security и реализует
    // UsersDetails, от чего он перестает быть формально
    // моделью и происходят какие-то баги при передаче
    // (и почему-то только в тесте), поэтому я решил сделать
    // отдельный класс модель (да и это полезно)
    /**
     * Метод, копирующий not null поля в target
     * @param target пользователь, куда копируются поля
     * @return пользователя с новыми полями
     */
    public User copyNotNullFields(User target) {
        // todo
        // можно покрасивее со словарем сделать

        if (name != null) {
            target.setName(name);
        }
        if (password != null) {
            target.setPassword(password);
        }
        if (url != null) {
            target.setUrl(url);
        }
        if (role != null) {
            target.setRole(role);
        }
        return target;
    }
}
