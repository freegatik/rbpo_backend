package ru.rbpo.backend.signature;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SignatureProperties.class)
public class SignatureConfig {
}
