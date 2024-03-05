package com.example.sentiStrength.service;

import java.util.Map;

public class ParseOneService {

    private final int[] table = {6, 8, 10, 11, 17, 18, 19, 23, 33, 34, 35, 36,
            37, 38, 39, 40, 42, 43, 44, 46, 47, 48, 49, 52, 53, 54, 55, 56, 59,
            62, 64, 65, 66, 72, 73, 74};
    private final String[] sTable = {"overwrite", "urlencoded", "stdin", "cmd",
            "train", "all", "numcorrect", "termWeights", "stress", "trinary",
            "binary", "scale", "sentenceCombineAv", "sentenceCombineTot",
            "paragraphCombineAv", "paragraphCombineTot", "noBoosters",
            "noNegatingPositiveFlipsEmotion", "noNegatingNegativeNeutralisesEmotion",
            "noIdioms", "questionsReduceNeg", "noEmoticons", "exclamations2",
            "noMultiplePosWords", "noMultipleNegWords",
            "noIgnoreBoosterWordsAfterNegatives", "noDictionary",
            "noDeleteExtraDuplicateLetters", "noMultipleLetters",
            "negatingWordsDontOccurBeforeSentiment", "negatingWordsOccurAfterSentiment",
            "alwaysSplitWordsAtApostrophes", "capitalsBoostTermSentiment",
            "explain", "echo", "UTF8"};
    // 到时候依据实际情况再修改修改

    /**
     * 解析一类参数添加进已有参数.
     * @param map json
     * @param sb 原本的参数
     * @return 返回解析后的所有参数
     */
    public StringBuilder processParams(final Map<Integer, Object> map, StringBuilder sb) {
        if (map.containsKey(25) && map.get(25).equals("true")) {
            return sb;
        }
        for (int i = 0; i < table.length; i++) {
            if (map.containsKey(table[i])) {  // 找到对应的参数
                if (map.get(table[i]).toString().equals("true")) {
                    sb.append(sTable[i]).append("!@#!@#");
                }
            }
        }
        return sb;

    }
}
