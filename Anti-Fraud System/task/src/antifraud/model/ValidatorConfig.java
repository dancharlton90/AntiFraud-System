package antifraud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "validator_config")
public class ValidatorConfig {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    private int maxAllowed;
    private int maxManual;

    public ValidatorConfig() {
    }

    public ValidatorConfig(int id, int maxAllowed, int maxManual) {
        this.id = id;
        this.maxAllowed = maxAllowed;
        this.maxManual = maxManual;
    }
}