package com.codesky.reb.engine.model.impl;

import com.codesky.reb.engine.config.ForbidWordConfig;
import com.codesky.reb.engine.model.ForbidWordModel;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;

public class ForbidWordModelImpl implements ForbidWordModel {

    @Autowired
    private ForbidWordConfig forbidWordConfig;

    /**
     * 是否含有敏感词
     */
    @Override
    public Boolean isForbidWord(String text) {
        return this.forbidWordConfig.haveForbidWord(text);
    }

    /**
     * 将敏感词替换成'*'
     * @param text
     * @return
     */
    @Override
    public String filterForbidWord(String text) {
        return this.forbidWordConfig.filterForbidWord(text);
    }

    /**
     * 处理输入中的所有特殊字符，如?、$、|、ascii中32以下的特殊字符等
     */
    @Override
    public String processInput(String text) {
        if (StringUtil.isBlank(text)) {
            return text;
        } else {
            if (text != null && text.length() >= 1) {
                String ret = text.replaceAll("\\?", "\\？");
                ret = ret.replaceAll("\\$", "");
                ret = ret.replaceAll("\\|", "");

                try {
                    String Encoding = "UTF-8";
                    byte[] StrBytes = ret.getBytes(Encoding);

                    for(int x1 = 0; x1 < StrBytes.length; ++x1) {
                        if (StrBytes[x1] <= 31 && StrBytes[x1] >= 0) {
                            StrBytes[x1] = 32;
                        }
                    }

                    ret = new String(StrBytes, Encoding);
                    return ret;
                } catch (UnsupportedEncodingException var7) {
                    return null;
                }
            } else {
                return text;
            }
        }
    }

    /**
     * 去除输入中的所有空格
     */
    @Override
    public String processTrim(String text) {
        if (StringUtil.isBlank(text)) {
            return text;
        } else {
            text = text.trim();
            text = text.replaceAll(" ", "");
            text = text.replaceAll("　", "");
            return text;
        }
    }

}
