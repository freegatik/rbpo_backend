package ru.rbpo.backend.binary;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Ответ multipart/mixed: manifest.bin затем data.bin (см. multipart.md). */
public final class MultipartMixedResponseFactory {

    private MultipartMixedResponseFactory() {}

    public static ResponseEntity<MultiValueMap<String, Object>> create(byte[] manifestBytes, byte[] dataBytes) {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("manifest", createPart("manifest.bin", manifestBytes));
        body.add("data", createPart("data.bin", dataBytes));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("multipart/mixed"));
        return ResponseEntity.ok().headers(headers).body(body);
    }

    private static HttpEntity<ByteArrayResource> createPart(String filename, byte[] content) {
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        partHeaders.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        partHeaders.setContentLength(content.length);

        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        return new HttpEntity<>(resource, partHeaders);
    }
}
