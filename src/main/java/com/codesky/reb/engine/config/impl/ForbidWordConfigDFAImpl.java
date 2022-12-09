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
import java.util.*;

public class ForbidWordConfigDFAImpl implements ForbidWordConfig, GameConfig {

    public static final int MinMatchType = 1;
    public static final int MaxMatchType = 2;
    private final String file = "config/sensitive-words.txt";
    private HashMap sensitiveWordMap;

    @Override
    public void init() {
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file);
            List<String> forbidWordList = getForbidWordList(inputStream, "UTF-8");
            this.sensitiveWordMap = new HashMap(forbidWordList.size());

            Iterator<String> iterator = forbidWordList.iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                Map nowMap = sensitiveWordMap;

                for(int i = 0 ; i < key.length() ; i++){
                    char keyChar = key.charAt(i);
                    Object wordMap = nowMap.get(keyChar);

                    if(wordMap != null){
                        nowMap = (Map) wordMap;
                    } else{
                        Map<String, String> newWorMap = new HashMap<>();
                        newWorMap.put("isEnd", "0");
                        nowMap.put(keyChar, newWorMap);
                        nowMap = newWorMap;
                    }

                    if(i == key.length() - 1){
                        nowMap.put("isEnd", "1");
                    }
                }
            }

        } catch (Exception e) {
            LoggerUtils.error(this.getClass(), "init", e, "forbidWord", "readError");
        }
    }

    @Override
    public void check(GameConfigModel var1) {}

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

    /**
     * 检查文字中是否包含敏感字符
     * @param txt           字符串
     * @param beginIndex    开始索引
     * @param matchType     匹配规则 1：最小匹配 2：最大匹配
     * @return 是否包含敏感词  0：不包含   >0：包含，敏感词的长度
     */
    @SuppressWarnings({ "rawtypes"})
    private int checkSensitiveWord(String txt,int beginIndex,int matchType){
        boolean flag = false;
        int matchFlag = 0;

        Map nowMap = sensitiveWordMap;
        for(int i = beginIndex; i < txt.length() ; i++){
            char word = txt.charAt(i);
            nowMap = (Map)nowMap.get(word);
            if(null == nowMap){
                break;
            }

            matchFlag++;
            if("1".equals(nowMap.get("isEnd"))){
                flag = true;
                if(MinMatchType == matchType){
                    break;
                }
            }
        }

        if(matchFlag < 1 || !flag){
            matchFlag = 0;
        }
        return matchFlag;
    }

    private Set<String> getSensitiveWord(String text , int matchType){
        Set<String> sensitiveWordList = new HashSet<String>();

        for(int i = 0 ; i < text.length() ; i++){
            int length = this.checkSensitiveWord(text, i, matchType);    //判断是否包含敏感字符
            if(length > 0){    //存在,加入list中
                sensitiveWordList.add(text.substring(i, i+length));
                i = i + length - 1;    //减1的原因，是因为for会自增
            }
        }

        return sensitiveWordList;
    }

    private String getReplaceChars(char replaceChar, int length) {
        String resultReplace = String.valueOf(replaceChar);

        for(int i = 1; i < length; ++i) {
            resultReplace = resultReplace + replaceChar;
        }

        return resultReplace;
    }

    @Override
    public boolean haveForbidWord(String text) {
        if (StringUtil.isBlank(text)) {
            return false;
        } else {
            boolean flag = false;
            for(int i = 0; i < text.length(); ++i) {
                int matchFlag = this.checkSensitiveWord(text, i, MaxMatchType);
                if (matchFlag > 0) {
                    flag = true;
                    break;
                }
            }

            return flag;
        }
    }

    @Override
    public String filterForbidWord(String text) {
        if (StringUtil.isBlank(text)) {
            return text;
        } else {
            String resultText = text;
            Set<String> set = this.getSensitiveWord(text, MaxMatchType);
            Iterator<String> iterator = set.iterator();
            String word;
            String replaceString;
            while (iterator.hasNext()) {
                word = iterator.next();
                replaceString = this.getReplaceChars('*', word.length());
                resultText = resultText.replaceAll(word,replaceString);
            }

            return resultText;
        }
    }
}
