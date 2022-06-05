package antifraud.service;

import antifraud.model.User.User;
import antifraud.repository.UserRepository;
import antifraud.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class UserService {

    @Autowired
    UserRepository userRepo;

    public ResponseEntity addNewUser(User user) {
        if (userRepo.existsByUsernameIgnoreCase(user.getUsername())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            if (userRepo.findAll().size() == 0) {
                user.setRole(Role.ADMINISTRATOR);
                userRepo.save(user);
            } else {
                user.setRole(Role.MERCHANT);
                user.setAccountNonLocked(false);
                userRepo.save(user);
            }
            return new ResponseEntity(user, HttpStatus.CREATED);
        }
    }

    public ResponseEntity getUserList() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    public ResponseEntity deleteUserByUsername(String username) {
        if (userRepo.existsByUsernameIgnoreCase(username)) {
            User user = userRepo.findUserByUsername(username).get();
                userRepo.delete(user);
                return ResponseEntity.ok(Map.of("username", username, "status", "Deleted successfully!"));
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity setAccountLock(String username, String operation) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getRole().equals(Role.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        switch (operation.toUpperCase()) {
            case "LOCK":
                user.setAccountNonLocked(false);
                userRepo.save(user);
                return ResponseEntity.ok(Map.of("status", "User " + username + " locked!"));
            case "UNLOCK":
                user.setAccountNonLocked(true);
                userRepo.save(user);
                return ResponseEntity.ok(Map.of("status", "User " + username + " unlocked!"));
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }


    public ResponseEntity changeRole(String username, String role) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getRole().equals(Role.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        switch (role.toUpperCase()) {
            case "MERCHANT":
                if (user.getRole().equals(Role.MERCHANT)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT);
                } else {
                    user.setRole(Role.MERCHANT);
                    userRepo.save(user);
                    return ResponseEntity.ok(user);
                }
            case "SUPPORT":
                if (user.getRole().equals(Role.SUPPORT)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT);
                } else {
                    user.setRole(Role.SUPPORT);
                    userRepo.save(user);
                    return ResponseEntity.ok(user);
                }
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
