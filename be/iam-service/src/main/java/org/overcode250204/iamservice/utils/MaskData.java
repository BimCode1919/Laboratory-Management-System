package org.overcode250204.iamservice.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MaskData {
    public String maskData(Object data, boolean isPII) {
        if (data == null) return "<NULL>";
        String s = data.toString();
        if (s.isBlank()) return "<BLANK>";


        if (isPII) {
            if (s.contains("@")) {
                return s.replaceAll("(?<=.).(?=.*@)", "*");
            }
            if (s.matches(".*\\d{7,}.*")) {
                return s.replaceAll("\\d(?=\\d{4})", "*");
            }
        }

        if (s.length() > 50) return s.substring(0, 47) + "...";
        return s;
    }
}
