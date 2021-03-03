package com.chivas.wipe.utils;

import com.chivas.wipe.debug.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BindViewUtils {

    private static final Pattern PATTERN_ID = Pattern.compile("(\\()[a-zA-Z0-9._]+(\\))");
    private static final Pattern PATTERN_TYPE = Pattern.compile("( )[a-zA-Z0-9]+( )");
    private static final Pattern PATTERN_NAME = Pattern.compile("( )[a-zA-Z0-9]+(;)$");

    public static List<String> parse(String filePath, String format) {
        List<String> result = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            Log.d("file no found! (" + filePath + ")");
            return result;
        }

        BufferedReader reader = null;
        boolean isBindView = false;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String lineString;
            StringBuilder builder = new StringBuilder();

            while ((lineString = reader.readLine()) != null) {
                lineString = sub(lineString);

                if (isBindView || lineString.startsWith("@BindView")) {
                    isBindView = !lineString.endsWith(";");

                    if (!TextUtils.isEmpty(builder)) {
                        builder.append(' ');
                    }
                    builder.append(lineString);

                    if (!isBindView) {
                        result.add(assemble(builder.toString(), format));
                        builder = new StringBuilder();
                    }
                }
            }

        } catch (Exception e) {
            Log.d(e.getMessage());
        } finally {
            SafeIoUtils.safeClose(reader);
        }

        return result;
    }

    private static String assemble(String data, String format) {
        String id = null;
        String type = null;
        String name = null;
        Matcher matcher;

        if ((matcher = PATTERN_ID.matcher(data)).find()) {
            id = matcher.group()
                    .replaceAll("\\(", "")
                    .replaceAll("\\)", "")
                    .replaceAll("R2", "R");
        }
        if ((matcher = PATTERN_TYPE.matcher(data)).find()) {
            type = matcher.group()
                    .replaceAll(" ", "");
        }
        if ((matcher = PATTERN_NAME.matcher(data)).find()) {
            name = matcher.group()
                    .replaceAll(";", "")
                    .replaceAll(" ", "");
        }

        return String.format(Locale.CHINA, format, name, type, id);
    }

    private static String sub(String input) {
        int length;
        if ((length = input.length()) == 0) {
            return "";
        }

        for (int i = 0; i < length; i++) {
            if (input.charAt(i) == ' ') {
                continue;
            }

            return (i == 0) ? input : input.substring(i);
        }
        return input;
    }

}
