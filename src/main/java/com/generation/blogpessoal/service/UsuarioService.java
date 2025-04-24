package com.generation.blogpessoal.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.model.UsuarioLogin;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.security.JwtService;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository; //acesso ao banco de dados.
	
	@Autowired
	private JwtService jwtService; //acesso para gerar o token.
	
	@Autowired
	private AuthenticationManager authenticationManager; //verifica autenticidade do usuario;
	
	
	
	
	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {
		
		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
			return Optional.empty();
		
		usuario.setSenha(criptografarSenha(usuario.getSenha()));
		
		return Optional.of(usuarioRepository.save(usuario));
		
	}
	
	
	
	
	public Optional<Usuario> atualizarUsuario(Usuario usuario) {
		
		if (usuarioRepository.findById(usuario.getId()).isPresent()){
			
			Optional<Usuario> buscarUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());
			
			if ((buscarUsuario.isPresent()) && (buscarUsuario.get().getId()) != usuario.getId())
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe!");
		
			usuario.setSenha(criptografarSenha(usuario.getSenha()));
			
			return Optional.ofNullable(usuarioRepository.save(usuario));
		
		}
		
		return Optional.empty();
	}
	
	
	
	
	public Optional<UsuarioLogin> autenticarUsuario(Optional<UsuarioLogin> usuarioLogin) {
		
		// gera o objeto de autenticacao.
		var credenciais = new UsernamePasswordAuthenticationToken(usuarioLogin.get().getUsuario(), usuarioLogin.get().getSenha());
	
		// autentica o usuário.
		Authentication authentication = authenticationManager.authenticate(credenciais);
		
		//se a autenticacao foi efetuada com sucesso:
		if (authentication.isAuthenticated()) {
			
			// busca os dados do usuário
			Optional<Usuario> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());
			
			//se o usuario foi encontrado
			if (usuario.isPresent()) {
				
				//preenche o objeto usuarioLogin com os dados encontrados.
				usuarioLogin.get().setId(usuario.get().getId());
				usuarioLogin.get().setNome(usuario.get().getNome());
				usuarioLogin.get().setFoto(usuario.get().getFoto());
				usuarioLogin.get().setToken(gerarToken(usuarioLogin.get().getUsuario()));
				usuarioLogin.get().setSenha("");
				
				// retorna o objeto preenchido.
				
				return usuarioLogin;
			}
			
		}
		
		return Optional.empty();
	
	}
	
	
	
	
	
	private String criptografarSenha(String senha) {
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); // instancia do objeto responsavel por criptograffar a senha.
		
		return encoder.encode(senha);
	}
	
	
	
	
	
	private String gerarToken(String usuario) {
		
		return "Bearer " + jwtService.generateToken(usuario);
	}
	
}
