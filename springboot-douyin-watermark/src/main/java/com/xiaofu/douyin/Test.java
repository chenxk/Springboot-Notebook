package com.xiaofu.douyin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaofu.douyin.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * Description of Test
 *
 * @author charles.chen
 * @date 2020-09-26 14:24
 */
@Slf4j
public class Test {
    static RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        String url = "https://m.toutiaoimg.cn/group/6871190293560557582/?app=news_article&timestamp=1601100212";
        System.out.println(getVideo(url));
        System.out.println(getVideo("https://www.ixigua.com/6868877360066891021/"));
    }

    //生成16位的随机数
    private static String getRandom() {
        Random random = new Random();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }


    public static String matchTouTiao(String redirectUrl) {
        Pattern p = Pattern.compile("/group/(.*)/");
        Matcher m = p.matcher(redirectUrl);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    // https://m.toutiaoimg.cn/i6868877360066891021/?app=news_article&timestamp=1601102655
    public static String matchTouTiao2(String redirectUrl) {
        Pattern p = Pattern.compile("toutiaoimg.cn/i(.*)/");
        Matcher m = p.matcher(redirectUrl);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static String matchXigua(String redirectUrl) {
        Pattern p = Pattern.compile("ixigua.com/(.*)/");
        Matcher m = p.matcher(redirectUrl);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static JSONObject getVideoId(String itemId) {
        String url = "https://m.toutiaoimg.cn/i" + itemId + "/info/v2/?_signature=";
        log.info("getVideoId:{}", url);
        JSONObject forObject = restTemplate.getForObject(url, JSONObject.class);
        log.info("forObject:{}", forObject);
        return forObject.getJSONObject("data");
    }

    public static JSONObject getVideoInfo(String videoId) {
        CRC32 crc32 = new CRC32();
        //String videoId = "v0300f6e0000bt9ie517qnk1hq4l1eag";
        String r = getRandom();
        String source = "/video/urls/v/1/toutiao/mp4/" + videoId + "?r=" + r;
        //进行crc32加密。
        crc32.update(source.getBytes());
        String crcString = crc32.getValue() + "";

        String xx = "https://ib.365yg.com" + source + "&s=" + crcString + "&aid=1217&nobase64=true&vfrom=xgplayer&_=" + System.currentTimeMillis() + "&callback=axiosJsonpCallback1";

        log.info("getVideoInfo:{}", xx);

        String forObject = restTemplate.getForObject(xx, String.class);
        String result = forObject.replaceAll("\\\\u0026", "&")
                .replace("axiosJsonpCallback1(", "")
                .replace(")", "");
        JSONObject jsonObject = JSON.parseObject(result);
        log.info("jsonObject:{}", jsonObject);

        return jsonObject;
    }

    public static String getVideo(String url) {
        String itemId = null;
        if (url.contains(CommonUtils.HUO_TOUTIAO_DOMAIN)) {
            itemId = matchTouTiao(url);
        }
        if (url.contains(CommonUtils.HUO_XIGUA_DOMAIN)) {
            itemId = matchXigua(url);
        }

        String videoId = getVideoId(itemId).getString("video_id");
        ;
        JSONObject videoInfo = getVideoInfo(videoId);

        JSONObject videoList = videoInfo.getJSONObject("data").getJSONObject("video_list");
        Set<String> strings = videoList.keySet();
        String mainUrl = videoList.getJSONObject("video_" + strings.size()).getString("main_url");
        return mainUrl;
    }
}


