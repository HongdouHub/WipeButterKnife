package com.chivas.wipe.utils;

import com.chivas.wipe.bean.FileParseBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chivas.wipe.constants.ButterKnifeConstants.BIND_VIEW;
import static com.chivas.wipe.constants.ButterKnifeConstants.ONCLICK;

public class JavaFileParseUtils {

    private JavaFileParseUtils() {
        //
    }

    public static Map<String, List<FileParseBean>> parse(String filePath) {
        Map<String, List<FileParseBean>> result = new HashMap<>();
        List<FileParseBean> bindViewList = new ArrayList<>();
        List<FileParseBean> onClickList = new ArrayList<>();

        result.put(BIND_VIEW, bindViewList);
        result.put(ONCLICK, onClickList);

        File file = new File(filePath);
        if (!file.exists()) {
            Log.d("file no found! (" + filePath + ")");
            return result;
        }

        // boolean isBindView = false;
        // boolean isOnclick = false;
        boolean[] statementFlag = new boolean[2];
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String lineString;
            StringBuilder bindViewBuilder = new StringBuilder();
            StringBuilder onClickBuilder = new StringBuilder();

            while ((lineString = reader.readLine()) != null) {
                lineString = sub(lineString);

                parseBindView(bindViewList, statementFlag, lineString, bindViewBuilder);
                parseOnClick(onClickList, statementFlag, lineString, onClickBuilder);
            }
        } catch (Exception e) {
            Log.d(e.getMessage());
        } finally {
            SafeIoUtils.safeClose(reader);
        }
        return result;
    }

    private static void parseBindView(List<FileParseBean> bindViewList, boolean[] statementFlag,
                                               String lineString, StringBuilder bindViewBuilder) {
        if (statementFlag[0] || lineString.startsWith(BIND_VIEW)) {
            statementFlag[0] = !lineString.endsWith(";");

            if (!TextUtils.isEmpty(bindViewBuilder)) {
                bindViewBuilder.append(' ');
            }
            bindViewBuilder.append(lineString);

            if (!statementFlag[0]) {
                bindViewList.add(assembleBindView(bindViewBuilder.toString()));
                bindViewBuilder.setLength(0);
            }
        }
    }

    private static void parseOnClick(List<FileParseBean> onClickList, boolean[] statementFlag,
                                              String lineString, StringBuilder onClickBuilder) {
        if (statementFlag[1] || lineString.startsWith(ONCLICK)) {
            statementFlag[1] = !lineString.endsWith("{");

            if (!TextUtils.isEmpty(onClickBuilder)) {
                onClickBuilder.append(' ');
            }
            onClickBuilder.append(lineString);

            if (!statementFlag[1]) {
                onClickList.add(assembleOnClick(onClickBuilder.toString()));
                onClickBuilder.setLength(0);
            }
        }
    }

    private static final Pattern PATTERN_ID = Pattern.compile("(\\()[a-zA-Z0-9._]+(\\))");
    private static final Pattern PATTERN_TYPE = Pattern.compile("( )[a-zA-Z0-9]+( )");
    private static final Pattern PATTERN_NAME = Pattern.compile("( )[a-zA-Z0-9]+(;)$");

    private static FileParseBean assembleBindView(String data) {
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

        return new FileParseBean(type, name, id);
    }

    private static final Pattern PATTERN_RES_ID = Pattern.compile("(\\(R)(2)?(.id.)[a-zA-Z0-9_]+(\\))");
    private static final Pattern PATTERN_RES_ID_ARRAY = Pattern.compile("(\\{)((R)(2)?(.id.)[a-zA-Z0-9_, ]+)+(})");
    private static final Pattern PATTERN_METHOD = Pattern.compile("(void )[a-zA-Z0-9]+( )?(\\()");
    private static final Pattern PATTERN_PARAMETER = Pattern.compile("(\\()[a-zA-Z0-9_ ]*(\\))");

    private static FileParseBean assembleOnClick(String data) {
        List<String> resIdList = new ArrayList<>();
        String method = null;
        boolean hasParameter = false;
        Matcher matcher;

        if ((matcher = PATTERN_RES_ID.matcher(data)).find()) {
            resIdList.add(matcher.group()
                    .replaceAll("\\(", "")
                    .replaceAll("\\)", "")
                    .replaceAll("R2", "R"));
        }
        if ((matcher = PATTERN_RES_ID_ARRAY.matcher(data)).find()) {
            String temp = matcher.group()
                    .replaceAll("\\{", "")
                    .replaceAll("}", "")
                    .replaceAll("R2", "R")
                    .replaceAll(" ", "");
            if (!temp.contains(",")) {
                resIdList.add(temp);
            } else {
                String[] split = temp.split(",");
                for (String s : split) {
                    if (!TextUtils.isEmpty(s)) {
                        resIdList.add(s);
                    }
                }
            }
        }

        if ((matcher = PATTERN_METHOD.matcher(data)).find()) {
            method = matcher.group()
                    .replaceAll("void ", "")
                    .replaceAll("\\(", "");
        }

        if ((matcher = PATTERN_PARAMETER.matcher(data)).find()) {
            String parameter = matcher.group()
                    .replaceAll("\\(", "")
                    .replaceAll("\\)", "");
            hasParameter = !TextUtils.isEmpty(parameter);
        }

        return new FileParseBean(resIdList, method, hasParameter);
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
