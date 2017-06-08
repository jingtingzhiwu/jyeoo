package com.wilkey.biz;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.wilkey.App;
import com.wilkey.common.Constant;
import com.wilkey.common.Constant.ReportTypes;
import com.wilkey.common.ReportListPool;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author wilkey
 * @mail admin@wilkey.vip
 * @Date 2017年5月19日 下午5:45:00
 * @desc 获取jyeoo所有科目试卷列表
 */
public class JyeooReportTypeProcessor implements PageProcessor {
	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		
		if (page.getUrl().toString().indexOf(Constant.HOMEPAGE_URL) > -1) {
			loop:for (String key : Constant.keyword) {
				for (ReportTypes type : Constant.ReportTypes.values()) {
					Request request = new Request(String.format(Constant.REPORT_TYPE_URL + Constant.randomparam(), key, type.getValue(), 1));
					request.putExtra("subject", type);
					request.putExtra("keyword", key);
					request.putExtra("pageNum", 1);
					page.addTargetRequest(request);
					break loop;
				}
			}
		} else if (page.getUrl().regex("http://www\\.jyeoo\\.com/search.*").match()) {	//解析试卷List列表页
			ReportTypes type = ReportTypes.valueOf(page.getRequest().getExtra("subject") + "");
			String keyword = (String) page.getRequest().getExtra("keyword");
			Integer pageNum = (Integer) page.getRequest().getExtra("pageNum");

			Document doc = Jsoup.parse(page.getHtml().get());
			Elements quesLi = doc.select(".ques-list").select("ul li.ques_li");

			for (Element li : quesLi) {
				String title = li.select("fieldset a").text();
				String reportGuid = li.select("fieldset").attr("id");
				Integer years = null;
				String district = null;
				Pattern p1 = Pattern.compile("((19|20)\\d{2})");
				Matcher m1 = p1.matcher(li.select(".fright").text());
				if (m1.find()) {
					years = StringUtils.isNotBlank(m1.group(1)) ? Integer.valueOf(m1.group(1)) : null;
				} else {
					m1 = p1.matcher(title);
					if (m1.find())
						years = StringUtils.isNotBlank(m1.group(1)) ? Integer.valueOf(m1.group(1)) : null;
				}
				for (String tmp : Constant.ALL_AREAS) {
					if (title.indexOf(tmp) > -1) {
						district = tmp;
						break;
					}
				}

				Request request = new Request(String.format(Constant.REPORT_DETAIL_URL + Constant.randomparam(), type.name(), reportGuid));
				request.putExtra("subject", type);
				request.putExtra("reportGuid", reportGuid);
				request.putExtra("years", years);
				request.putExtra("district", district);
				App.getRedisScheduler().push(request, new Task() {

					@Override
					public String getUUID() {
						return "ReportDetailSpider";
					}

					@Override
					public Site getSite() {
						return Site.me().setDomain(Constant.HOMEPAGE_URL + Constant.randomparam()).setSleepTime(10 * 1000).setTimeOut(60 * 1000);
					}
				});
			}

			// 下一页
			if (pageNum == 1) {
				String maxpageStr = doc.select(".page a") != null && doc.select(".page a").size() > 0 ? doc.select(".page a").last().attr("href") : "";
				Pattern p1 = Pattern.compile("(p=\\d+)");
				Matcher m1 = p1.matcher(maxpageStr);
				int maxpage = 1;
				if (m1.find()) {
					maxpageStr = m1.group(1).replaceAll("[^\\d.]", "");
					maxpage = StringUtils.isNotBlank(maxpageStr) ? Integer.valueOf(maxpageStr) : 1;
				}
				for (int i = 2; i <= maxpage; i++) {
					Request request = new Request(String.format(Constant.REPORT_TYPE_URL + Constant.randomparam(), keyword, type.getValue(), i));
					request.putExtra("subject", type);
					request.putExtra("keyword", keyword);
					request.putExtra("pageNum", i);
					page.addTargetRequest(request);
				}
			}
		}
	}

	@Override
	public Site getSite() {
        if (site==null){
            site= Site.me().setDomain(Constant.HOMEPAGE_URL + Constant.randomparam())
                    .setSleepTime(10 * 1000)
					.setTimeOut(60 * 1000);
        }
        return site;
	}

	public static void main(String[] args) throws InterruptedException, JMException {
		Spider sp = Spider.create(new JyeooReportTypeProcessor()).addUrl(Constant.HOMEPAGE_URL + Constant.randomparam()).thread(25);
		sp.setUUID("ReportListSpider");
		sp.setDownloader(new JsoupDownloader());
		sp.setExecutorService(ReportListPool.getInstance());
		SpiderMonitor.instance().register(sp);
		sp.run();
		System.err.println("report list done...");
	}
}