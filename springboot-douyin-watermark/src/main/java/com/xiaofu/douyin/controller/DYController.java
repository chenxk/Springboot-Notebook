package com.xiaofu.douyin.controller;


import com.alibaba.fastjson.JSON;
import com.xiaofu.douyin.po.ResultDto;
import com.xiaofu.douyin.service.VideoParseUrlService;
import com.xiaofu.douyin.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author xiaofu-公众号：程序员内点事
 * @description 抖音无水印视频下载
 * @date 2020/9/15 18:44
 */
@Slf4j
@Controller
public class DYController {

    @Autowired
    private VideoParseUrlService videoParseUrlService;

    @RequestMapping("/index")
    public String index() {
        return "index";
    }

    /**
     * @param url
     * @author xiaofu
     * @description 解析无水印视频url
     * @date 2020/9/15 12:43
     */
    @RequestMapping("/parseVideoUrl")
    @ResponseBody
    public String parseVideoUrl(@RequestBody String url) throws Exception {

        log.info("待解析URL :{}", url);

        ResultDto resultDto = new ResultDto();
        try {
            url = URLDecoder.decode(url, "utf-8").replace("url=", "");
            //url = splitUrl(url);
            if (url.contains(CommonUtils.HUO_SHAN_DOMAIN)) {
                resultDto = videoParseUrlService.hsParseUrl(url);
            } else if (url.contains(CommonUtils.DOU_YIN_DOMAIN)) {
                resultDto = videoParseUrlService.dyParseUrl(url);
            } else if (url.contains(CommonUtils.HUO_TOUTIAO_DOMAIN)) {
                resultDto = videoParseUrlService.ttParseUrl(url);
            } else if (url.contains(CommonUtils.HUO_XIGUA_DOMAIN)) {
                resultDto = videoParseUrlService.ttParseUrl(url);
            }
        } catch (Exception e) {

            log.error("去水印异常 {}", e);
        }
        return JSON.toJSONString(resultDto);
    }

    public String splitUrl(String url) {
        String regex = "(http:|https:)//[^[A-Za-z0-9\\._\\?%&+\\-=/#]]*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(0);
        }
        return url;
    }

    @RequestMapping("/download")
    public void download(String url, HttpServletResponse response) throws IOException {
        //String url = "http://v26-dy.ixigua.com/f34fdeebb00b6534ec606a97191ba6d0/5f6cc47a/video/tos/cn/tos-cn-ve-15/41cb1daa44c448a99caa31b6132ca30e/?a=1128&br=1269&bt=423&cr=0&cs=0&cv=1&dr=0&ds=6&er=&l=2020092423080001019805820215288F84&lr=&mime_type=video_mp4&qs=0&rc=anRvN29oNTpydzMzZmkzM0ApPDtkaTs4ZTwzN2U8Mzg0OmdvYWdibWgyNjJfLS0tLS9zczEuMTU2YS0zXmNhYTM0YC46Yw%3D%3D&vl=&vr=";
        InputStream inputStream = getInputStreamByUrl(url);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        String nowStr = now.format(format);
        String fileName = "video";
        // 每次导出，文件名进行处理
        fileName += "_" + nowStr + "_" + new Random().nextInt(100);
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("video/mpeg4");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        //fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".MP4");

        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        OutputStream os;
        os = new BufferedOutputStream(response.getOutputStream());
        in = new BufferedInputStream(inputStream);
        out = new BufferedOutputStream(os);
        byte[] buffer = new byte[1024 * 8];
        int j = -1;
        while ((j = in.read(buffer)) != -1) {
            out.write(buffer, 0, j);
        }
    }

    public static InputStream getInputStreamByUrl(String strUrl) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(20 * 1000);
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(conn.getInputStream(), output);
            return new ByteArrayInputStream(output.toByteArray());
        } catch (Exception e) {
            log.error(e + "");
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                log.error(e + "");
            }
        }
        return null;
    }
}