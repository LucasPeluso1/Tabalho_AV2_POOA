package edu.ucsal.fiadopay.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@RestController
@RequestMapping("/fiadopay/webhook")
public class WebhookCallbackController {

    private static final Logger log = LoggerFactory.getLogger(WebhookCallbackController.class);

    @Value("${fiadopay.webhook-secret}")
    private String secret;

    @PostMapping("/test")
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature) {

        log.info("--- [Webhook Sink] Recebendo notificação ---");
        
        if (signature == null) {
            log.warn("[Webhook Sink] Assinatura ausente. Rejeitando.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Signature missing");
        }

        
        String calculatedHmac = hmac(payload, secret);
        if (!calculatedHmac.equals(signature)) {
            log.error("[Webhook Sink] Assinatura INVÁLIDA! Recebido: {}, Calculado: {}", signature, calculatedHmac);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Signature");
        }

        log.info("[Webhook Sink] Assinatura VÁLIDA. Payload: {}", payload);
        
        
        return ResponseEntity.ok("Webhook received and verified");
    }

    private String hmac(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular HMAC", e);
        }
    }
}