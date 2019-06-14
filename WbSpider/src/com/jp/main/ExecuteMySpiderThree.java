package com.jp.main;
/* 作者：成娓娓
 * 时间：2019.6.12
 * */

import com.alibaba.fastjson.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ExecuteMySpiderThree {

	//TODO 这是斗米网站记录的爬取  http://www.doumi.com/bj/    按照 城市 - 全职/兼职  去爬
	public static void main(String[] args) throws Exception {
		//TODO 所有城市
		int w = 0;
		//获取城市列表的url
		String changeCityUrl="http://www.doumi.com/cityselect/";
		Document changeCityDoc=MySpiderTool.getDocument(changeCityUrl);
		if (null!=changeCityDoc) {
			Map<String,String> endCityMap=MySpiderTool.getDoumiCityMap(changeCityDoc);
			if(null!=endCityMap){
				for (Map.Entry<String,String> entry : endCityMap.entrySet()) {
					String cityHref = entry.getValue();
					String cityName = entry.getKey();
					System.out.println(cityName);

					if(!(cityName.contains("北京") || cityName.contains("上海") || cityName.contains("广州") || cityName.contains("深圳"))){
						continue;
					}

					//TODO 某个城市分页
					for (int p = 1; p < 5; p++) {
						String url="";
						if(p==1){
							url = cityHref;
						}else {
							if(cityHref.contains("w1")){
								url = cityHref.replace("w1","o"+p+"w1");
							}else if(cityHref.contains("w2")){
								url = cityHref.replace("w2","o"+p+"w2");
							}
						}
						try{
							Thread.sleep(3000);
							Document doc=MySpiderTool.getDocument(url);
							if (null!=doc) {
								Elements divElements = doc.select(".jzList-txt-t");
								for (int i = 0; i < divElements.size(); ++i) {
//								for (int i = 0; i < 1; ++i) {
									//储存职位详细信息的json
									JSONObject posJson=new JSONObject();
									posJson.put("职位城市",cityName);

									// 获取一个职位的div
									Element div = divElements.get(i);

									Element posNameEle=div.select("a").first();
									String posName="无职位名称";
									if(null!=posNameEle){
										posName=posNameEle.text();
									}
									posJson.put("职位名称",posName);
									posName=cityName+"=="+posName;
									System.out.println(posName);

									if(null!=posNameEle){
										String compLinkHref = "http://www.doumi.com"+posNameEle.attr("href");
										System.out.println(compLinkHref);
										posJson.put("职位信息链接",compLinkHref);
										if(null!=compLinkHref && !"javascript:;".equals(compLinkHref)){
											//TODO 进入职位介绍主页，获取职位信息
											Thread.sleep(3000);
											Document compLinkDoc=MySpiderTool.getDocument(compLinkHref);
											if (null!=compLinkDoc) {
												Element salarySpanElement=compLinkDoc.select(".fl.salary-num").first();
												if(null!=salarySpanElement){
													String salary=salarySpanElement.text();
													System.out.println(salary);
													posJson.put("职位薪资",salary);
												}
												Element salaryTipsDivElement=compLinkDoc.select(".salary-tips").first();
												if(null!=salaryTipsDivElement){
													String salaryTips=salaryTipsDivElement.text();
													System.out.println(salaryTips);
													posJson.put("职位简介",salaryTips);
												}
												Element conditionDivElement=compLinkDoc.select(".jz-condition.clearfix").first();
												if(null!=conditionDivElement){
													String conditionTips=conditionDivElement.text();
													System.out.println(conditionTips);
													posJson.put("职位要求",conditionTips);
												}
												Element descriptionDivElement=compLinkDoc.getElementById("description-box");
												if(null!=descriptionDivElement){
													String description=descriptionDivElement.text();
													System.out.println(description);
													posJson.put("职位详情",description);
												}
												Element salaryWelfareDivElement=compLinkDoc.select(".jz-d-info.salary-welfare").first();
												if(null!=salaryWelfareDivElement){
													String salaryWelfare=salaryWelfareDivElement.text();
													System.out.println(salaryWelfare);
													posJson.put("公司福利",salaryWelfare);
												}
												Element areaDivElement=compLinkDoc.select(".bg-work.pr-80").first();
												if(null!=areaDivElement){
													Elements areasDiv=areaDivElement.select(".jz-d-area");
													List list=new ArrayList();
													for (Element areaDiv : areasDiv) {
														String area=areaDiv.text();
														list.add(area);
													}
													System.out.println(list);
													posJson.put("工作地点",list);
												}

												Element cpyNameDivElement=compLinkDoc.select(".cpy-name").first();
												if(null!=cpyNameDivElement){
													Element cpyNameLink=cpyNameDivElement.select("a").first();
													String cpyName="";
													if(null!=posNameEle){
														cpyName=cpyNameLink.text();
													}
													System.out.println(cpyName);
													posJson.put("公司名称",cpyName);

													String compHref = "http://www.doumi.com"+cpyNameLink.attr("href");
													System.out.println(compHref);
													posJson.put("公司信息链接",compHref);
													//TODO 进入公司介绍主页，获取公司信息
													Thread.sleep(3000);
													Document relDoc=MySpiderTool.getDocument(compHref);
													if (null!=relDoc) {
														Element compInfoDivElement=relDoc.select(".jz-d-info").first();
														if(null!=compInfoDivElement){
															String compInfo=compInfoDivElement.text();
															System.out.println(compInfo);
															posJson.put("公司介绍",compInfo);
														}
														Element compInfoUlElement=relDoc.select(".ident-list").first();
														if(null!=compInfoUlElement){
															String compInfo=compInfoUlElement.text();
															System.out.println(compInfo);
															posJson.put("公司基本信息",compInfo);
														}
													}

												}
											}
										}
									}

									//TODO 保存到本地目录
									w++;
									posName=posName.replaceAll("/","-");
									posName=posName.replaceAll("\\\\","");
									String fileName=w+"-"+posName+".txt";
									MySpiderTool.saveToLocal(fileName,posJson.toJSONString());
								}
							} else {
								System.out.println("Network is error : "+url);
							}

						}catch (Exception e){
							System.out.println("========================解析出错====================="+url);
						}
					}

				}
			}
		}

	}


}
