package com.example.sentiStrength.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.sentiStrength.service.*;



import org.apache.commons.io.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@CrossOrigin
@RequestMapping("text")
@RestController
public class ReceiveController {

    private String keywords = new String();

    public String getFile_path() {
        return file_path;
    }

    /**
     * 按照人名打分后的文件路径，
     */
    private String file_path;

    /**
     * 按照时间打分之后的文件路径数组
     */
    ArrayList filenames = new ArrayList();
    private StringBuilder sb = new StringBuilder();
    private final String Separator = "!@#!@#";
    private final SentiStrengthService sentiStrengthService = new SentiStrengthService();
    private final ParseFiles parseFiles = new ParseFiles();
    private final String pathOfFiles = "src/main/java/Files_Resource/";
    private final String pathOfFiles1 = "/Users/zhaoxinyi/Desktop/未命名文件夹 2/back-end/sentiStrengthTry2.0/src/main/java/Files_Resource/";


   @Resource
   CrawlRuner crawlRuner;

    /**
     * 接收参数
     * @param map
     * @return
     */
    @ResponseBody
    @PostMapping("options")
    public String receiveParams(@RequestBody Map<Integer,Object> map){
        ParseOneService parseOneService = new ParseOneService();
        ParseTwoService parseTwoService = new ParseTwoService();

        // 初始化参数集合
        sb = new StringBuilder();

        sb = parseTwoService.processParams(map,sb);
        sb = parseOneService.processParams(map,sb);

        return "success";
    }


    /**
     * 处理text
     */
    @ResponseBody
    @RequestMapping("textTest")
    public String handleText(@RequestBody Map<String,String> map){
        System.out.println("textTest");
        String content = map.get("text");
        sb.append("text").append(Separator).append(content).append(Separator);
        String response = sentiStrengthService.run(sb);
        String[] results = response.split(Separator);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg","success");
        for (int i=0; i<results.length; i++){
            jsonObject.put("res"+i,results[i]);
        }
        return jsonObject.toJSONString();
    }

    /**
     * 处理文件,第二个是优化文件
     */
    @ResponseBody
    @PostMapping("fileTest")
    public ResponseEntity<byte[]> handleFiles(@RequestParam(value = "file") MultipartFile[] files,
                                              @RequestParam(value = "optimise",required = false) MultipartFile optimise, HttpServletResponse response) {
        long lTime= Calendar.getInstance().getTimeInMillis();
        String targetDirPath = pathOfFiles + lTime;

        StringBuilder filenames_sb = new StringBuilder();
        int validFileNum = 0;
        for (MultipartFile file:files) {
            if (file != null && file.getSize() > 0) {
                filenames_sb.append(parseFiles.saveMultipartFile(file,targetDirPath)).append(Separator);
                validFileNum ++;
                System.out.println(filenames_sb.toString());
                //根据文件类型储存在不同的文件夹下
            }
        }
        if(validFileNum == 0){
            return null;
        }

        if(validFileNum == 1){
            // filename_sb本身末尾有Separator,这里就不用加了
            sb.append("input").append(Separator).append(filenames_sb);
            if (optimise != null && optimise.getSize() > 0) {
                sb.append("optimise").append(Separator);
                sb.append(parseFiles.saveMultipartFile(optimise,targetDirPath)).append(Separator);
            }
        }else{  // 多文件
            sb.append("inputfolder").append(Separator).append(targetDirPath);
        }
        String response1 = sentiStrengthService.run(sb);
        String[] results = response1.split(Separator);


        byte[] bytes = null;
        for (int i=0; i<results.length; i++){
            System.out.println(results[i]);
            byte[] subFile = parseFiles.transferFiles(results[i]);
            if(subFile!=null){
                bytes = subFile;
            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", "products.xlsx");
        // 返回响应实体

        String fileName = "attachment;filename="+files[0].getOriginalFilename().substring(0,files[0].getOriginalFilename().length()-4)+"_"+"out"+".txt";
        response.setHeader("content-type", "text/html;charset=UTF-8");
        response.setHeader("Content-Disposition", new String(fileName.getBytes(), StandardCharsets.UTF_8));
        response.addCookie(new Cookie("cook-001-name" ,"cookie-001-value"));
        response.addCookie(new Cookie("cook-002-name" ,"cookie-002-value"));

        if(bytes == null){
            for (int i=0; i<results.length; i++){
                bytes = results[i].getBytes();
            }

        }
        return ResponseEntity.ok()
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(bytes);
    }


    /**
     * 词典类文件设置
     */
    @PostMapping("fileSet")
    public String setFiles(@RequestParam(value = "dataLoc",required = false) MultipartFile[] dataLoc,
                           @RequestParam(value = "sentiDict",required = false) MultipartFile sentiDict,
                           @RequestParam(value = "addFile",required = false) MultipartFile addFile,
                           @RequestParam(value = "lemmaFile",required = false) MultipartFile lemmaFile) {
        /*这里的files数组就是前端传递过来的fiel文件*/
        long lTime= Calendar.getInstance().getTimeInMillis();
        String targetDirPath = pathOfFiles + lTime;
        System.out.println("fileSet");
        if(dataLoc!=null && dataLoc.length > 0){
            for (MultipartFile file:dataLoc) {
                if (file != null && file.getSize() > 0) {
                    parseFiles.saveMultipartFile(file,targetDirPath);
                }
            }
            sb.append("sentidata").append(Separator).append(targetDirPath).append(Separator);
        }

        lTime= Calendar.getInstance().getTimeInMillis();
        targetDirPath = pathOfFiles + lTime;
        if (sentiDict != null && sentiDict.getSize() > 0) {
            sb.append("emotionlookuptable").append(Separator);
            sb.append(parseFiles.saveMultipartFile(sentiDict,targetDirPath)).append(Separator);
        }
        if (addFile != null && addFile.getSize() > 0) {
            sb.append("additionalfile").append(Separator);
            sb.append(parseFiles.saveMultipartFile(addFile,targetDirPath)).append(Separator);
        }
        if (lemmaFile != null && lemmaFile.getSize() > 0) {
            sb.append("lemmaFile").append(Separator);
            sb.append(parseFiles.saveMultipartFile(lemmaFile,targetDirPath)).append(Separator);
        }
        return "success";
    }


//    /**
//     * 处理上传的文件，返回分析结果用于绘制表格
//     */
//    @ResponseBody
//    @PostMapping("graph")
//    public ResponseEntity<JSONArray> handleGraph(@RequestParam(value = "file") MultipartFile[] files) throws IOException {
//        JSONArray jsonArray = new JSONArray();
//        //文件夹的名字
//        long lTime= Calendar.getInstance().getTimeInMillis();
//        String targetDirPath = pathOfFiles + lTime;
//        //这个是文件名的合集
//        StringBuilder filenames_sb = new StringBuilder();
//        StringBuilder version_sb = new StringBuilder();
//        int fileNum = 0;
//
//
//        for (MultipartFile file:files) {
//            if (file != null && file.getSize() > 0) {
//                filenames_sb.append(parseFiles.saveMultipartFile(file,targetDirPath)).append(Separator);
//                String fileName = file.getOriginalFilename();
//                String[] temp = fileName.split(" ");
//                String versionName = temp[0]+temp[1];
//                version_sb.append(versionName).append(Separator);
//                System.out.println(filenames_sb.toString());
//                fileNum++;
//                //根据文件类型储存在不同的文件夹下
//            }
//        }
//
//        String[] filenames_list = String.valueOf(filenames_sb).split(Separator);
//        String[] version_list = String.valueOf(version_sb).split(Separator);
//        for(int i = 0;i<fileNum;i++){
//            String finalPath =filenames_list[i];
//            File targetFile = new File(finalPath);
//            int sumNum = 0;
//            int posNum = 0;
//            int negNum = 0;
//            try(BufferedReader br = new BufferedReader(new FileReader(targetFile));){
//                String readLine = null;
//                while ((readLine = br.readLine())!=null){
//                    if(!readLine.equals("Overall\tText")){
//                        String temp = readLine.substring(0,1);
//                        int grade = 0;
//                        if(temp.equals("-")){
//                            grade = Integer.parseInt(readLine.substring(0,2));
//                        }else {
//                            grade = Integer.parseInt(readLine.substring(0,1));
//                        }
//                        sumNum++;
//                        if(grade>0){
//                            posNum++;
//                        }
//                        if(grade<0){
//                            negNum++;
//                        }
//                    }
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("version",version_list[i]);
//            jsonObject.put("textSum",sumNum);
//            jsonObject.put("posSum",posNum);
//            jsonObject.put("negSum",negNum);
//            jsonArray.add(jsonObject);
//        }
//
//        return ResponseEntity.ok().body(jsonArray);
//
//    }


    /**
     * 接收项目名称
     * @param keyword 项目名称
     * @return 返回人名列表
     */
    @ResponseBody
    @PostMapping("keywords")
    public String[] receiveKeywords(@RequestBody String keyword){
        //TODo 根据项目名称爬虫
        keywords = keyword;
        String s = crawlRuner.RunWithKeyWords("try", keywords);
        String[] list = s.substring(1,s.length()-1).split(",");
        String[] result = new String[list.length];
        for(int i = 0;i<list.length;i++){
            String temp = list[i].substring(2,list[i].length()-1);
            result[i] = temp;
        }
        return result;
    }


    /**
     * 接收人名
     * @param name  人名
     * @return 返回人名评论打分后的文件
     */
    @ResponseBody
    @PostMapping("nameFile")
    public ResponseEntity<byte[]> receiveNameFile(@RequestBody String name,HttpServletResponse response) throws IOException {
        file_path = null;
        /* TODo1 爬虫 （输入： 全局变量keywords + 人名 targetName） （输出：本地文件名 toBeScoredFilePath）*/
        String targetName = name;
//        String nm = targetName.substring(2,targetName.length()-1);
//        System.out.println(nm);
        //打完分后的文件路径
         String point_name = crawlRuner.RunWithAuthor("name",keywords,targetName);
//        String point_name = "E:\\大三下\\软工Ⅲ\\back-end\\sentiStrengthTry2.0\\src\\main\\java\\Files_Resource\\beam Abacn0_out.txt";


        file_path = point_name;
        System.out.println("point");
        System.out.println(file_path);
        /* 参数我会在此之前就传给 /text/options 保存在本地/
        /* TODo3 传回打分文件 ,这边不知到能不能下载*/
        File file = new File(file_path);
        byte[] bytes = null;

        byte[] subFile = parseFiles.readFiles(file_path);
        if(subFile!=null){
            bytes = subFile;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", "products.xlsx");
        // 返回响应实体

        String fileName = "attachment;filename="+file.getName();
        System.out.println(fileName);
        response.setHeader("content-type", "text/html;charset=UTF-8");
        response.setHeader("Content-Disposition", new String(fileName.getBytes(), StandardCharsets.UTF_8));
        response.addCookie(new Cookie("cook-001-name" ,"cookie-001-value"));
        response.addCookie(new Cookie("cook-002-name" ,"cookie-002-value"));

        System.out.println(file_path);
        return ResponseEntity.ok()
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(bytes);

    }

    /**
     * @return 返回人名的处理结果
     */
    @ResponseBody
    @GetMapping("nameResult")
    public ResponseEntity<JSONArray>  receiveNameResult(){
        /* TODo2 传回处理结果 （输入： 已经评分的文件流/文件路径）（输出：代表结果的jsonArray ）*/
        /* 大概类型是这样呢 */
        String path = null;
        int i=0;
        while (path == null){
            i++;
            try {
                Thread.currentThread().sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("第"+i+"次尝试");
            path = getFile_path();
        }

        System.out.println("nameResult");
        System.out.println(path);
        JSONArray jsonArray = parseFiles.getJsonContent(path);
        System.out.println(jsonArray);
        return ResponseEntity.ok().body(jsonArray);

    }


    /**
     * 接收时间段
     *
     * @param
     * @return 返回根据时间的评论的打分结果
     */
    @ResponseBody
    @PostMapping("dateFile")
    public ResponseEntity<byte[]> receiveDateFile(@RequestBody Map<String, Object[]> input, HttpServletResponse response) throws IOException {

        System.out.println(input.get("startList")[0]);
        String[] startDate = new String[100];
        String[] endDate = new String[100];
        String[] version = new String[100];

        filenames.clear();
        for(int i =0;i<input.get("startList").length;i++){
            startDate[i]= input.get("startList")[i].toString();
            endDate[i]= input.get("endList")[i].toString();
            version[i] = input.get("version")[i].toString();
            System.out.println(startDate[i]);
            System.out.println(endDate[i]);
            //TODo2 打分 （输入: 还没有评分的文件路径 toBeScoredFilePath）（输出：已经评分的文件 这里的连接可以自己商量）
            String point_name = crawlRuner.RunWithTime("time",keywords,version[i],startDate[i],endDate[i]);

            filenames.add(point_name);

        }

//        filenames.add("src/main/java/Files_Resource/beam 2.41.0 2023-05-17 2023-05-200_out.txt");
//        filenames.add("src/main/java/Files_Resource/beam 2.42.0 2023-05-22 2023-05-240_out.txt");

        //参数我会在此之前就传给 /text/options 保存在本地/
        String filename = "attachment;filename=" + keywords;
        System.out.println(filename);
        //TODo3 传回打分文件

        Map<String, byte[]> byteFileMap = new HashMap<>();
        for(int i=0;i<filenames.size();i++){
            byte[] a = parseFiles.readFiles(filenames.get(i).toString());
            byteFileMap.put(filenames.get(i).toString().split("/")[4], a);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        batchFileToZIP(byteFileMap, byteArrayOutputStream);

        String zipFileName = String.format("%s%s", keywords, ".zip");

        return buildByteFileResponse(zipFileName, byteArrayOutputStream.toByteArray(), response);




    }


    /**
     * @return 返回时间的处理结果
     */
    @ResponseBody
    @GetMapping("dateResult")
    public ResponseEntity<JSONArray>  receiveDateResult() throws IOException{
        //TODo2 传回处理结果 （输入： 已经评分的文件流/文件路径）（输出：代表结果的jsonArray ）
        //大概类型是这样呢,有一个数组filenames，里面存放的是打完分的文件路径，
        //类似”src/main/java/Files_Resource/apache/beam 2.42.0 2020-05-24 2022-05-290.out.txt"
        JSONArray jsonArray = new JSONArray();
        for(int i = 0;i<filenames.size();i++){
            //String finalPath ="/Users/zhaoxinyi/Desktop/未命名文件夹 2/back-end/sentiStrengthTry2.0/"+  filenames_list[i];
            String finalPath =filenames.get(i).toString();
            String[] versionTemp = finalPath.split(" ");
            String[] versionList = versionTemp[0].split("/");
            int num = versionList.length;
            String version = versionList[num-1];
            String versionName = version+versionTemp[1];
            File targetFile = new File(finalPath);
            int sumNum = 0;
            int posNum = 0;
            int negNum = 0;
            try(BufferedReader br = new BufferedReader(new FileReader(targetFile));){
                String readLine = null;
                while ((readLine = br.readLine())!=null){
                    if(!readLine.equals("Overall\tText")){
                        String temp = readLine.substring(0,1);
                        int grade = 0;
                        if(temp.equals("-")){
                            grade = Integer.parseInt(readLine.substring(0,2));
                        }else {
                            grade = Integer.parseInt(readLine.substring(0,1));
                        }
                        sumNum++;
                        if(grade>0){
                            posNum++;
                        }
                        if(grade<0){
                            negNum++;
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("version",versionName);
            jsonObject.put("textSum",sumNum);
            jsonObject.put("posSum",posNum);
            jsonObject.put("negSum",negNum);
            System.out.println(sumNum);
            jsonArray.add(jsonObject);
        }

        return ResponseEntity.ok().body(jsonArray);

    }


    /**
     * 文件批量压缩,不保存实际位置
     *
     * @param byteList         文件字节码Map，k:fileName，v：byte[]
     * @param byteOutPutStream 字节输出流
     */
    public static void batchFileToZIP(Map<String, byte[]> byteList, ByteArrayOutputStream byteOutPutStream) {

        ZipOutputStream zipOutputStream = new ZipOutputStream(byteOutPutStream);
        try {
            for (Map.Entry<String, byte[]> entry : byteList.entrySet()) {
                //写入一个条目，我们需要给这个条目起个名字，相当于起一个文件名称
                zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));
                zipOutputStream.write(entry.getValue());
            }
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zipOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ResponseEntity下载zip文件
     *
     * @param fileName 文件名
     * @param byteFile 字节码
     * @param response response
     * @return
     */
    public static ResponseEntity<byte[]> buildByteFileResponse(String fileName, byte[] byteFile, HttpServletResponse response) {
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.flushBuffer();
            return ResponseEntity.ok()
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                    .body(byteFile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("下载失败.".getBytes());
        }
    }



}
