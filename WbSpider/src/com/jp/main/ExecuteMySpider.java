package com.jp.main;
/* 作者：jiaopan
 * 时间：2016.5
 * */

import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class ExecuteMySpider {

	//TODO 这是首页40条记录的爬取
	public static void main(String[] args) throws Exception {

		String url = "https://www.58.com/ppeseoll20401/";
		String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
		String html = null;
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent", userAgent)
				.build();
		Response response = client.newCall(request).execute();

		if (response.isSuccessful()) {
			html = response.body().string().replace("\\", "");
			Document doc = Jsoup.parse(html);

			Element table = doc.getElementsByClass("textlist").first();
			// 使用选择器选择该table内所有的<tr> <tr/>
			Elements trs = table.select("tr");
			//遍历该表格内的所有的<tr> <tr/>
//			for (int i = 0; i < trs.size(); ++i) {
			for (int i = 0; i < 1; ++i) {
				// 获取一个tr
				Element tr = trs.get(i);
				// 获取该行的所有td节点
				Elements tds = tr.select("td");
				// 选择某一个td节点
				String posName = tds.get(0).text();
				System.out.println(posName);

				//储存职位详细信息的json
				JSONObject posJson=new JSONObject();

				String posTime = tds.get(1).text();
				System.out.println(posTime);
				posJson.put("职位发布时间",posTime);

				//取第一个a标签
				Element link = tds.get(0).select("a").first();
				if(null!=link){
					//属性href的值
					String relHref = link.attr("href");
					System.out.println(relHref);
					posJson.put("职位详细信息链接",relHref);

					//TODO 获取岗位详细信息
					String relUserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
					OkHttpClient relClient = new OkHttpClient();
					Request relRequest = new Request.Builder()
							.url(relHref)
							.addHeader("User-Agent", relUserAgent)
							.build();
					Response relResponse = relClient.newCall(relRequest).execute();

					if (relResponse.isSuccessful()) {
						String relHtml = relResponse.body().string().replace("\\", "");
						Document relDoc = Jsoup.parse(relHtml);
						Element span=relDoc.select(".pos_title").first();
						if(null!=span){
							String pos_title=span.text();
							System.out.println(pos_title);
							posJson.put("职位标题",pos_title);
						}

						Element posSalaryElement=relDoc.select(".pos_salary").first();
						String pos_salary="";
						if(null!=posSalaryElement){
							pos_salary=posSalaryElement.text();
							System.out.println(pos_salary);
						}

						Element font18Element=relDoc.select(".font18").first();
						if(null!=font18Element){
							String font18=font18Element.text();
							System.out.println(font18);
							posJson.put("职位薪资",pos_salary+" "+font18);
						}

						Element posNameElement=relDoc.select(".pos_name").first();
						if(null!=posNameElement){
							String pos_name=posNameElement.text();
							System.out.println(pos_name);
							posJson.put("职位名称",pos_name);
						}

						Element posWelfareDiv = relDoc.select(".pos_welfare").first();
						if(null!=posWelfareDiv){
							String posWelfareItem=posWelfareDiv.text();
							System.out.println(posWelfareItem);
							posJson.put("职位福利",posWelfareItem);
						}

						Elements posBaseConditionDiv=relDoc.select(".pos_base_condition");
						if(null!=posBaseConditionDiv){
							String posBaseConditionItem=posBaseConditionDiv.text();
							System.out.println(posBaseConditionItem);
							posJson.put("职位要求",posBaseConditionItem);
						}

						Elements posAreaItemDiv = relDoc.select(".pos-area");
						if(null!=posAreaItemDiv){
							String posAreaItem=posAreaItemDiv.text();
							System.out.println(posAreaItem);
							posJson.put("职位地址",posAreaItem);
						}

						Element titleElement=relDoc.select(".title").first();
						if(null!=titleElement){
							String title=titleElement.text();
							System.out.println(title);
						}

						Element desElement=relDoc.select(".des").first();
						if(null!=desElement){
							String divDes=desElement.text();
							System.out.println(divDes);
							posJson.put("职位描述",divDes);
						}

						Element divCompIntro=relDoc.select(".comp_intro").first();
						if(null!=divCompIntro){
							Element compIntroTitleElement=divCompIntro.select(".title").first();
							if(null!=compIntroTitleElement){
								String compIntroTitle=compIntroTitleElement.text();
								System.out.println(compIntroTitle);
							}
						}

						Element shijiElement=relDoc.select(".shiji").first();
						if(null!=shijiElement){
							String shiji=shijiElement.text();
							System.out.println(shiji);
							posJson.put("公司介绍",shiji);
						}

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
					}else {
						System.out.println("Network is error : "+relHref);
					}
				}

				//TODO 保存到本地目录
				int num=i+1;
				posName=posName.replaceAll("/","-");
				posName=posName.replaceAll("\\\\","");
				String fileName=num+"-"+posName+".txt";
				FileTool.saveToLocal(fileName,posJson.toJSONString());
			}
		} else {
			System.out.println("Network is error : "+url);
		}
	}


}
