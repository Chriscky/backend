package com.example.sentiStrength.service;

import java.util.Map;

public class ParseTwoService {

    private final int[] table = {5, 9, 13, 14, 15, 16, 20, 21, 22, 29,
            30, 31, 41, 50, 51, 57, 58, 60, 61, 63, 68, 69, 70, 71};
    private final String[] sTable = {"filesubstring","listen", "annotatecol",
            "textcol", "idcol", "lang", "iterations", "minimprovement",
            "multi", "keywords", "wordsBeforeKeywords", "wordsAfterKeywords",
            "negativeMultiplier", "minPunctuationWithExclamation", "mood",
            "illegalDoubleLettersInWordMiddle", "illegalDoubleLettersAtWordEnd",
            "negatedWordStrengthMultiplier", "maxWordsBeforeSentimentToNegate",
            "maxWordsAfterSentimentToNegate", "MinSentencePosForQuotesIrony",
            "MinSentencePosForPunctuationIrony", "MinSentencePosForTermsIrony",
            "MinSentencePosForAllIrony"};


    public StringBuilder processParams(Map<Integer,Object> map, StringBuilder sb){
        if(map.containsKey(25) && map.get(25).equals("true")) return sb.append("help");
        for (int i = 0; i < table.length; i++) {
            if (map.containsKey(table[i])) {  // 找到对应的参数
                if (!map.get(table[i]).toString().equals("-2")) {
                    sb.append(sTable[i]).append("!@#!@#");
                    sb.append(map.get(table[i]).toString()).append("!@#!@#");
                }
            }
        }
        return sb;

    }
}
