package antifraud.service;

import antifraud.model.Transaction;
import antifraud.repository.TransactionRepository;
import antifraud.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    @Autowired
    TransactionValidatorService transactionValidator;
    @Autowired
    TransactionRepository transactionRepository;



    public ResponseEntity checkTransaction(Transaction transaction) {
        Map<String, String> resultMap = transactionValidator.getValidationResults(transaction);
        transaction.setResult(resultMap.get("result"));
        transactionRepository.save(transaction);
        return ResponseEntity.ok(resultMap);
    }


    public ResponseEntity getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }


    public ResponseEntity getTransactionsByCard(String cardnumber) {
        if (!Validator.isCardValid(cardnumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<Transaction> transList = transactionRepository.findAllByNumber(cardnumber);
        if (transList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(transactionRepository.findAllByNumber(cardnumber));
    }


    public ResponseEntity updateFeedback(Long transactionId, String feedback) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Check for bad request
        if (!List.of("ALLOWED", "MANUAL_PROCESSING", "PROHIBITED").contains(feedback)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Check for repeat operations
        if (transaction.getResult().equals(feedback)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // Check for unprocessed transaction
        if (!transaction.getFeedback().equals("")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        // All good - Do the thing
        return ResponseEntity.ok(transactionValidator.processReview(transaction, feedback));

    }
}
