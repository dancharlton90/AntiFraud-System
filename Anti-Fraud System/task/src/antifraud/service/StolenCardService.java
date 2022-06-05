package antifraud.service;

import antifraud.util.Validator;
import antifraud.model.StolenCard;
import antifraud.repository.StolenCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class StolenCardService {

    @Autowired
    StolenCardRepository stolenCardRepo;

    public ResponseEntity getAll() {
        return ResponseEntity.ok(stolenCardRepo.findAll());
    }

    public ResponseEntity addCard(StolenCard stolenCard) {
        if (!Validator.isCardValid(stolenCard.getNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (stolenCardRepo.existsStolencardByNumber(stolenCard.getNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        } else {
            stolenCardRepo.save(stolenCard);
            return ResponseEntity.ok(stolenCard);
        }
    }

    public ResponseEntity deleteByCardNumber(String cardNumber) {
        if (!Validator.isCardValid(cardNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        StolenCard stolenCard = stolenCardRepo.findByNumber(cardNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        stolenCardRepo.delete(stolenCard);
        return ResponseEntity.ok(Map.of("status", "Card " + cardNumber + " successfully removed!"));
    }
}
