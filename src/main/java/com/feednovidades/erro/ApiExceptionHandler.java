package com.feednovidades.erro;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduz qualquer exceção para o formato único {@link ErroResposta}.
 * Estende ResponseEntityExceptionHandler para que também os erros do próprio
 * Spring MVC (rota inexistente, método não permitido etc.) saiam no mesmo formato.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** Entrada inválida: 422 (escolha documentada no README). */
    private static final int UNPROCESSABLE = 422;

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<Object> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(404, "NAO_ENCONTRADO", ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<Object> conflito(ConflitoException ex) {
        return resposta(409, "CONFLITO", ex.getMessage(), List.of());
    }

    @ExceptionHandler(ValidacaoException.class)
    ResponseEntity<Object> validacao(ValidacaoException ex) {
        return resposta(UNPROCESSABLE, "VALIDACAO", ex.getMessage(), List.of());
    }

    /** Rede de segurança para violações de constraint não tratadas no serviço. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Object> integridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade no banco", ex);
        return resposta(409, "CONFLITO", "A operação conflita com dados já existentes", List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return resposta(500, "ERRO_INTERNO", "Erro interno do servidor", List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .sorted()
                .toList();
        return resposta(UNPROCESSABLE, "VALIDACAO", "Dados inválidos", detalhes);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return resposta(UNPROCESSABLE, "VALIDACAO", "Corpo da requisição ausente, malformado ou com tipos inválidos", List.of());
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String nome = ex.getPropertyName() != null ? ex.getPropertyName() : "parâmetro";
        return resposta(UNPROCESSABLE, "VALIDACAO", "Valor inválido para '" + nome + "'", List.of());
    }

    /** Demais erros do Spring MVC (404 de rota, 405, 415...) no mesmo formato. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String mensagem = status != null ? status.getReasonPhrase() : "Erro na requisição";
        String codigo = statusCode.value() == 404 ? "NAO_ENCONTRADO" : "REQUISICAO_INVALIDA";
        return resposta(statusCode.value(), codigo, mensagem, List.of());
    }

    private static ResponseEntity<Object> resposta(int status, String erro, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status).body(new ErroResposta(status, erro, mensagem, detalhes));
    }
}
