package antifraud.service;

import antifraud.util.Validator;
import antifraud.model.SuspiciousIP;
import antifraud.repository.SuspiciousIPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class SuspiciousIPService {

    @Autowired
    SuspiciousIPRepository suspiciousIPRepo;

    public ResponseEntity getAll() {
        return ResponseEntity.ok(suspiciousIPRepo.findAll());
    }

    public ResponseEntity addIp(SuspiciousIP ip) {
        if (suspiciousIPRepo.existsSuspiciousipByIp(ip.getIp())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        } else if (!Validator.isIpValid(ip.getIp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else {
            suspiciousIPRepo.save(ip);
            return ResponseEntity.ok(ip);
        }
    }

    public ResponseEntity deleteByIp(String ipAddress) {
        if (!Validator.isIpValid(ipAddress)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        SuspiciousIP SusIp = suspiciousIPRepo.findByIp(ipAddress)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        suspiciousIPRepo.delete(SusIp);
        return ResponseEntity.ok(Map.of("status", "IP " + ipAddress + " successfully removed!"));
    }
}
