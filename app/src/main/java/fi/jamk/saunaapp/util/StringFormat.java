package fi.jamk.saunaapp.util;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

/**
 * Helper class for formatting strings
 */

public class StringFormat {
    public static String roundedKilometersShort(Locale locale, double d) {
        return String.format(locale, "%.1f", (d+.05)) +" km";
    }
}
