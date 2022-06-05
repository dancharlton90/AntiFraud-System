package antifraud.service;

import antifraud.enums.Region;
import antifraud.model.Transaction;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIPRepository;
import antifraud.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TransactionValidatorService {

    @Autowired
    SuspiciousIPRepository suspiciousIPRepository;
    @Autowired
    StolenCardRepository stolenCardRepository;
    @Autowired
    TransactionRepository transactionRepository;

    // TODO - Need to save these variables and load on app start?
    // Maybe inject value holding entity and use values from that object w/ save on update?
    // Find out how to load from database on initialisation?

    private int maxAllowed = 200;
    private int maxManual = 1500;

    public Map<String, String> getValidationResults(Transaction transaction) {

        System.out.println("[DEBUG]Validation:");
        System.out.println("[DEBUG]maxALLOWED: " + maxAllowed);
        System.out.println("[DEBUG]maxMANUAL: " + maxManual);


        List<Transaction> transactionList =
                transactionRepository.findByDateBetween(transaction.getDate().minusHours(1), transaction.getDate());
        int ipCorrelationCount = getIpCorrelationCount(transaction, transactionList);
        int regionCorrelationCount = getRegionCorrelationCount(transaction, transactionList);

        // First check for any prohibited reasons:
        List<String> results = transactionProhibitedCheck(transaction, ipCorrelationCount, regionCorrelationCount);
        if (!results.isEmpty()) {
            return Map.of(
                    "result", "PROHIBITED",
                    "info", String.join(", ", results));
        }

        // Second check for any manual processing reasons:
        results = transactionManualProcessingCheck(transaction, ipCorrelationCount, regionCorrelationCount);
        if (!results.isEmpty()) {
            return Map.of(
                    "result", "MANUAL_PROCESSING",
                    "info", String.join(", ", results));
        }

        // Return ALLOWED if no reasons to deny:
        return Map.of(
                "result", "ALLOWED",
                "info", "none");
    }



    // PROHIBITED - Checks amount, stolen cards, suspicious IPs, IP correlation, region correlation
    private List<String> transactionProhibitedCheck(Transaction transaction, int ipCorrCount, int regionCorrCount) {
        List<String> reasons = new ArrayList<>();

        // Check amount > 1500 = PROHIBITED
        if (transaction.getAmount() > maxManual) {
            reasons.add("amount");
        }

        // Check for stolen card
        if (stolenCardRepository.existsStolencardByNumber(transaction.getNumber())) {
            reasons.add("card-number");
        }

        // Check for suspicious IP
        if (suspiciousIPRepository.existsSuspiciousipByIp(transaction.getIp())) {
            reasons.add("ip");
        }

        // Check for IP correlation - 3 or more UNIQUE IP addresses for same card number in last hour.
        if (ipCorrCount >= 3) {
            reasons.add("ip-correlation");
        }

        // Check for Region correlation - 3 or more UNIQUE regions for same card number in last hour.
        if (regionCorrCount >= 3) {
            reasons.add("region-correlation");
        }
        return reasons;
    }

    // MANUAL_PROCESSING - Checks amount, IP correlation, region correlation
    private List<String> transactionManualProcessingCheck(
            Transaction transaction, int ipCorrCount, int regionCorrCount) {
        List<String> reasons = new ArrayList<>();

        // Check amount > 200 AND amount <= 1500
        if (transaction.getAmount() > maxAllowed && transaction.getAmount() <= maxManual) {
            reasons.add("amount");
        }

        // Check for IP correlation - 2 UNIQUE IP addresses for same card number in last hour.
        if (ipCorrCount == 2) {
            reasons.add("ip-correlation");
        }

        // Check for region correlation - 2  UNIQUE regions for same card number in last hour.
        if (regionCorrCount == 2) {
            reasons.add("region-correlation");
        }
        return reasons;
    }


    private int getIpCorrelationCount(Transaction transaction, List<Transaction> transactionList) {
        int ipCorrelationCount = 0;
        if (!transactionList.isEmpty()) {
            List<String> foundIps = new ArrayList<>();
            for (Transaction transInList : transactionList) {
                if (!transaction.getIp().equals(transInList.getIp()) && !foundIps.contains(transInList.getIp())) {
                    foundIps.add(transInList.getIp());
                    ipCorrelationCount++;
                }
            }
        }
        return ipCorrelationCount;
    }

    private int getRegionCorrelationCount(Transaction transaction, List<Transaction> transactionList) {
        int regionCorrelationCount = 0;
        if (!transactionList.isEmpty()) {
            List<Region> foundRegions = new ArrayList<>();
            for (Transaction transInList : transactionList) {
                if (!transaction.getRegion().equals(transInList.getRegion()) && !foundRegions.contains(transInList.getRegion())) {
                    foundRegions.add(transInList.getRegion());
                    regionCorrelationCount++;
                }
            }
        }
        return regionCorrelationCount;
    }


    // TODO - Do something better with this mess of a method
    public Transaction processReview(Transaction transaction, String feedback) {
        switch (transaction.getResult()) {
            // Initial transaction was:
            case "ALLOWED":
                switch (feedback) {
                    // and the feedback is:
                    case "MANUAL_PROCESSING":
                        transaction.setFeedback("MANUAL_PROCESSING");
                        transactionRepository.save(transaction);
                        maxAllowed = getDecreasedLimit("ALLOWED", transaction.getAmount());
                        // TODO Work out how to save Validator service to DB for persistence.
                        return transaction;
                    // and the feedback is:
                    case "PROHIBITED":
                        transaction.setFeedback("PROHIBITED");
                        transactionRepository.save(transaction);
                        maxAllowed = getDecreasedLimit("ALLOWED", transaction.getAmount());
                        maxManual = getDecreasedLimit("MANUAL", transaction.getAmount());
                        // TODO Work out how to save Validator service to DB for persistence.
                        return transaction;
                    default:
                        throw new IllegalArgumentException("feedback switch -> allowed default case");
                }
            // Initial transaction was:
            case "MANUAL_PROCESSING":
                switch (feedback) {
                    // and the feedback is:
                    case "ALLOWED":
                        transaction.setFeedback("ALLOWED");
                        transactionRepository.save(transaction);
                        maxAllowed = getIncreasedLimit("ALLOWED", transaction.getAmount());
                        // TODO Work out how to save Validator service to DB for persistence.
                        return transaction;
                    // and the feedback is:
                    case "PROHIBITED":
                        transaction.setFeedback("PROHIBITED");
                        transactionRepository.save(transaction);
                        maxManual = getDecreasedLimit("MANUAL", transaction.getAmount());
                        // TODO Work out how to save Validator service to DB for persistence.
                        return transaction;
                    default:
                        throw new IllegalArgumentException("feedback switch -> manual_processing default case");
                }
            // Initial transaction was:
            case "PROHIBITED":
                switch (feedback) {
                    // and the feedback is:
                    case "ALLOWED":
                        transaction.setFeedback("ALLOWED");
                        transactionRepository.save(transaction);
                        maxAllowed = getIncreasedLimit("ALLOWED", transaction.getAmount());
                        maxManual = getIncreasedLimit("MANUAL", transaction.getAmount());
                        // TODO Work out how to save Validator service to DB for persistence.
                        return transaction;
                    // and the feedback is:
                    case "MANUAL_PROCESSING":
                        transaction.setFeedback("MANUAL_PROCESSING");
                        transactionRepository.save(transaction);
                        maxManual = getIncreasedLimit("MANUAL", transaction.getAmount());
                        // TODO Work out how to save Validator service to DB for persistence.
                        return transaction;
                    default:
                        throw new IllegalArgumentException("feedback switch -> prohibited default case");
                }
            default:
                throw new IllegalArgumentException("processReview Default case");
        }
    }

    private int getIncreasedLimit(String allowedOrManual, long valueFromTrans) {
        switch (allowedOrManual) {
            case "ALLOWED":
                return (int) Math.ceil(0.8 * maxAllowed + 0.2 * valueFromTrans);
            case "MANUAL":
                return (int) Math.ceil(0.8 * maxManual + 0.2 * valueFromTrans);
            default:
                throw new IllegalArgumentException("getIncreasedLimit Default case");
        }
    }

    private int getDecreasedLimit(String allowedOrManual, long valueFromTrans) {
        switch (allowedOrManual) {
            case "ALLOWED":
                return (int) Math.ceil(0.8 * maxAllowed - 0.2 * valueFromTrans);
            case "MANUAL":
                return (int) Math.ceil(0.8 * maxManual - 0.2 * valueFromTrans);
            default:
                throw new IllegalArgumentException("getDecreasedLimit Default case");
        }
    }
}
