package antifraud.controller;

import antifraud.model.StolenCard;
import antifraud.model.SuspiciousIP;
import antifraud.model.Transaction;
import antifraud.service.StolenCardService;
import antifraud.service.SuspiciousIPService;
import antifraud.service.TransactionService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {

    @Autowired
    TransactionService transactionService;
    @Autowired
    SuspiciousIPService suspiciousIPService;
    @Autowired
    StolenCardService stolenCardService;

    // Transaction Operations
    @PostMapping("/transaction")
    public ResponseEntity postTransaction(@RequestBody @Valid Transaction transaction) {
        return transactionService.checkTransaction(transaction);
    }

    @GetMapping("/history")
    public ResponseEntity getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/history/{cardnumber}")
    public ResponseEntity getTransactionsByCard(@PathVariable String cardnumber) {
        return transactionService.getTransactionsByCard(cardnumber);
    }

    @PutMapping("/transaction")
    public ResponseEntity processTransaction(@RequestBody JsonNode node) {
        Long id = node.get("transactionId").asLong();
        String feedback = node.get("feedback").asText();
        return transactionService.updateFeedback(id, feedback);
    }


    // Suspicious IP Operations
    @GetMapping("/suspicious-ip")
    public ResponseEntity getAllSusIps() {
        return suspiciousIPService.getAll();
    }

    @PostMapping("/suspicious-ip")
    public ResponseEntity addNewIp(@RequestBody @Valid SuspiciousIP ip) {
        return suspiciousIPService.addIp(ip);
    }

    @DeleteMapping("/suspicious-ip/{ipAddress}")
    public ResponseEntity deleteIp(@PathVariable String ipAddress) {
        return suspiciousIPService.deleteByIp(ipAddress);
    }


    // Stolen Card Operations
    @GetMapping("/stolencard")
    public ResponseEntity getAllStolenCards() {
        return stolenCardService.getAll();
    }

    @PostMapping("/stolencard")
    public ResponseEntity addNewCard(@RequestBody @Valid StolenCard stolenCard) {
        return stolenCardService.addCard(stolenCard);
    }

    @DeleteMapping("/stolencard/{cardNumber}")
    public ResponseEntity deleteCard(@PathVariable String cardNumber) {
        return stolenCardService.deleteByCardNumber(cardNumber);
    }
}
