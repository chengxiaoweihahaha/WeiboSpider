package com.jp.main;
/* 作者：成娓娓
 * 时间：2019.6.12
 * */

import com.alibaba.fastjson.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;


public class ExecuteMySpiderTwo {

	//TODO 这是58网站500条记录的爬取
	public static void main(String[] args) throws Exception {

		//TODO 所有城市
		int w = 0;
		//获取城市列表的url
		String changeCityUrl="https://www.58.com/changecity.html";
		Document changeCityDoc=MySpiderTool.getDocument(changeCityUrl);
		if (null!=changeCityDoc) {
			Map<String,String> endCityMap=MySpiderTool.getCityMap(changeCityDoc);
			if(null!=endCityMap){
				for (Map.Entry<String,String> entry : endCityMap.entrySet()) {
					String hotLinkHref = "https://"+entry.getValue()+".58.com/job/";
					String hotCityName = entry.getKey();
					System.out.println(hotCityName);

					//TODO 某个城市分页
					for (int p = 1; p < 2; p++) {
             //		for (int p = 14; p < 15; p++) {
						String url="";
						if(p==1){
							url = hotLinkHref;
						}else {
							url = hotLinkHref+"pn"+p+"/";
						}
						try{
							Thread.sleep(3000);
							Document doc=MySpiderTool.getDocument(url);
							if (null!=doc) {
								// 使用选择器获取ul
								Element ulElement = doc.getElementById("list_con");
								// 使用选择器选择该ul内所有的<li> <li/>
								Elements lis = ulElement.select("li");
             //					for (int i = 0; i < lis.size(); ++i) {
								for (int i = 0; i < 1; ++i) {
									//储存职位详细信息的json
									JSONObject posJson=new JSONObject();
									posJson.put("职位城市",hotCityName);

									// 获取一个li
									Element li = lis.get(i);

									Element posNameEle=li.select(".name").first();
									String posName="无职位名称";
									if(null!=posNameEle){
										posName=posNameEle.text();
									}
									posName=hotCityName+"=="+posName;
									System.out.println(posName);

									Element compNameDiv=li.select(".comp_name").first();
									if(null!=compNameDiv){
										//取compNameDiv的第一个a标签
										Element link = compNameDiv.select("a").first();
										if(null!=link){
											String compName=link.attr("title");
											System.out.println(compName);
											posJson.put("公司名称",compName);

											//TODO 进入公司介绍主页，获取公司联系方式
											String compLinkHref = link.attr("href");
											Thread.sleep(3000);
											Document compLinkDoc=MySpiderTool.getDocument(compLinkHref);
											if (null!=compLinkDoc) {
												Element contactDivElement=compLinkDoc.select(".contact").first();
												if(null!=contactDivElement){
													Elements contactDivs=contactDivElement.select(".c_detail_item");
													for (Element contactDiv : contactDivs) {
														String compContact=contactDiv.text();
														if(compContact.contains("联系人")){
															compContact=compContact.replace("联系人","");
															posJson.put("联系人",compContact);
														}else if(compContact.contains("招聘邮箱")){
															compContact=compContact.replace("招聘邮箱","");
															posJson.put("招聘邮箱",compContact);
														}else if(compContact.contains("联系电话")){
															compContact=compContact.replace("联系电话","");
															posJson.put("联系电话",compContact);
														}else if(compContact.contains("公司官网")){
															compContact=compContact.replace("公司官网","");
															posJson.put("公司官网",compContact);
														}
													}
												}
											}
										}
									}

									//取第一个a标签
									Element link = li.select("a").first();
									if(null!=link){
										//属性href的值
										String relHref = link.attr("href");
										System.out.println(relHref);
										posJson.put("职位详细信息链接",relHref);

										//TODO 获取岗位详细信息
										Thread.sleep(3000);
										Document relDoc=MySpiderTool.getDocument(relHref);
										if (null!=relDoc) {
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

											//TODO 获取公司相册
											posJson=MySpiderTool.getPicList(relDoc,posJson);

										}else {
											System.out.println("Network is error : "+relHref);
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
