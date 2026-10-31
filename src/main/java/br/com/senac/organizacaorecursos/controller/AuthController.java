package br.com.senac.organizacaorecursos.controller;

import br.com.senac.organizacaorecursos.dto.LoginRequest;
import br.com.senac.organizacaorecursos.dto.LoginResponse;
import br.com.senac.organizacaorecursos.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(new LoginResponse(authService.login(request.email(), request.senha()), "Bearer"));
    }
}
