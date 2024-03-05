package com.example.sentiStrength.service;

import com.example.sentiStrength.service.SentiStrengthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;


@Component
public class CrawlRuner {
    private final String pathOfFiles = "src/main/java/Files_Resource/";

    private final String Separator = "!@#!@#";

    private final SentiStrengthService sentiStrengthService = new SentiStrengthService();

    private final String sOut = "start with filename";

    // windows和linux中python输出的编码格式好像不一样.
    // 考虑到后面方便部署,这里引用了application.properties中的配置，也可以直接赋值
    @Value("${crawler.command}")
    String command;
    @Value("${crawler.path}")
    String path;


    // 调用函数就可以获取脚本运行的接过来
    /*
     * name: 脚本文件名 不用添加后缀(.py)
     * values : 调用脚本传递的参数，这里长度可变
     */
    public String RunWithKeyWords(String name, String... values){
        // 生成args
        int argsLength = values.length + 2;
        String[] args = new String[argsLength];
        args[0] = command;
        args[1] = path + name + ".py";
        for (int i=2;i<argsLength;i++) {
            args[i] = values[i-2];
        }
        // 运行
        Process proc;
        try{
            proc = Runtime.getRuntime().exec(args);
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            // 获取输出信息
            String line = null,s = "";
            while ((line = in.readLine()) != null) {
                s += line;
            }

            in.close();
            proc.waitFor();
            return s;
        }catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }


    public String RunWithAuthor(String name, String... values){
        // 生成args
        int argsLength = values.length + 2;
        String[] args = new String[argsLength];
        args[0] = command;
        args[1] = path + name + ".py";
        for(int i=2;i<argsLength;i++){
            args[i] = values[i-2];
        }
        String repo = values[0].split("/")[1];
        String author = values[1];

        System.out.println(author);
        // 运行
        Process proc;
        try{
            proc = Runtime.getRuntime().exec(args);
            BufferedReader in;

            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            // 获取输出信息
            String line = null;
            FileWriter writer;
            String file_name = pathOfFiles + repo + " " + author +".txt";
            writer = new FileWriter(file_name);
            writer.write("");//清空原文件内容
            writer.flush();
            writer.close();

            writer = new FileWriter(file_name,true);
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                writer.write(line);
                writer.write("\n");
                writer.flush();
            }
            writer.close();
            in.close();
            proc.waitFor();
            StringBuilder sb = new StringBuilder();
            sb.append("trinary").append(Separator);
            sb.append("input").append(Separator).append(file_name).append(Separator);
            String response1 = sentiStrengthService.run(sb);
            String results = response1.split(Separator)[0];
            String file_name_out = results.substring(results.indexOf(sOut)+sOut.length());

            return file_name_out;
        }catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    public String RunWithTime(String name, String... values){
        // 生成args
        int argsLength = values.length + 2;
        String[] args = new String[argsLength];
        args[0] = command;
        args[1] = path + name + ".py";
        for(int i=2;i<argsLength;i++){
            args[i] = values[i-2];
        }
        //apache/beam
        String repo = values[0].split("/")[1];
        String version = values[1];
        String start_time = values[2];
        String end_time = values[3];
        // 运行
        Process proc;
        try{
            String line = null;
            String file_name = pathOfFiles + repo + " " + version + " "+ start_time + " " + end_time +".txt";
            FileWriter writer;
            writer = new FileWriter(file_name);
            writer.write("");//清空原文件内容
            writer.flush();
            writer.close();

            proc = Runtime.getRuntime().exec(args);
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            // 获取输出信息

            writer = new FileWriter(file_name,true);
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                writer.write(line);
                writer.write("\n");
                writer.flush();
            }
            writer.close();
            in.close();
            proc.waitFor();
            StringBuilder sb = new StringBuilder();
            sb.append("trinary").append(Separator);
            sb.append("input").append(Separator).append(file_name).append(Separator);
            String response1 = sentiStrengthService.run(sb);
            String results = response1.split(Separator)[0];
            String file_name_out = results.substring(results.indexOf(sOut)+sOut.length());
            return file_name_out;
        }catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }


}
