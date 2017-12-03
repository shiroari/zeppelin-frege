package org.apache.zeppelin.haskell;

import java.util.regex.Pattern;

public class Parser {

    private static final Pattern HAS_Z_DISPLAY = Pattern.compile("(^|\\s)z_display\\s*=", Pattern.MULTILINE);

    private static final Pattern HAS_Z_MAIN = Pattern.compile("(^|\\s)z_main\\s*=", Pattern.MULTILINE);

    public static boolean hasDisplay(String script) {
        return HAS_Z_DISPLAY.matcher(script).find();
    }

    public static boolean hasMain(String script) {
        return HAS_Z_MAIN.matcher(script).find();
    }
}
