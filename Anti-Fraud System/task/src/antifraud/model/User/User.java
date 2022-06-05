package antifraud.model.User;

import antifraud.security.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@Entity
@Validated
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull(message = "Name cannot be empty!")
    private String name;

    @NotNull(message = "Username cannot be empty!")
    private String username;

    @NotNull(message = "Password cannot be empty!")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonIgnore
    private boolean isAccountNonLocked;

    public User() {
        this.isAccountNonLocked = true;
    }
}
