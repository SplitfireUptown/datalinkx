package com.datalinkx.copilot.llm;

public class LLMUtils {
    public static String buildRAGPrompt(String question, String context) {
        return "请利用如下上下文的信息回答问题：" + "\n" +
                question + "\n" +
                "上下文信息如下：" + "\n" +
                context + "\n" +
                "如果上下文信息中没有帮助,则不允许胡乱回答！";
    }

    public static String buildPrompt(String question) {
        return "请合理的回答问题：" + "\n" +
                question + "\n" +
                "不允许胡乱回答！";
    }
}
