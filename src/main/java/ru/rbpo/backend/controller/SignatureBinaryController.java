package ru.rbpo.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import ru.rbpo.backend.binary.MultipartMixedResponseFactory;
import ru.rbpo.backend.dto.MalwareSignatureIdsRequest;
import ru.rbpo.backend.service.SignatureBinaryExportService;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/** Бинарная выдача сигнатур (multipart/mixed: manifest.bin + data.bin). */
@RestController
@RequestMapping("/api/binary/signatures")
public class SignatureBinaryController {

    private final SignatureBinaryExportService binaryExportService;

    public SignatureBinaryController(SignatureBinaryExportService binaryExportService) {
        this.binaryExportService = binaryExportService;
    }

    @GetMapping("/full")
    public ResponseEntity<MultiValueMap<String, Object>> full() {
        SignatureBinaryExportService.BinaryExportPayload p = binaryExportService.exportFull();
        return MultipartMixedResponseFactory.create(p.manifestBin(), p.dataBin());
    }

    @GetMapping("/increment")
    public ResponseEntity<MultiValueMap<String, Object>> increment(@RequestParam(required = false) String since) {
        Instant sinceInstant = parseSinceMandatory(since);
        SignatureBinaryExportService.BinaryExportPayload p = binaryExportService.exportIncrement(sinceInstant);
        return MultipartMixedResponseFactory.create(p.manifestBin(), p.dataBin());
    }

    @PostMapping("/by-ids")
    public ResponseEntity<MultiValueMap<String, Object>> byIds(@Valid @RequestBody MalwareSignatureIdsRequest request) {
        SignatureBinaryExportService.BinaryExportPayload p = binaryExportService.exportByIds(request.getIds());
        return MultipartMixedResponseFactory.create(p.manifestBin(), p.dataBin());
    }

    private static Instant parseSinceMandatory(String since) {
        if (since == null || since.isBlank()) {
            throw new IllegalArgumentException("Параметр since обязателен");
        }
        try {
            return Instant.parse(since);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Параметр since должен быть в формате ISO-8601 (например, 2025-03-17T00:00:00Z)");
        }
    }
}
