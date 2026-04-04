package com.senol.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter[] FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ISO_LOCAL_DATE
    };

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null) return null;
        text = text.trim();
        if (text.isEmpty()) return null;

        // time-only like HH:mm or HH:mm:ss
        if (text.matches("^\\d{2}:\\d{2}(:\\d{2})?$")) {
            try {
                if (text.length() == 5) { // HH:mm
                    LocalTime t = LocalTime.parse(text, DateTimeFormatter.ofPattern("HH:mm"));
                    return LocalDate.now().atTime(t);
                } else { // HH:mm:ss
                    LocalTime t = LocalTime.parse(text, DateTimeFormatter.ofPattern("HH:mm:ss"));
                    return LocalDate.now().atTime(t);
                }
            } catch (DateTimeParseException ignored) {}
        }

        for (DateTimeFormatter f : FORMATTERS) {
            try {
                if (f == DateTimeFormatter.ISO_LOCAL_DATE) {
                    LocalDate d = LocalDate.parse(text, f);
                    return d.atStartOfDay();
                } else {
                    return LocalDateTime.parse(text, f);
                }
            } catch (DateTimeParseException ignored) {
            }
        }
        // Fallback: try default ISO
        try {
            return LocalDateTime.parse(text);
        } catch (Exception e) {
            throw ctxt.weirdStringException(text, LocalDateTime.class, "Unparseable LocalDateTime");
        }
    }
}
