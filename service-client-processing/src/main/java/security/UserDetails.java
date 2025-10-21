package security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class UserDetails {

    private final Long id;

    private final String username;

    private final String email;

    @JsonIgnore
    private String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetails(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetails build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetails(
                user.getId(),
                user.getLogin(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    public boolean isEnabled() {
        return true;    }

    public boolean isCredentialsNonExpired() {
        return true;    }

    public boolean isAccountNonLocked() {
        return true;    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
            return false;
        UserDetails user = (UserDetails) obj;
        return Objects.equals(id, user.id);
    }
}
