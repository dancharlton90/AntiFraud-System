package antifraud.repository;

import antifraud.model.SuspiciousIP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SuspiciousIPRepository extends JpaRepository<SuspiciousIP, Long> {

    boolean existsSuspiciousipByIp(String ip);

    Optional<SuspiciousIP> findByIp(String ip);
}