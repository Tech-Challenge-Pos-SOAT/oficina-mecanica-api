package com.postech.oficinamecanica.domain.customer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentTest {

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725"})
    void shouldAcceptValidCpfWithOrWithoutFormatting(String value) {
        assertThat(new Document(value).unformatted()).isEqualTo("52998224725");
    }

    @Test
    void shouldFormatCpfCanonicallyRegardlessOfInputFormat() {
        assertThat(new Document("52998224725").value()).isEqualTo("529.982.247-25");
        assertThat(new Document("529.982.247-25").value()).isEqualTo("529.982.247-25");
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.222.333/0001-81", "11222333000181"})
    void shouldAcceptValidCnpjWithOrWithoutFormatting(String value) {
        assertThat(new Document(value).unformatted()).isEqualTo("11222333000181");
    }

    @Test
    void shouldFormatCnpjCanonicallyRegardlessOfInputFormat() {
        assertThat(new Document("11222333000181").value()).isEqualTo("11.222.333/0001-81");
    }

    @Test
    void shouldTreatDifferentlyFormattedDocumentsAsEqual() {
        assertThat(new Document("123.456.789-09")).isEqualTo(new Document("12345678909"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"111.111.111-11", "529.982.247-26", "123", ""})
    void shouldRejectInvalidCpf(String value) {
        assertThatThrownBy(() -> new Document(value))
            .isInstanceOf(InvalidDocumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.111.111/1111-11", "11.222.333/0001-82"})
    void shouldRejectInvalidCnpj(String value) {
        assertThatThrownBy(() -> new Document(value))
            .isInstanceOf(InvalidDocumentException.class);
    }

    @Test
    void shouldRejectNullDocument() {
        assertThatThrownBy(() -> new Document(null))
            .isInstanceOf(InvalidDocumentException.class);
    }
}
