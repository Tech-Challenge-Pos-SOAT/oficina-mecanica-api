package com.postech.oficinamecanica.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordEncoderAdapterTest {
    private final BCryptPasswordEncoderAdapter encoder = new BCryptPasswordEncoderAdapter();

    @Test
    void shouldEncodePasswordAsBcryptHash() {
        String hash = encoder.encode("senha123");

        assertThat(hash).isNotBlank();
        assertThat(hash).isNotEqualTo("senha123");
        assertThat(new BCryptPasswordEncoder().matches("senha123", hash)).isTrue();
    }

    @Test
    void shouldProduceDifferentHashesForSamePasswordDueToSalt() {
        String hash1 = encoder.encode("senha123");
        String hash2 = encoder.encode("senha123");

        assertThat(hash1).isNotEqualTo(hash2);
    }
}
