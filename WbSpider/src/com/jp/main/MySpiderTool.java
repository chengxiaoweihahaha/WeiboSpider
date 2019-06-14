package com.jp.main;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*  本类主要是 下载那些已经访问过的文件*/
public class MySpiderTool {

    private static String dirPath;

    /*
    *  生成目录
    * */
    private static void mkdir() {
        if (dirPath == null) {
            dirPath = Class.class.getClass().getResource("/").getPath() + "temp\\";
        }
        File fileDir = new File(dirPath);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
    }

    /**
     * 保存网页字节数组到本地文件，filePath 为要保存的文件的相对地址
     */

    public static void saveToLocal(String fileName,String content) {
        mkdir();
        String filePath = dirPath + fileName ;
        byte[] data = content.getBytes();
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(filePath)));
            for (int i = 0; i < data.length; i++) {
                out.write(data[i]);
            }
            out.flush();
            out.close();
            System.out.println("文件："+ fileName + "已经被存储在"+ filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * OkHttpClient获取Document
     */
    public static Document getDocument(String url) {
        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", userAgent)
                .build();
        Document doc=null;
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String html = response.body().string().replace("\\", "");
                doc = Jsoup.parse(html);
            }
        } catch (IOException e) {
            System.out.println("OkHttpClient获取Document出错============"+e);
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * WebClient获取Document
     */
    public static Document getDocumentByWebClient(String url) {
        WebClient webClient = new WebClient();
        //设置webClient的相关参数
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        //模拟浏览器打开一个目标网址
        Document doc=null;
        try {
            HtmlPage rootPage = webClient.getPage(url);
            String html = rootPage.asText();
            doc = Jsoup.parse(html);
        } catch (IOException e) {
            System.out.println("WebClient获取Document出错============"+e);
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * 获取58网站所有城市
     */
    public static Map<String,String> getCityMap(Document changeCityDoc) {
        Map<String,String> endCityMap=new TreeMap<>();
        try {
            //TODO 存放最终城市的map
            //取scrip里面的所有城市+代号
            Elements elements = changeCityDoc.getElementsByTag("script").eq(2);
		    /*循环遍历script下面的JS变量*/
            for (Element element : elements) {
		        /*取得JS变量数组*/
                String[] data = element.data().toString().split("var");
				/*取得单个JS变量*/
                for(String variable : data){
                    if(variable.contains("=")){
						/*取到满足条件的JS变量*/
                        if(variable.contains("independentCityList")){
                            String[]  kvp = variable.split("=");
                            String citysStr=kvp[1];
                            citysStr=citysStr.replaceAll("\\r|\\n","");
                            citysStr="["+citysStr+"]";
                            List<Map> cityJsonList=JSONArray.parseArray(citysStr,Map.class);
                            Map<String,String> cityMap=cityJsonList.get(0);
                            for (Map.Entry<String,String> entry : cityMap.entrySet()) {
                                String str=entry.getValue().substring(0, entry.getValue().indexOf("|"));//截取|之前的字符串
                                endCityMap.put(entry.getKey(),str);
                            }

                        }else if(variable.contains("cityList")){
                            String[]  kvp = variable.split("=");
                            String citysStr=kvp[1];
                            citysStr=citysStr.replaceAll("\\r|\\n","");
                            citysStr="["+citysStr+"]";
                            List<Map> cityJsonList=JSONArray.parseArray(citysStr,Map.class);
                            Map<String,Map<String,String>> cityMap2=cityJsonList.get(0);

                            for (Map.Entry<String, Map<String, String>> entry : cityMap2.entrySet()) {
                                String privince=entry.getKey();//省
                                if(!"海外".equals(privince)){
                                    Map<String,String> map=entry.getValue();
                                    for (Map.Entry<String,String> mapEntry : map.entrySet()) {
                                        if(null!=mapEntry.getValue() && ""!=mapEntry.getValue()){
                                            String city=mapEntry.getKey();//市
                                            String str=mapEntry.getValue().substring(0, mapEntry.getValue().indexOf("|"));//截取|之前的字符串
                                            endCityMap.put(privince+"-"+city,str);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("获取58网站所有城市出错============"+e);
            e.printStackTrace();
        }
        return endCityMap;
    }

    /**
     * 获取58网站某公司相册
     */
    public static JSONObject getPicList(Document relDoc,JSONObject posJson) {
        try {
            Element photosDiv=relDoc.select(".photos").first();
			/*取得script下面的JS变量*/
            if(null!=photosDiv){
                Elements e = photosDiv.getElementsByTag("script").eq(0);
		        /*循环遍历script下面的JS变量*/
                for (Element element : e) {
		            /*取得JS变量数组*/
                    String[] data = element.data().toString().split("var");
					/*取得单个JS变量*/
                    for(String variable : data){
                        if(variable.contains("=")){
							/*取到满足条件的JS变量*/
                            if(variable.contains("picList")){
                                String[]  kvp = variable.split("=");
                                String imgUrlStr=kvp[1];
                                imgUrlStr=imgUrlStr.replaceAll("'","");
                                imgUrlStr=imgUrlStr.replaceAll("-","");
                                imgUrlStr=imgUrlStr.replaceAll(" ","");
                                imgUrlStr=imgUrlStr.replaceAll("\\|\\|;","");
                                imgUrlStr=imgUrlStr.replaceAll(".jpg","_188_150.jpg");
                                String[] imgUrls=imgUrlStr.split("\\|\\|");
                                List list = new ArrayList();
                                int m = 0;
                                for (String imgUrl : imgUrls) {
                                    String img="pic1.58cdn.com.cn"+imgUrl;
                                    list.add(m, img);
                                    m++;
                                }
                                if(null!=list && list.size()>0){
                                    posJson.put("公司介绍照片",list);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("获取58网站某公司相册出错==========="+e);
            e.printStackTrace();
        }
        return posJson;
    }


    //TODO 自测main方法
    public static void main(String[] args) throws Exception {
        String url="";
        Document document=getDocumentByWebClient(url);
    }

}
