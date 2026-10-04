package com.eshopper.catalog.brand;

import java.text.Normalizer;
import java.util.Locale;

final class Slugs {

    private Slugs() {}

    public static String from(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
