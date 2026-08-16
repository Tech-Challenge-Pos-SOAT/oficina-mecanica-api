package com.postech.oficinamecanica.domain.vehicle;

import java.util.regex.Pattern;

public record Plate(String value) {

    private static final Pattern OLD_FORMAT = Pattern.compile("^[A-Z]{3}[0-9]{4}$");
    private static final Pattern MERCOSUL_FORMAT = Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");

    public Plate {
        String raw = value == null ? "" : value.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (!OLD_FORMAT.matcher(raw).matches() && !MERCOSUL_FORMAT.matcher(raw).matches()) {
            throw new InvalidPlateException(value);
        }
        value = format(raw);
    }

    public String unformatted() {
        return value.replaceAll("[^A-Z0-9]", "");
    }

    private static String format(String raw) {
        if (OLD_FORMAT.matcher(raw).matches()) {
            return raw.substring(0, 3) + "-" + raw.substring(3);
        }
        return raw;
    }
}
