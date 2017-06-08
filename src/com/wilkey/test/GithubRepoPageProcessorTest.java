package com.wilkey.test;

import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.wilkey.common.Constant;
import com.wilkey.common.DynamicProxies;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class GithubRepoPageProcessorTest implements PageProcessor {

	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(5000);

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		// 部分二：定义如何抽取页面信息，并保存下来
		// 切换代理，ua
		Document doc = Jsoup.parse(page.getHtml().get());
		System.err.println(doc.select("#divTree"));
		System.err.println(doc.html());

	}

	@Override
	public Site getSite() {
		site.setUserAgent(Constant.agents[new Random().nextInt(Constant.agents.length)]);
		return site;
	}

	//http://ip.catr.cn
	public static void main(String[] args) throws InterruptedException {
		new Thread(new DynamicProxies()).start();
		Thread.sleep(3000);
		Spider.create(new GithubRepoPageProcessorTest())
				// 从"https://github.com/code4craft"开始抓
				.addUrl(Constant.QUES_URL[2]).addPipeline(new JsonFilePipeline("D:\\"))
				// 开启5个线程抓取
				.thread(5)
				// 启动爬虫
				.run();
	}
}