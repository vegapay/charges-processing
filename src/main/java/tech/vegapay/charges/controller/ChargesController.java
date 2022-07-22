package tech.vegapay.charges.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.vegapay.charges.dto.ChargesComputeRequest;
import tech.vegapay.charges.service.ChargeProcessingService;

@Slf4j
@CrossOrigin()
@RestController()
@RequestMapping("/chargesProcessing")
public class ChargesController {

    private ChargeProcessingService chargeProcessingService;

    public ChargesController(ChargeProcessingService chargeProcessingService) {
        this.chargeProcessingService = chargeProcessingService;
    }

    @PostMapping("/get")
    public ResponseEntity<Double> getCharges(@RequestBody ChargesComputeRequest charges) {
        log.info("Received Request to getCharges with Request Body {}", charges);
        try {
            return new ResponseEntity<>(chargeProcessingService.getCharges(charges), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(0D, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/applyTransactionCharges")
    public ResponseEntity<String> applyTransactionCharges(@RequestBody ChargesComputeRequest chargesComputeRequest) {
        log.info("Received Request to applyTransactionCharges with Request Body {}", chargesComputeRequest);
        try {
            return new ResponseEntity<>(chargeProcessingService.applyTransactionCharges(chargesComputeRequest), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
        }
    }
}
