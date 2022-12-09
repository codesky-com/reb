package com.codesky.reb.engine.config.impl;

import com.codesky.reb.engine.config.ForbidWordConfig;
import com.codesky.reb.engine.interfaces.GameConfig;
import com.codesky.reb.engine.model.GameConfigModel;
import com.codesky.reb.utils.LoggerUtils;
import jodd.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ForbidWordConfigImpl implements ForbidWordConfig, GameConfig {
    private final String file = "config/sensitive-words2.txt";
    private List<String> forbidWords;

    @Override
    public void init() {
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file);
            this.forbidWords = getForbidWordList(inputStream, "UTF-8");
        } catch (Exception e) {
            LoggerUtils.error(this.getClass(), "init", e, "forbidWord", "readError");
        }
    }

    @Override
    public void check(GameConfigModel var1) {

    }

    /**
     * 从配置文件中加载敏感词返回列表
     */
    private List<String> getForbidWordList(InputStream input, String charsetName) throws IOException {
        InputStreamReader is = new InputStreamReader(input, charsetName);
        BufferedReader reader = new BufferedReader(is);

        String line;
        List<String> result = new ArrayList<>();
        while (null != (line = reader.readLine())) {
            result.add(line);
        }
        return result;
    }

    private String replaceAll(String text) {
        //String[] a = {" ", "&", "!", "！", "@", "#", "$", "￥", "*", "^", "%", "?", "？", "<", ">", "《", "》"};
        text = text.trim();
        text = text.replaceAll(" ", "");
        text = text.replaceAll("　", "");
        text = text.replaceAll("\\*", "");
        text = text.replaceAll("#", "");
        text = text.replaceAll("-", "");
        text = text.replaceAll("=", "");
        text = text.replaceAll(",", "");
        text = text.replaceAll("，", "");
        text = text.replaceAll(".", "");
        text = text.replaceAll("。", "");
        text = text.replaceAll("\\+", "");
        text = text.replaceAll("/", "");
        text = text.replaceAll("|", "");
        text = text.replaceAll("\\\\", "");
        return text;
    }

    @Override
    public boolean haveForbidWord(String text) {
        boolean isMatch = false;
        if (StringUtil.isBlank(text)) {
            return false;
        } else {
            for (String reg : this.forbidWords) {
                isMatch = Pattern.matches(reg, text);
                if (isMatch) {
                    break;
                }
            }
        }

        return isMatch;
    }


    private Set<String> getSensitiveWord(String text){
        Set<String> sensitiveWordList = new HashSet<>();

        for (String reg : this.forbidWords) {

        }

        return sensitiveWordList;
    }

    @Override
    public String filterForbidWord(String text) {
        if (StringUtil.isBlank(text)) {
            return text;
        } else {
            text = this.replaceAll(text);
            Set<String> sensitiveWord = getSensitiveWord(text);
            for (String word : sensitiveWord) {
                text = text.replaceAll(word,"*");
            }
            return text;
        }
    }
}
