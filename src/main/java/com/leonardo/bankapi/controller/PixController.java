package com.leonardo.bankapi.controller;

import com.leonardo.bankapi.dto.*;
import com.leonardo.bankapi.service.PixService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pix")
public class PixController {

    private final PixService pixService;

    public PixController(PixService pixService) {
        this.pixService = pixService;
    }

    /**
     * POST /pix/keys
     * Cadastra uma nova chave PIX para uma conta.
     * Body: { "type": "EMAIL", "keyValue": "user@email.com", "accountId": "uuid" }
     */
    @PostMapping("/keys")
    @ResponseStatus(HttpStatus.CREATED)
    public PixKeyResponse registerKey(@Valid @RequestBody PixKeyRequest request) {
        return pixService.registerKey(request);
    }

    /**
     * GET /pix/keys/account/{accountId}
     * Lista todas as chaves PIX de uma conta.
     */
    @GetMapping("/keys/account/{accountId}")
    public List<PixKeyResponse> getKeysByAccount(@PathVariable String accountId) {
        return pixService.getKeysByAccount(accountId);
    }

    /**
     * DELETE /pix/keys/{keyId}
     * Remove uma chave PIX pelo ID.
     */
    @DeleteMapping("/keys/{keyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKey(@PathVariable String keyId) {
        pixService.deleteKey(keyId);
    }

    /**
     * POST /pix/send/{fromAccountId}
     * Envia PIX da conta informada para a chave PIX de destino.
     * Body: { "pixKey": "destino@email.com", "amount": 100.00, "description": "..." }
     */
    @PostMapping("/send/{fromAccountId}")
    public TransactionResponse send(@PathVariable String fromAccountId,
                                     @Valid @RequestBody PixSendRequest request) {
        return pixService.send(fromAccountId, request);
    }
}
