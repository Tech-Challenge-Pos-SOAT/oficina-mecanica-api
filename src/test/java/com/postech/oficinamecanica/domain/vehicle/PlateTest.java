package com.postech.oficinamecanica.domain.vehicle;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlateTest {

    @ParameterizedTest
    @ValueSource(strings = {"ABC-1234", "ABC1234"})
    void shouldAcceptValidOldFormatPlateWithOrWithoutDash(String value) {
        assertThat(new Plate(value).unformatted()).isEqualTo("ABC1234");
    }

    @Test
    void shouldFormatOldPlateCanonicallyWithDash() {
        assertThat(new Plate("ABC1234").value()).isEqualTo("ABC-1234");
        assertThat(new Plate("ABC-1234").value()).isEqualTo("ABC-1234");
    }

    @Test
    void shouldAcceptValidMercosulPlate() {
        assertThat(new Plate("abc1d23").value()).isEqualTo("ABC1D23");
    }

    @Test
    void shouldTreatDifferentlyFormattedPlatesAsEqual() {
        assertThat(new Plate("ABC-1234")).isEqualTo(new Plate("ABC1234"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB-1234", "ABCD-1234", "ABC-123", "123-ABCD", ""})
    void shouldRejectInvalidPlate(String value) {
        assertThatThrownBy(() -> new Plate(value))
            .isInstanceOf(InvalidPlateException.class);
    }

    @Test
    void shouldRejectNullPlate() {
        assertThatThrownBy(() -> new Plate(null))
            .isInstanceOf(InvalidPlateException.class);
    }
}
