package com.wilkey.biz;

import java.net.Proxy;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wilkey.common.Constant;
import com.wilkey.common.DynamicProxies;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.WMCollections;

public class JsoupDownloader extends AbstractDownloader implements Downloader {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Page download(Request request, Task task) {

		Site site = null;
		if (task != null) {
			site = task.getSite();
		}
		Set<Integer> acceptStatCode;
		if (site != null) {
			acceptStatCode = site.getAcceptStatCode();
		} else {
			acceptStatCode = WMCollections.newHashSet(200);
		}
		logger.info("downloading page {}", request.getUrl());

		int statusCode = 0;
		Proxy proxy = DynamicProxies.getAProxy(Integer.valueOf(Thread.currentThread().getName().substring(Thread.currentThread().getName().lastIndexOf("-") + 1)) - 1);

		int failure = 0;
		Document doc = null;
		Connection conn = null;
		if (null == proxy) {
			conn = Jsoup.connect(request.getUrl());
		} else {
			conn = Jsoup.connect(request.getUrl()).proxy(proxy);
		}

		while (true) {
			try {
				Response response = conn.header("User-Agent", Constant.agents[new Random().nextInt(Constant.agents.length)])
						.header("Cache-Control", "no-store")
						.header("Connection", "close")
						.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
						.header("Accept-Encoding", "gzip, deflate, sdch")
						.header("Accept-Language", "zh-CN,zh;q=0.8")
						.timeout(1000 * 60)
						.execute();
				doc = response.parse();

				statusCode = response.statusCode();
				request.putExtra(Request.STATUS_CODE, statusCode);
				if (statusAccept(acceptStatCode, statusCode)) {
					Page page = new Page();
					page.setRawText(doc.html());
					page.setUrl(new PlainText(request.getUrl()));
					page.setRequest(request);
					page.setStatusCode(statusCode);
					onSuccess(request);
					return page;
				} else {
					logger.warn("get page {} error, status code {} ", request.getUrl(), statusCode);
					return null;
				}
			} catch (Exception e) {
				onError(request);
				failure++;
				if (failure == 3) {
					logger.warn("get page {} error, 3 times, last proxy {}, exception {} ", request.getUrl(), proxy, e.getMessage());
					return null;
				}
				try {
					TimeUnit.SECONDS.sleep(new Random().nextInt(10));
				} catch (InterruptedException e1) {
				}
				conn = conn.proxy(DynamicProxies.getAProxy(Integer.valueOf(Thread.currentThread().getName().substring(Thread.currentThread().getName().lastIndexOf("-") + 1)) - 1));
				logger.warn("download page {} error", request.getUrl(), e);
				continue;
			} finally {
				request.putExtra(Request.STATUS_CODE, statusCode);
			}
		}
	}

	protected boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
		return acceptStatCode.contains(statusCode);
	}

	@Override
	public void setThread(int arg0) {
		// TODO Auto-generated method stub

	}

}
