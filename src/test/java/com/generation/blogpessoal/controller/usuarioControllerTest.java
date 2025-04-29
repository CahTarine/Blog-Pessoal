package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class usuarioControllerTest {
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@BeforeAll
	void start() {
		
		usuarioRepository.deleteAll(); //limpa tudo do bd virtual.
		
		usuarioService.cadastrarUsuario(new Usuario(null, "root", "root@root.com", "rootroot", "-"));
		
	}
	
	@Test
	@DisplayName("Cadastrar Usuario")
	public void deveCriarUmUsuario() {
		
		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(new Usuario(null, "camille", "camille@root.com", 
				"123456789", "-")); // igual preenchimento do Json no Insomnia.
		
		ResponseEntity<Usuario> corpoResposta = testRestTemplate.exchange("/usuarios/cadastrar", HttpMethod.POST, 
				corpoRequisicao, Usuario.class);
		
		assertEquals(HttpStatus.CREATED, corpoResposta.getStatusCode());
	}
	
	@Test
	@DisplayName("Nao deve permitir duplicacao do Usuario")
	public void naoDeveDuplicarUsuario() {
		
		usuarioService.cadastrarUsuario(new Usuario(null, "maria", "maria@root.com", "123456789", "-"));
		
		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(new Usuario(null, "maria", "maria@root.com", 
				"123456789", "-"));		
		
		ResponseEntity<Usuario> corpoResposta = testRestTemplate.exchange("/usuarios/cadastrar", HttpMethod.POST, 
				corpoRequisicao, Usuario.class);
		
		assertEquals(HttpStatus.BAD_REQUEST, corpoResposta.getStatusCode());
	}
	
	@Test
	@DisplayName("Atualizar um Usuario")
	public void deveAtualizarUmUsuario() {
		
		Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(new Usuario(null, "juliana", "juliana@root.com", "123456789", "-"));
	
		Usuario usuarioUpdate = new Usuario(usuarioCadastrado.get().getId(),
				"Juliana Ramos", "juliana_ramos@root.com", "juh12345", "-");
		
		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(usuarioUpdate);
		
		ResponseEntity<Usuario> corpoResposta = testRestTemplate.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/atualizar", HttpMethod.PUT, corpoRequisicao, Usuario.class);
		
		assertEquals(HttpStatus.OK, corpoResposta.getStatusCode());
	
	
	}
	
	@Test
	@DisplayName("Listar todos os Usuarios")
	public void deveMostrarTodosUsuarios() {
		
		usuarioService.cadastrarUsuario(new Usuario(null, "sabrina", "sabrina@root.com", "123456789", "-"));
		
		usuarioService.cadastrarUsuario(new Usuario(null, "ricardo", "ricardo@root.com", "123456789", "-"));
		
		ResponseEntity<String> resposta = testRestTemplate
				.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/all", HttpMethod.GET, null, String.class);
		
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
	
	
	}
	
	
	
	
	
	
}
