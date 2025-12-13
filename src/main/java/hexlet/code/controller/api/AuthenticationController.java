package hexlet.code.controller.api;

import hexlet.code.dto.AuthRequest;
import hexlet.code.util.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.sentry.Sentry;

@RestController
@RequestMapping("/api")
public class AuthenticationController {
    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public String create(@RequestBody AuthRequest authRequest) {
        var authentication = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword());

        authenticationManager.authenticate(authentication);

        var token = jwtUtils.generateToken(authRequest.getUsername());
        return token;
    }

    @GetMapping("/sentry-test")
    public String testSentry() {
        try {
            throw new Exception("Test error for Sentry from java-project-99");
        } catch (Exception e) {
            Sentry.captureException(e);
            return "Test error sent to Sentry! Check your Issues panel.";
        }
    }
}
