package dev.felnull.ttsvoice.core.util;

public final class StringUtils {

    public static int getComplementPoint(String target, String text) {
        target = target.toLowerCase();
        text = text.toLowerCase();

        int point = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            for (int j = 0; j < target.length(); j++) {
                char tc = target.charAt(j);
                if (c == tc)
                    point++;
            }
        }
        return point;
    }
}
