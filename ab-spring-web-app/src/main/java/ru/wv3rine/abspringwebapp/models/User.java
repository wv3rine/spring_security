package ru.wv3rine.abspringwebapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// В задании написано про
// name notnull, хотя логичнее это сделать с логином (с учетом
// акцента на аутентификации)

// Еще: Семен когда-то говорил, что лучше использовать рекорды,
// но Entity не работает с рекордами: есть какой-то обход или
// к JPA это не относится?
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    @NotNull
    @NotBlank
    private String login;
    @NotNull
    @NotBlank
    private String password;
    // todo
    // в контроллерах url сейчас заменен просто на id,
    // но по-хорошему нужно туда сунуть именно url (и сделать
    // его значение по умолчанию как id
    private String url;
    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
