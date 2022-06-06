package antifraud.repository;

import antifraud.model.ValidatorConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValidatorConfigRepository extends JpaRepository<ValidatorConfig, Integer> {

    @Override
    Optional<ValidatorConfig> findById(Integer id);

    boolean existsById(Integer id);
}