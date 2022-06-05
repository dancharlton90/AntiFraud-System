package antifraud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Validated
@Entity
@Table(name = "stolencard")
public class StolenCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    private String number;

    public StolenCard() {
    }

}