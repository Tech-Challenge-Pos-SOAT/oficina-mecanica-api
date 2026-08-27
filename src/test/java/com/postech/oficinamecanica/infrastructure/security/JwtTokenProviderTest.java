package com.postech.oficinamecanica.infrastructure.security;

import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {
    private static final String SECRET = "unit-test-jwt-secret-key-com-pelo-menos-32-bytes-de-tamanho";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 3_600_000L);

    @Test
    void shouldGenerateNonBlankToken() {
        String token = provider.generateToken(anEmployee());

        assertThat(token).isNotBlank();
    }

    @Test
    void shouldValidateTokenItGenerated() {
        String token = provider.generateToken(anEmployee());

        assertThat(provider.isValid(token)).isTrue();
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1_000L);

        String token = expiredProvider.generateToken(anEmployee());

        assertThat(expiredProvider.isValid(token)).isFalse();
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThat(provider.isValid("isto-nao-e-um-jwt")).isFalse();
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        JwtTokenProvider otherProvider = new JwtTokenProvider(
            "outro-jwt-secret-key-completamente-diferente-com-32-bytes", 3_600_000L
        );
        String token = otherProvider.generateToken(anEmployee());

        assertThat(provider.isValid(token)).isFalse();
    }

    @Test
    void shouldRejectBlankToken() {
        assertThat(provider.isValid("")).isFalse();
    }

    private static Employee anEmployee() {
        Instant now = Instant.now();
        return new Employee(1L, "Carlos Souza", "carlos.souza@oficina.com", "hashed-password",
            EmployeeRole.ATTENDANT, EntityStatus.ACTIVE, now, now);
    }
}
