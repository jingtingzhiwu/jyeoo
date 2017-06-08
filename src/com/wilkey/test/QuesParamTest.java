package com.wilkey.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;
import com.wilkey.biz.JsoupDownloader;
import com.wilkey.common.Constant;
import com.wilkey.common.DynamicProxies;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author wilkey
 * @mail admin@wilkey.vip
 * @Date 2017年5月19日 下午5:45:00
 * @desc 测试jyeoo所有科目table.degree下的题型，难度，题类，来源，做为参数迭代，即选择题、填空题
 */
public class QuesParamTest implements PageProcessor {

	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		// 1、查询数据库 sp_subject表，获取所有科目
		// 2、迭代科目，拼接url，获取题目分类http://www.jyeoo.com/english2/ques/partialcategory?a=undefined&f=1&r=0.4777701425650114
		// 3、迭代题目分类
		// 4、迭代题型
		// 5、拼接url获取题目

		if (page.getUrl().toString().equals(Constant.HOMEPAGE_URL)) {
			List<String> list = page.getHtml().$("div.sub-group a", "href").all();
			for (String string : list) {
				if (StringUtils.isBlank(string) || string.endsWith("report"))
					continue;
				page.addTargetRequest(string);
			}
		} else if (page.getUrl().regex("http://www\\.jyeoo\\.com/(\\w+)/ques/search").match()) {
			Pattern p = Pattern.compile("http://www\\.jyeoo\\.com/(\\w+)/ques/search");
			Matcher m = p.matcher(page.getUrl().toString());
			if (m.find()) {
				Document doc = Jsoup.parse(page.getHtml().get());
				Elements eles = doc.select("table.degree tr");

				String json = "";
				for (Element element : eles) {
					Elements ahref = element.select("a");
					Map map = new HashMap();
					for (Element element2 : ahref) {
						map.put(element2.attr("onclick").replaceAll("[^\\d.]", ""), element2.text());
					}
					json += element.select("th").text() + ": " + JSONObject.toJSON(map);
				}
				System.err.println(m.group(1) + json);
			}
		}

	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) throws InterruptedException {
		new Thread(new DynamicProxies()).start();
		Thread.sleep(2000);
		Spider sp = Spider.create(new QuesParamTest()).addUrl(Constant.HOMEPAGE_URL).thread(25);
		sp.setDownloader(new JsoupDownloader());
		sp.run();
		System.err.println("1");
	}
}