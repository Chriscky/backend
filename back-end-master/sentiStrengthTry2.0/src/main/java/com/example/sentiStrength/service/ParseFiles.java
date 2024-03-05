package com.example.sentiStrength.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

public class ParseFiles {
    private final String[] oneTable = {"Saved optimised term weights to {}","Term weights saved to {}",
            "One-Classify + save with ID: {}", "one-annotateAllLines results are saved in {}"};
    private final String[] mutiTable = {"Muti-Classify + save with ID: {}",
            "muti-annotateAllLines results are saved in {}"};
    private final String sOut = "start with filename";
    private final String Separator = "!@#!@#";


    /**
     * 解析结果文件名
     * @param response
     * @return
     */
    public byte[] transferFiles(String response){
        for(String s:oneTable){
            if(response.contains(s)){
                String sub = response.substring(response.indexOf(s)+s.length());
                byte[] content = readFiles(sub);
                if(content!=null){
                    return content;
                }
            }
        }
        for(String s:mutiTable){
            if(response.contains(s)){
                String sub = response.substring(response.indexOf(s)+s.length());
                byte[] content = readFiles(sub);
                if(content!=null){
                    return content;
                }
            }
        }
        if(response.contains(sOut)){
            String sub = response.substring(response.indexOf(sOut)+sOut.length());
            byte[] content = readFiles(sub);
            if(content!=null){
                return content;
            }
        }
        return null;
    }


    public byte[] readFiles(final String filename){
        try{
            InputStream is = new FileInputStream(filename);
            int iAvail = is.available();
            byte[] bytes = new byte[iAvail];
            is.read(bytes);
            is.close();
            return bytes;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param file 流文件
     * @param targetDirPath 存储MultipartFile文件的目标文件夹
     * @return 文件的存储的绝对路径
     */
    public String saveMultipartFile(MultipartFile file, String targetDirPath){

        File toFile = null;
        if (file.equals("") || file.getSize() <= 0) {
            return null;
        } else {

            /*获取文件原名称*/
            String originalFilename = file.getOriginalFilename();
            /*获取文件格式*/
            String fileFormat = originalFilename.substring(originalFilename.lastIndexOf("."));

            String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
            toFile = new File(targetDirPath + File.separator + uuid + fileFormat);

            String absolutePath = null;
            try {
                absolutePath = toFile.getCanonicalPath();

                /*判断路径中的文件夹是否存在，如果不存在，先创建文件夹*/
                String dirPath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
                File dir = new File(dirPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                InputStream ins = file.getInputStream();

                inputStreamToFile(ins, toFile);
                ins.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return absolutePath.substring(absolutePath.indexOf("src"));
        }

    }

    //获取流文件
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 按照JSON格式读取文件内容
     * @param filepath 文件路径
     * @return JSON格式的文件内容
     */
    public JSONArray getJsonContent(String filepath){
        JSONArray array = new JSONArray();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filepath));
            String line = reader.readLine();
            line = reader.readLine(); // 舍弃首行
            System.out.println("start");
            while (line != null) {
                if(line.length() > 0){
                    JSONObject jsonObject = new JSONObject();
                    int space = line.indexOf("\t");
//                    System.out.println(line.charAt(0)); // score
//                    System.out.println(line.substring(2,12));  // datetime
//                    System.out.println(line.substring(14));  // content
                    jsonObject.put("time",line.substring(space+1,space+11));
                    jsonObject.put("content",line.substring(space+13));
                    jsonObject.put("score",line.substring(0,space));
                    array.add(jsonObject);
                }
                // 读取下一行
                line = reader.readLine();
            }
            System.out.println(array.size());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }

    /**
     * 删除本地临时文件
     *
     * @param file
     */
    public static void deleteTempFile(File file) {
        if (file != null) {
            File del = new File(file.toURI());
            del.delete();
        }
    }


}
