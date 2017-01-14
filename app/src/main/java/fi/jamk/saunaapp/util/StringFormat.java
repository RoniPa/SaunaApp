package fi.jamk.saunaapp.util;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

/**
 * Helper class for formatting strings
 */

public class StringFormat {
    public static String roundedKilometersShort(Context ctx, double d) {
        return String.format(getLocale(ctx), "%.1f", (d+.05)) +" km";
    }

    private static Locale getLocale(Context ctx) {
        // API level 24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ctx.getResources().getConfiguration().getLocales().get(0);
        } else {
            return ctx.getResources().getConfiguration().locale;
        }
    }
}
