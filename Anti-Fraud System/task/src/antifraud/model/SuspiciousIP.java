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
@Table(name = "suspiciousip")
public class SuspiciousIP {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    private String ip;

    public SuspiciousIP() {
    }
}