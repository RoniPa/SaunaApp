package fi.jamk.saunaapp.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class for formatting strings
 */

public class StringFormat {
    public static String roundedKilometersShort(Locale locale, double d) {
        return String.format(locale, "%.1f", (d+.05)) +" km";
    }

    /**
     * Return display string for date.
     *
     *   * Same date        -> HH:mm
     *   * Same year        -> dd.MM. HH:mm
     *   * Different year   -> dd.MM.yyyy
     *
     * @param locale
     * @param date
     *
     * @return
     */
    public static String shortTime(Locale locale, Date date) {
        if (locale == null) {
            locale = Locale.getDefault();
        }

        final Calendar cal = Calendar.getInstance();
        DateFormat df;

        cal.add(Calendar.DATE, -1);
        if (date.after(cal.getTime())) {
            df = new SimpleDateFormat("HH:mm", locale);
        } else {
            cal.clear();
            cal.add(Calendar.YEAR, -1);
            if (date.after(cal.getTime())) {
                df = new SimpleDateFormat("dd.MM. HH:mm", locale);
            } else {
                df = new SimpleDateFormat("dd.MM.yyyy", locale);
            }
        }
        return df.format(date);
    }
}
