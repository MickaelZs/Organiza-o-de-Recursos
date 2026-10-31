package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email).filter(Usuario::getAtivo)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(senha, usuario.getSenha())) throw new InvalidCredentialsException();
        return jwtService.generateToken(usuario);
    }
}
