# AntiFraud-System

[Anti-Fraud System](https://hyperskill.org/projects/232) from JetBrains Academy - Challenging Difficulty

[My JetBrainsAcademy Profile](https://hyperskill.org/profile/204045764)

REST API with full intergration of Spring Boot:
- Spring Security (authorising and authenticating) 
- Spring Data (JPA using H2 DB)
- Spring Validation

System for validating payments based on amounts and potential flags for suspicious activity:
- Uses Luhn Validation for checking for erroneous inputs / fake cards
- Uses regex checking for checking entered IP addresses
- Checks transaction against known fraudulent cards
- Checks against known suspicious IPs
- Checks if same card number has been used across regions in short amount of time
- Checks if different IPs have been used with the same card number in short amount of time

System will then process the transaction and either allow, flag for manual review, or prohibit the transaction.
A support role will then be able to review the result of any transaction and change to allowed or prohibited.
System will adapt based on amount, so manually allowing transactions will increase threshold for automatic allowance, and prohibiting will decrease.

Allows Unauthorised users to:
- Register

Allows Merchants to:
- POST new transaction (and receive initial result)

Allows Support to:
- Manually review any transaction on system (only once)
- Get full list of transactions

Allows Admin to:
- Get Users registered for system
- Change role for users
- Delete users
- (Admin is restricted from business roles - i.e. reviewing transactions)
