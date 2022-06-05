package antifraud.controller;

import antifraud.model.User.User;
import antifraud.repository.UserRepository;
import antifraud.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepo;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/user")
    public ResponseEntity addNewUser(@RequestBody @Valid User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return userService.addNewUser(user);
    }

    @GetMapping("/list")
    public ResponseEntity getUserList() {
        return userService.getUserList();
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity deleteUser(@PathVariable String username) {
        return userService.deleteUserByUsername(username);
    }

    @PutMapping("/role")
    public ResponseEntity setRole(@RequestBody JsonNode jsonNode) {
        String username = jsonNode.get("username").asText();
        String role = jsonNode.get("role").asText();
        return userService.changeRole(username, role);
    }

    @PutMapping("/access")
    public ResponseEntity setAccess(@RequestBody JsonNode jsonNode) {
        String username = jsonNode.get("username").asText();
        String operation = jsonNode.get("operation").asText();
        return userService.setAccountLock(username, operation);
    }


}
