package ru.rbpo.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rbpo.backend.binary.*;
import ru.rbpo.backend.exception.InvalidSignatureDataException;
import ru.rbpo.backend.model.MalwareSignature;
import ru.rbpo.backend.model.SignatureStatus;
import ru.rbpo.backend.repository.MalwareSignatureRepository;
import ru.rbpo.backend.signature.SignatureService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Бинарный экспорт сигнатур: manifest.bin + data.bin, подпись манифеста байтовым методом ЭЦП. */
@Service
public class SignatureBinaryExportService {

    private final MalwareSignatureRepository signatureRepository;
    private final SignatureService signatureService;
    private final String studentMagicSuffix;

    public SignatureBinaryExportService(MalwareSignatureRepository signatureRepository,
                                       SignatureService signatureService,
                                       @Value("${rbpo.binary-format.student-surname:RBPO}") String studentMagicSuffix) {
        this.signatureRepository = signatureRepository;
        this.signatureService = signatureService;
        this.studentMagicSuffix = studentMagicSuffix != null ? studentMagicSuffix.trim() : "RBPO";
    }

    public BinaryExportPayload exportFull() {
        List<MalwareSignature> records = signatureRepository.findByStatus(SignatureStatus.ACTUAL).stream()
                .sorted(Comparator.comparing(MalwareSignature::getId))
                .toList();
        return pack(records, SignatureBinaryFormat.EXPORT_FULL, Instant.now().toEpochMilli(),
                SignatureBinaryFormat.SINCE_NOT_APPLICABLE);
    }

    public BinaryExportPayload exportIncrement(Instant since) {
        if (since == null) {
            throw new InvalidSignatureDataException("Параметр since обязателен");
        }
        List<MalwareSignature> records = signatureRepository.findByUpdatedAtAfter(since).stream()
                .sorted(Comparator.comparing(MalwareSignature::getId))
                .toList();
        return pack(records, SignatureBinaryFormat.EXPORT_INCREMENT, Instant.now().toEpochMilli(),
                since.toEpochMilli());
    }

    public BinaryExportPayload exportByIds(List<UUID> ids) {
        List<MalwareSignature> records;
        if (ids == null || ids.isEmpty()) {
            records = List.of();
        } else {
            records = signatureRepository.findByIdIn(ids).stream()
                    .sorted(Comparator.comparing(MalwareSignature::getId))
                    .toList();
        }
        return pack(records, SignatureBinaryFormat.EXPORT_BY_IDS, Instant.now().toEpochMilli(),
                SignatureBinaryFormat.SINCE_NOT_APPLICABLE);
    }

    private BinaryExportPayload pack(List<MalwareSignature> records, int exportType, long generatedAt,
                                     long sinceEpochMillis) {
        DataBinBuildResult dataBin = SignatureDataBinSerializer.build(studentMagicSuffix, records);

        List<byte[]> recordSigs = new ArrayList<>();
        for (MalwareSignature r : records) {
            recordSigs.add(ManifestBinSerializer.decodeRecordSignatureBase64(r.getDigitalSignatureBase64()));
        }

        byte[] unsignedManifest = ManifestBinSerializer.buildUnsignedManifest(
                studentMagicSuffix,
                exportType,
                generatedAt,
                sinceEpochMillis,
                dataBin.sha256(),
                records,
                dataBin.recordPayloadLengths(),
                recordSigs);

        byte[] manifestSig = signatureService.signBytes(unsignedManifest);
        byte[] manifestBin = ManifestBinSerializer.appendManifestSignature(unsignedManifest, manifestSig);
        return new BinaryExportPayload(manifestBin, dataBin.fullBytes());
    }

    public record BinaryExportPayload(byte[] manifestBin, byte[] dataBin) {}
}
