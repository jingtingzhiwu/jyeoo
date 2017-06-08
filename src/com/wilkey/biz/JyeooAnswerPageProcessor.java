package com.wilkey.biz;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.wilkey.App;
import com.wilkey.bean.SpQuesItemAnswer;
import com.wilkey.common.AnswerPool;
import com.wilkey.common.Constant;
import com.wilkey.common.PictureDownloadPool;
import com.wilkey.db.DBUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;

public class JyeooAnswerPageProcessor implements PageProcessor {

	public JyeooAnswerPageProcessor() {
	}

	private static volatile LinkedList<SpQuesItemAnswer> answers = new LinkedList<SpQuesItemAnswer>();

	public static void main(String[] args) throws InterruptedException, SQLException, JMException {
		Thread answerThread = new Thread(new Runnable() {
			public void run() {
				synchronized (answers) {
					while (true) {
						if (answers.size() > 1) {
							InsertAnswer(answers);
							answers.clear();
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
		answerThread.setDaemon(true);
		answerThread.start();
		Spider sp = Spider.create(new JyeooAnswerPageProcessor()).addUrl(Constant.HOMEPAGE_URL + Constant.randomparam()).thread(5);
		sp.setUUID("AnswerSpider");
		sp.setDownloader(new JsoupDownloader());
		sp.setExecutorService(AnswerPool.getInstance());
		sp.setScheduler(App.getRedisScheduler());
		SpiderMonitor.instance().register(sp);
		sp.run();
	}

	@Override
	public void process(Page page) {

		if (page.getUrl().regex("http://www\\.jyeoo\\.com/(\\w+)/ques/detail.*").match()) {
			String itemId = (String) page.getRequest().getExtra("itemId");
			String subject = (String) page.getRequest().getExtra("subject");
			String quesGuid = (String) page.getRequest().getExtra("quesGuid");
			String answerOpinion = null;

			Document doc = Jsoup.parse(page.getHtml().get());

			// ******************************如果是选择题{
			if (doc.select(".pt2") != null && doc.select(".pt2").size() > 0) {
				Elements selectoptionElements = doc.select(".pt2").select("td");
				for (int i = 0; i < selectoptionElements.size(); i++) {
					Element selectoption = selectoptionElements.get(i);
					if (selectoption.select("label[class*=s]").size() > 0) {
						answerOpinion = (i + 1) + "";
					}
				}
			}
			// ****************************** }
			String answerHtml = doc.select("div[class*=pt]:contains(解答)").html();
			String answerPoint = doc.select("div[class*=pt]:contains(考点)").html();
			String answerNote = doc.select("div[class*=pt]:contains(分析)").html();
			String answerComment = doc.select("div[class*=pt]:contains(点评)").html();
			String answerGuid = UUID.randomUUID().toString();
			String answerlocalpath = "";

			LinkedList<String> answerpic = com.wilkey.utils.StringUtils.GetMatcher(answerHtml, Constant.PIC_REGX);
			for (String srcpath : answerpic) {
				answerlocalpath = org.springframework.util.StringUtils.getFilenameExtension(srcpath);
				answerlocalpath = Constant.PIC_DIR_SAVEPATH + subject + File.separator + quesGuid + "__" + answerGuid + "." + answerlocalpath;
				com.wilkey.utils.StringUtils.makeDirs(answerlocalpath);
				PictureDownloadPool.getInstance().execute(new PictureDownloadProcessor(srcpath, answerlocalpath));
				answerHtml = answerHtml.replace(srcpath, answerlocalpath);
			}
			answerlocalpath = "";

			answerpic = com.wilkey.utils.StringUtils.GetMatcher(answerPoint, Constant.PIC_REGX);
			for (String srcpath : answerpic) {
				answerlocalpath = org.springframework.util.StringUtils.getFilenameExtension(srcpath);
				answerlocalpath = Constant.PIC_DIR_SAVEPATH + subject + File.separator + quesGuid + "__" + answerGuid + "." + answerlocalpath;
				com.wilkey.utils.StringUtils.makeDirs(answerlocalpath);
				PictureDownloadPool.getInstance().execute(new PictureDownloadProcessor(srcpath, answerlocalpath));
				answerPoint = answerPoint.replace(srcpath, answerlocalpath);
			}
			answerlocalpath = "";

			answerpic = com.wilkey.utils.StringUtils.GetMatcher(answerNote, Constant.PIC_REGX);
			for (String srcpath : answerpic) {
				answerlocalpath = org.springframework.util.StringUtils.getFilenameExtension(srcpath);
				answerlocalpath = Constant.PIC_DIR_SAVEPATH + subject + File.separator + quesGuid + "__" + answerGuid + "." + answerlocalpath;
				com.wilkey.utils.StringUtils.makeDirs(answerlocalpath);
				PictureDownloadPool.getInstance().execute(new PictureDownloadProcessor(srcpath, answerlocalpath));
				answerNote = answerNote.replace(srcpath, answerlocalpath);
			}
			answerlocalpath = "";

			answerpic = com.wilkey.utils.StringUtils.GetMatcher(answerComment, Constant.PIC_REGX);
			for (String srcpath : answerpic) {
				answerlocalpath = org.springframework.util.StringUtils.getFilenameExtension(srcpath);
				answerlocalpath = Constant.PIC_DIR_SAVEPATH + subject + File.separator + quesGuid + "__" + answerGuid + "." + answerlocalpath;
				com.wilkey.utils.StringUtils.makeDirs(answerlocalpath);
				PictureDownloadPool.getInstance().execute(new PictureDownloadProcessor(srcpath, answerlocalpath));
				answerComment = answerComment.replace(srcpath, answerlocalpath);
			}

			answers.add(new SpQuesItemAnswer(answerGuid, itemId, answerOpinion, answerHtml, answerPoint, answerNote, answerComment));

			if (page.getTargetRequests().size() == 0)
				System.err.println("answer done...");
		}
	}

	private static Site site = Site.me().setRetryTimes(3);

	@Override
	public Site getSite() {
		if (site == null) {
            site= Site.me().setDomain(Constant.HOMEPAGE_URL + Constant.randomparam())
                    .setSleepTime(10 * 1000)
					.setTimeOut(60 * 1000);
		}
		return site;
	}

	private static synchronized void InsertAnswer(LinkedList<SpQuesItemAnswer> answers) {
		String sql = DBUtil.genInsertSql("com.wilkey.bean.SpQuesItemAnswer");
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DBUtil.openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			pstmt = conn.prepareStatement(sql);
			int size = answers.size() / 1000;
			size = answers.size() % 1000 >= 0 ? size + 1 : size; // 5521,5
			for (int i = 0; i < size; i++) { // 6
				for (int j = 0; j < (i == size - 1 ? answers.size() % 1000 : 1000); j++) {
					int _index = i * 1000 + j;
					pstmt.setString(1, answers.get(_index).getId());
					pstmt.setString(2, answers.get(_index).getAnswerComment());
					pstmt.setString(3, answers.get(_index).getAnswerHtml());
					pstmt.setString(4, answers.get(_index).getAnswerNote());
					pstmt.setString(5, answers.get(_index).getAnswerOpinion());
					pstmt.setString(6, answers.get(_index).getAnswerPoint());
					pstmt.setString(7, answers.get(_index).getItemId());
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
