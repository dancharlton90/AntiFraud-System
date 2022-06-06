package antifraud.service;

import antifraud.enums.Region;
import antifraud.model.Transaction;
import antifraud.model.ValidatorConfig;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIPRepository;
import antifraud.repository.TransactionRepository;
import antifraud.repository.ValidatorConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionValidatorService {

    @Autowired
    SuspiciousIPRepository suspiciousIPRepository;
    @Autowired
    StolenCardRepository stolenCardRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    ValidatorConfigRepository validatorConfigRepository;
    @Autowired
    ValidatorConfig config;

    // TODO - Find cleaner way to store ValidatorService variables than injecting config entity?
    // TODO - Refactor ALLOWED, MANUAL_PROCESSING, PROHIBITED to enums.

    @Bean
    public ValidatorConfig getConfig() {
        Optional<ValidatorConfig> configOptional = validatorConfigRepository.findById(1);
        if (configOptional.isEmpty()) {
            config = new ValidatorConfig(1, 200, 1500);
            return config;
        } else {
            return configOptional.get();
        }
    }


    public Map<String, String> getValidationResults(Transaction transaction) {

        List<Transaction> transactionList =
                transactionRepository.findByDateBetween(transaction.getDate().minusHours(1), transaction.getDate());
        int ipCorrelationCount = getIpCorrelationCount(transaction, transactionList);
        int regionCorrelationCount = getRegionCorrelationCount(transaction, transactionList);

        // First check for any prohibited reasons and return if !isEmpty:
        List<String> results = transactionProhibitedCheck(transaction, ipCorrelationCount, regionCorrelationCount);
        if (!results.isEmpty()) {
            return Map.of(
                    "result", "PROHIBITED",
                    "info", String.join(", ", results));
        }

        // Second check for any manual processing reasons and return if !isEmpty:
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
        if (transaction.getAmount() > config.getMaxManual()) {
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
        if (transaction.getAmount() > config.getMaxAllowed() && transaction.getAmount() <= config.getMaxManual()) {
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


    // TODO - Refactor this mess
    public Transaction processReview(Transaction transaction, String feedback) {
        switch (transaction.getResult()) {
            // Initial transaction was:
            case "ALLOWED":
                switch (feedback) {
                    // and the feedback is:
                    case "MANUAL_PROCESSING":
                        transaction.setFeedback("MANUAL_PROCESSING");
                        transactionRepository.save(transaction);
                        config.setMaxAllowed(getDecreasedLimit("ALLOWED", transaction.getAmount()));
                        validatorConfigRepository.save(config);
                        return transaction;
                    // and the feedback is:
                    case "PROHIBITED":
                        transaction.setFeedback("PROHIBITED");
                        transactionRepository.save(transaction);
                        config.setMaxAllowed(getDecreasedLimit("ALLOWED", transaction.getAmount()));
                        config.setMaxManual(getDecreasedLimit("MANUAL", transaction.getAmount()));
                        validatorConfigRepository.save(config);
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
                        config.setMaxAllowed(getIncreasedLimit("ALLOWED", transaction.getAmount()));
                        validatorConfigRepository.save(config);
                        return transaction;
                    // and the feedback is:
                    case "PROHIBITED":
                        transaction.setFeedback("PROHIBITED");
                        transactionRepository.save(transaction);
                        config.setMaxManual(getDecreasedLimit("MANUAL", transaction.getAmount()));
                        validatorConfigRepository.save(config);
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
                        config.setMaxAllowed(getIncreasedLimit("ALLOWED", transaction.getAmount()));
                        config.setMaxManual(getIncreasedLimit("MANUAL", transaction.getAmount()));
                        validatorConfigRepository.save(config);
                        return transaction;
                    // and the feedback is:
                    case "MANUAL_PROCESSING":
                        transaction.setFeedback("MANUAL_PROCESSING");
                        transactionRepository.save(transaction);
                        config.setMaxManual(getIncreasedLimit("MANUAL", transaction.getAmount()));
                        validatorConfigRepository.save(config);
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
                return (int) Math.ceil(0.8 * config.getMaxAllowed() + 0.2 * valueFromTrans);
            case "MANUAL":
                return (int) Math.ceil(0.8 * config.getMaxManual() + 0.2 * valueFromTrans);
            default:
                throw new IllegalArgumentException("getIncreasedLimit Default case");
        }
    }

    private int getDecreasedLimit(String allowedOrManual, long valueFromTrans) {
        switch (allowedOrManual) {
            case "ALLOWED":
                return (int) Math.ceil(0.8 * config.getMaxAllowed() - 0.2 * valueFromTrans);
            case "MANUAL":
                return (int) Math.ceil(0.8 * config.getMaxManual() - 0.2 * valueFromTrans);
            default:
                throw new IllegalArgumentException("getDecreasedLimit Default case");
        }
    }
}
