package com.example.sentiStrength.service;

import uk.ac.wlv.sentistrength.SentiStrength;

public class SentiStrengthService {
    private final String Separator = "!@#!@#";


    public String run(final StringBuilder sb){
        String[] args = sb.toString().trim().split(Separator);
        SentiStrength sentiStrength = new SentiStrength();
        System.out.println(args.length);
        for(String s:args){
            System.out.println(s);
        }
        String response = sentiStrength.initialiseAndRun(args);
        return response;
    }
}
