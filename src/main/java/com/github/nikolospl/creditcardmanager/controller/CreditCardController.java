package com.github.nikolospl.creditcardmanager.controller;

import com.github.nikolospl.creditcardmanager.dto.CreateCardRequest;
import com.github.nikolospl.creditcardmanager.dto.CreditCardResponse;
import com.github.nikolospl.creditcardmanager.dto.PayRequest;
import com.github.nikolospl.creditcardmanager.service.CreditCardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@CrossOrigin(origins = "http://localhost:4200")
public class CreditCardController {
    private final CreditCardService cardService;

    public CreditCardController(CreditCardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CreditCardResponse> createCard(@Valid @RequestBody CreateCardRequest request){
        CreditCardResponse response = cardService.issueCard(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<CreditCardResponse> getCardById(@PathVariable UUID id){
        CreditCardResponse response = cardService.getCardById(id);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/block")
    public ResponseEntity<CreditCardResponse> blockCard(@PathVariable UUID id){
        CreditCardResponse response = cardService.blockCard(id);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/unblock")
    public ResponseEntity<CreditCardResponse> unblockCard(@PathVariable UUID id){
        CreditCardResponse response = cardService.unblockCard(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<CreditCardResponse> processPayment(@PathVariable UUID id, @Valid @RequestBody PayRequest request){
        CreditCardResponse response = cardService.processPayment(id,request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/repay")
    public ResponseEntity<CreditCardResponse> repayDebt(@PathVariable UUID id, @Valid @RequestBody PayRequest request){
        CreditCardResponse response = cardService.repayDebt(id,request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/changeLimit")
    public ResponseEntity<CreditCardResponse> changeLimit(@PathVariable UUID id, @Valid @RequestBody PayRequest request){
        CreditCardResponse response = cardService.changeLimit(id, request);
        return ResponseEntity.ok(response);
    }
}
