package com.wilkey.biz;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.wilkey.App;
import com.wilkey.bean.SpReportItem;
import com.wilkey.bean.SpReportItemOpinion;
import com.wilkey.bean.SpReportType;
import com.wilkey.common.Constant;
import com.wilkey.common.Constant.ReportTypes;
import com.wilkey.common.PictureDownloadPool;
import com.wilkey.common.ReportDetailPool;
import com.wilkey.db.DBUtil;

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
 * @desc 采集试卷中的试题
 */
public class JyeooReportDetailProcessor implements PageProcessor {
	private static volatile LinkedList<SpReportType> types = new LinkedList<SpReportType>();
	private static volatile LinkedList<SpReportItem> items = new LinkedList<SpReportItem>();
	private static volatile LinkedList<SpReportItemOpinion> opinions = new LinkedList<SpReportItemOpinion>();
	
	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		ReportTypes type = ReportTypes.valueOf(page.getRequest().getExtra("subject") + "");
		String reportGuid = (String) page.getRequest().getExtra("reportGuid");
		Integer years = (Integer) page.getRequest().getExtra("years");
		String district = (String) page.getRequest().getExtra("district");

		Document doc = Jsoup.parse(page.getHtml().get());
		/****************************************** 解析type
		 */
		String title = doc.select(".rpt h1").text();
		Integer score = null, viewTimes = null, downloadTimes = null;
		Double difficultyValue = null;
		String rpt = doc.select(".rpt-count").text();
		String[] rpts = rpt.split("\\|");
		for (String string : rpts) {
			if (string.indexOf("总分") > -1) {
				string = string.replaceAll("[^\\d.]", "");
				score = StringUtils.isNotBlank(string) ? Integer.valueOf(string) : 120;
			} else if (string.indexOf("难度") > -1) {
				string = string.replaceAll("[^\\d.]", "");
				difficultyValue = StringUtils.isNotBlank(string) ? Double.valueOf(string) : 1D;
			} else if (string.indexOf("浏览") > -1) {
				string = string.replaceAll("[^\\d.]", "");
				viewTimes = StringUtils.isNotBlank(string) ? Integer.valueOf(string) : 0;
			} else if (string.indexOf("下载") > -1) {
				string = string.replaceAll("[^\\d.]", "");
				downloadTimes = StringUtils.isNotBlank(string) ? Integer.valueOf(string) : 0;
			}
		}
		String typeId = UUID.randomUUID().toString();
		types.add(new SpReportType(typeId, difficultyValue, district, downloadTimes, reportGuid, score, title, viewTimes, years, type.name()));
		/****************************************** 解析type
		 */
		
		for (Element element : doc.select(".rpt_b h3")) {
			String quesType = element.text();	// 题型
			for (String itemtype : Constant.getItemTypeSet()) {
				if (quesType.indexOf(itemtype) > -1) {
					quesType = itemtype;
					break;
				}
			}
			Elements fieldsets = element.nextElementSibling().select("fieldset");
			for (Element li : fieldsets) {
				String quesGuid = li.attr("id");
				String titleHtml = li.select("div.pt1").html();
				String itemId = UUID.randomUUID().toString();
				title = li.select("div.pt1").text();

				String combinationTimes = null;
				try {
					combinationTimes = li.nextElementSibling().select(".fieldtip").select("label:contains(组卷)").text();
					combinationTimes = StringUtils.isNotBlank(combinationTimes) ? combinationTimes.replaceAll("[^\\d.]", "") : "0";
				} catch (Exception e) {
					e.printStackTrace();
				}

				String reallyTimes = li.nextElementSibling().select(".fieldtip").select("label:contains(真题)").text();
				reallyTimes = StringUtils.isNotBlank(reallyTimes) ? reallyTimes.replaceAll("[^\\d.]", "") : "0";

				String quesDifficultyValue = li.nextElementSibling().select(".fieldtip").select("label:contains(难度)").text();
				quesDifficultyValue = StringUtils.isNotBlank(quesDifficultyValue) ? quesDifficultyValue.replaceAll("[^\\d.]", "") : "0";

				String localpath = "";
				// 保存题目图片, ext, path
				LinkedList<String> ouputpic = com.wilkey.utils.StringUtils.GetMatcher(titleHtml, Constant.PIC_REGX);
				for (String srcpath : ouputpic) {
					localpath = org.springframework.util.StringUtils.getFilenameExtension(srcpath);
					localpath = Constant.PIC_DIR_SAVEPATH + type.name() + File.separator + quesGuid + "." + localpath;
					com.wilkey.utils.StringUtils.makeDirs(localpath);
					PictureDownloadPool.getInstance().execute(new PictureDownloadProcessor(srcpath, localpath));
					titleHtml = titleHtml.replace(srcpath, localpath);
					localpath += File.pathSeparator;
				}
				// ******************************如果是选择题{
				if (li.select(".pt2") != null && li.select(".pt2").size() > 0) {
					Elements selectoptionElements = li.select(".pt2").select("td");
					for (Element selectoption : selectoptionElements) {
						String opinionGuid = UUID.randomUUID().toString();
						String selectoptionhtml = selectoption.select("label").html();
						String opinionlocalpath = "";
						// 如果选项有图片
						LinkedList<String> opinionpic = com.wilkey.utils.StringUtils.GetMatcher(selectoption.html(), Constant.PIC_REGX);
						for (String srcpath : opinionpic) {
							opinionlocalpath = org.springframework.util.StringUtils.getFilenameExtension(srcpath);
							opinionlocalpath = Constant.PIC_DIR_SAVEPATH + type.name() + File.separator + quesGuid + "_" + opinionGuid + "." + opinionlocalpath;
							com.wilkey.utils.StringUtils.makeDirs(opinionlocalpath);
							PictureDownloadPool.getInstance().execute(new PictureDownloadProcessor(srcpath, opinionlocalpath));
							selectoptionhtml = selectoptionhtml.replace(srcpath, opinionlocalpath);
						}
						opinions.add(new SpReportItemOpinion(opinionGuid, itemId, selectoptionhtml));
					}
				}
				// ****************************** }
				items.add(new SpReportItem(itemId, typeId, StringUtils.isBlank(combinationTimes) ? null : Integer.valueOf(combinationTimes),
						StringUtils.isBlank(quesDifficultyValue) ? null : Double.valueOf(quesDifficultyValue), district, quesGuid, quesType, localpath,
						StringUtils.isBlank(reallyTimes) ? null : Integer.valueOf(reallyTimes), title, titleHtml, years));

				Map<String, Object> answerMap = new HashMap<String, Object>();
				answerMap.put("itemId", itemId);
				answerMap.put("quesGuid", quesGuid);
				answerMap.put("subject", type.name());
				Request request = new Request(String.format(Constant.ANSWER_URL + Constant.randomparam(), type.name(), quesGuid));
				request.setExtras(answerMap);
				App.getRedisScheduler().push(request, new Task() {

					@Override
					public String getUUID() {
						return "AnswerSpider";
					}

					@Override
					public Site getSite() {
						return Site.me().setDomain(Constant.HOMEPAGE_URL + Constant.randomparam()).setSleepTime(10 * 1000).setTimeOut(60 * 1000);
					}
				});
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
		Thread itemThread = new Thread(new Runnable() {
			public void run() {
				synchronized (items) {
					while (true) {
						if (items.size() > 1) {
							InsertItem(items);
							items.clear();
						}
						try {
							DBUtil.closeConnection();
							TimeUnit.MINUTES.sleep(1);
						} catch (Exception e) {
						}
					}
				}
			}
		});
		itemThread.setDaemon(true);
		itemThread.start();

		Thread itemOpinionThread = new Thread(new Runnable() {
			public void run() {
				synchronized (opinions) {
					while (true) {
						if (opinions.size() > 1) {
							InsertItemOpinion(opinions);
							opinions.clear();
						}
						try {
							DBUtil.closeConnection();
							TimeUnit.MINUTES.sleep(1);
						} catch (Exception e) {
						}
					}
				}
			}
		});
		itemOpinionThread.setDaemon(true);
		itemOpinionThread.start();
		
		Thread typesThread = new Thread(new Runnable() {
			public void run() {
				synchronized (types) {
					while (true) {
						if (types.size() > 1) {
							InsertReportType(types);
							types.clear();
						}
						try {
							DBUtil.closeConnection();
							TimeUnit.MINUTES.sleep(1);
						} catch (Exception e) {
						}
					}
				}
			}
		});
		typesThread.setDaemon(true);
		typesThread.start();
		
		Spider sp = Spider.create(new JyeooReportDetailProcessor()).addUrl(Constant.HOMEPAGE_URL + Constant.randomparam()).thread(25);
		sp.setUUID("ReportDetailSpider");
		sp.setDownloader(new JsoupDownloader());
		sp.setExecutorService(ReportDetailPool.getInstance());
		sp.setScheduler(App.getRedisScheduler());
		SpiderMonitor.instance().register(sp);
		sp.start();
	}

	private static void InsertReportType(LinkedList<SpReportType> types) {
		String sql = DBUtil.genInsertSql("com.wilkey.bean.SpReportType");
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DBUtil.openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			pstmt = conn.prepareStatement(sql);
			int size = types.size() / 10000;
			size = types.size() % 10000 >= 0 ? size + 1 : size; // 5521,5
			for (int i = 0; i < size; i++) { // 6
				for (int j = 0; j < (i == size - 1 ? types.size() % 10000 : 10000); j++) {
					int _index = i * 10000 + j;
					pstmt.setString(1, types.get(_index).getId());
					if (null == types.get(_index).getDifficultyValue())
						pstmt.setNull(2, Types.DOUBLE);
					else
						pstmt.setDouble(2, types.get(_index).getDifficultyValue());
					pstmt.setString(3, types.get(_index).getDistrict());

					if (null == types.get(_index).getDownloadTimes())
						pstmt.setNull(4, Types.INTEGER);
					else
						pstmt.setInt(4, types.get(_index).getDownloadTimes());

					pstmt.setString(5, types.get(_index).getGuid());
					if (null == types.get(_index).getScore())
						pstmt.setNull(6, Types.INTEGER);
					else
						pstmt.setInt(6, types.get(_index).getScore());
					pstmt.setString(7, types.get(_index).getTitle());

					if (null == types.get(_index).getViewTimes())
						pstmt.setNull(8, Types.INTEGER);
					else
						pstmt.setInt(8, types.get(_index).getViewTimes());

					if (null == types.get(_index).getYears())
						pstmt.setNull(9, Types.INTEGER);
					else
						pstmt.setInt(9, types.get(_index).getYears());
					pstmt.setString(10, types.get(_index).getSubject());
					pstmt.addBatch();
				}
				pstmt.executeBatch();
				pstmt.clearBatch();
				conn.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != conn)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		types.clear();
	}

	private static synchronized void InsertItemOpinion(LinkedList<SpReportItemOpinion> opinions) {
		String sql = DBUtil.genInsertSql("com.wilkey.bean.SpReportItemOpinion");
		Connection conn = null;
		PreparedStatement pstmt = null;
		String id = null;
		try {
			conn = DBUtil.openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			pstmt = conn.prepareStatement(sql);
			int size = opinions.size() / 1000;
			size = opinions.size() % 1000 >= 0 ? size + 1 : size; // 5521,5
			for (int i = 0; i < size; i++) { // 6
				for (int j = 0; j < (i == size - 1 ? opinions.size() % 1000 : 1000); j++) {
					int _index = i * 1000 + j;
					id = opinions.get(_index).getId();
					pstmt.setString(1, opinions.get(_index).getId());
					pstmt.setString(2, opinions.get(_index).getItemId());
					pstmt.setString(3, opinions.get(_index).getOpinion());
					pstmt.addBatch();
				}
				pstmt.executeBatch();
				pstmt.clearBatch();
				conn.commit();
			}
		} catch (Exception e) {
			System.err.println("xxxxxxxxxxxxxx > No value specified for parameter 1 :" + id);
			e.printStackTrace();
		} finally {
			if (null != conn)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	private static synchronized void InsertItem(LinkedList<SpReportItem> items) {
		String sql = DBUtil.genInsertSql("com.wilkey.bean.SpReportItem");
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DBUtil.openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			pstmt = conn.prepareStatement(sql);
			int size = items.size() / 1000;
			size = items.size() % 1000 >= 0 ? size + 1 : size; // 5521,5
			for (int i = 0; i < size; i++) { // 6
				for (int j = 0; j < (i == size - 1 ? items.size() % 1000 : 1000); j++) {
					int _index = i * 1000 + j;
					pstmt.setString(1, items.get(_index).getId());
					pstmt.setString(2, items.get(_index).getTypeId());

					if (null == items.get(_index).getCombinationTimes())
						pstmt.setNull(3, Types.INTEGER);
					else
						pstmt.setInt(3, items.get(_index).getCombinationTimes());
					if (null == items.get(_index).getDifficultyValue())
						pstmt.setNull(4, Types.DOUBLE);
					else
						pstmt.setDouble(4, items.get(_index).getDifficultyValue());

					pstmt.setString(5, items.get(_index).getDistrict());
					pstmt.setString(6, items.get(_index).getGuid());
					pstmt.setString(7, items.get(_index).getItemType());
					pstmt.setString(8, items.get(_index).getPicPath());

					if (null == items.get(_index).getReallyTimes())
						pstmt.setNull(9, Types.INTEGER);
					else
						pstmt.setInt(9, items.get(_index).getReallyTimes());

					pstmt.setString(10, items.get(_index).getTitle());
					pstmt.setString(11, items.get(_index).getTitleHtml());

					if (null == items.get(_index).getYears())
						pstmt.setNull(12, Types.INTEGER);
					else
						pstmt.setInt(12, items.get(_index).getYears());

					pstmt.addBatch();
				}
				pstmt.executeBatch();
				pstmt.clearBatch();
				conn.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != conn)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
}