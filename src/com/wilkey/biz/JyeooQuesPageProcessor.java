package com.wilkey.biz;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;
import com.wilkey.App;
import com.wilkey.bean.SpQuesItem;
import com.wilkey.bean.SpQuesItemOpinion;
import com.wilkey.bean.SpQuesNode;
import com.wilkey.common.Constant;
import com.wilkey.common.Constant.QuestionTypes;
import com.wilkey.common.PictureDownloadPool;
import com.wilkey.common.QuesPool;
import com.wilkey.db.DBUtil;
import com.wilkey.utils.TreeNode;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;

public class JyeooQuesPageProcessor implements PageProcessor {

	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3);
	private static volatile LinkedList<SpQuesItem> items = new LinkedList<SpQuesItem>();
	private static volatile LinkedList<SpQuesItemOpinion> opinions = new LinkedList<SpQuesItemOpinion>();

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		// 1、查询数据库 sp_subject表，获取所有科目
		// 2、迭代科目，拼接url，获取题目分类http://www.jyeoo.com/english2/ques/partialcategory?a=undefined&f=1&r=0.4777701425650114
		// 3、迭代题目分类
		// 4、迭代题型
		// 5、拼接url获取题目
		if (page.getHtml().get().contains("request too fast, please control the request frequency")) {
			System.err.println("Caution, robot check, request too fast, using PROXY : " + site.getHttpProxy().toHostString());
		}
		
		if (page.getUrl().toString().indexOf(Constant.HOMEPAGE_URL) > -1) {

			/*** get NodeTree from db
			 */
			List<SpQuesNode> spNodeList = new ArrayList<SpQuesNode>();
			Connection conn = null;
			try {
				conn = DBUtil.openConnection();
				spNodeList = DBUtil.queryBeanList(conn, "select * from sp_ques_node where node_level=9999 limit 10", SpQuesNode.class);
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
			/*** get NodeTree from db
			 */
			
			for (SpQuesNode spQuesNode : spNodeList) {

				QuestionTypes type = Constant.QuestionTypes.valueOf(spQuesNode.getSubject());

				//题型
				Map<String, String> mapkv = JSONObject.parseObject(type.getItemTypeJson(), Map.class);
				List<String> itemTypeList = new ArrayList<String>();
				for (Map.Entry<String, String> ctmap : mapkv.entrySet()) {
					String ct = ctmap.getKey();
					if (!"0".equals(ct))
						itemTypeList.add(ct);
				}
				mapkv.clear();
				//难度
				mapkv = JSONObject.parseObject(type.getDifficultyTypeJson(), Map.class);
				List<String> diffTypeList = new ArrayList<String>();
				for (Map.Entry<String, String> ctmap : mapkv.entrySet()) {
					String ct = ctmap.getKey();
					if (!"0".equals(ct))
						diffTypeList.add(ct);
				}
				mapkv.clear();
				//题类
				mapkv = JSONObject.parseObject(type.getQuesTypeJson(), Map.class);
				List<String> quesTypeList = new ArrayList<String>();
				for (Map.Entry<String, String> ctmap : mapkv.entrySet()) {
					String ct = ctmap.getKey();
					if (!"0".equals(ct))
						quesTypeList.add(ct);
				}
				mapkv.clear();
				//来源
				mapkv = JSONObject.parseObject(type.getSourceTypeJson(), Map.class);
				List<String> srouceTypeList = new ArrayList<String>();
				for (Map.Entry<String, String> ctmap : mapkv.entrySet()) {
					String ct = ctmap.getKey();
					if (!"0".equals(ct))
						srouceTypeList.add(ct);
				}

				for (String itemType : itemTypeList) { // 题目的类型：解答题，选择题
					for (String diffType : diffTypeList) {
						for (String quesType : quesTypeList) {
							for (String sourceType : srouceTypeList) {
								Request request = new Request(String.format(Constant.QUES_ITEM_URL + Constant.randomparam(), type.name(), spQuesNode.getNodeId(), itemType, diffType, quesType, sourceType, 1));
								request.putExtra("nodevalue", spQuesNode.getNodeId());
								request.putExtra("nodename", spQuesNode.getNodeName());
								request.putExtra("subject", type);
								request.putExtra("itemType", itemType);
								request.putExtra("diffType", diffType);
								request.putExtra("quesType", quesType);
								request.putExtra("sourceType", sourceType);
								request.putExtra("pageNum", 1);
								page.addTargetRequest(request);
							}
						}
					}
				}
			}
		} else if (page.getUrl().regex("http://www\\.jyeoo\\.com/(\\w+)/ques/partialques.*").match()) {
			// 解析最大页，指定科目、题型、节点下，进行翻页
			Integer pageNum = (Integer) page.getRequest().getExtra("pageNum");
			QuestionTypes type = QuestionTypes.valueOf(page.getRequest().getExtra("subject") + "");
			String nodevalue = (String) page.getRequest().getExtra("nodevalue");
			String nodename = (String) page.getRequest().getExtra("nodename");
			String itemTypeInt = page.getRequest().getExtra("itemType") + "";
			String diffTypeInt = page.getRequest().getExtra("diffType") + "";
			String quesTypeInt = page.getRequest().getExtra("quesType") + "";
			String sourceTypeInt = page.getRequest().getExtra("sourceType") + "";
			Map<String, String> itemtypekv = JSONObject.parseObject(type.getItemTypeJson(), Map.class);
			Map<String, String> difftypekv = JSONObject.parseObject(type.getDifficultyTypeJson(), Map.class);
			Map<String, String> questypekv = JSONObject.parseObject(type.getQuesTypeJson(), Map.class);
			Map<String, String> sourcetypekv = JSONObject.parseObject(type.getSourceTypeJson(), Map.class);
			
			Document doc = Jsoup.parse(page.getHtml().get());
			Elements quesLi = doc.select(".ques-list").select("ul li.ques_li");
			for (Element li : quesLi) {
				String quesGuid = li.select("fieldset").attr("id");
				String title = li.select("div.pt1").text();
				String titleHtml = li.select("div.pt1").html();

				// ******************************处理年份和地区{
				String Area_Years = li.select("div.pt1 >a[target=_blank]").html();
				if (StringUtils.isBlank(Area_Years) && title.indexOf("（") > -1 && title.indexOf("）") > -1)
					Area_Years = title.substring(title.indexOf("（") + 1, title.indexOf("）"));
				String years = Area_Years.replaceAll("[^\\d{4}]", "");
				String district = Area_Years.indexOf("•") != -1 ? Area_Years.substring(Area_Years.indexOf("•") + 1) : Area_Years;
				while (district.length() >= 2) {
					if (Constant.ALL_AREA_STR.indexOf(district) != -1) {
						district = Constant.ALL_AREA_STR.substring(Constant.ALL_AREA_STR.indexOf(district), Constant.ALL_AREA_STR.indexOf(district) + 20);
						district = district.substring(0, district.indexOf(","));
						System.err.println(district);
						break;
					}
					district = district.substring(0, district.length() - 1);
				}
				if(StringUtils.isNotBlank(district) && district.length() ==1)
					district = null;
				// ****************************** }

				String combinationTimes = li.select(".fieldtip").select("label:contains(组卷)").text();
				combinationTimes = StringUtils.isNotBlank(combinationTimes) ? combinationTimes.replaceAll("[^\\d.]", "") : "0";

				String reallyTimes = li.select(".fieldtip").select("label:contains(真题)").text();
				reallyTimes = StringUtils.isNotBlank(reallyTimes) ? reallyTimes.replaceAll("[^\\d.]", "") : "0";

				String difficultyValue = li.select(".fieldtip").select("label:contains(难度)").text();
				difficultyValue = StringUtils.isNotBlank(difficultyValue) ? difficultyValue.replaceAll("[^\\d.]", "") : "0";

				String itemType = itemtypekv != null && !itemtypekv.isEmpty() ? (String) itemtypekv.get(itemTypeInt) : "";

				String diffType = difftypekv != null && !difftypekv.isEmpty() ? (String) difftypekv.get(diffTypeInt) : "";

				String quesType = questypekv != null && !questypekv.isEmpty() ? (String) questypekv.get(quesTypeInt) : "";

				String sourceType = sourcetypekv != null && !sourcetypekv.isEmpty() ? (String) sourcetypekv.get(sourceTypeInt) : "";
				
				String itemId = UUID.randomUUID().toString();
				
				String localpath = "";
				//保存题目图片, ext, path
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
						opinions.add(new SpQuesItemOpinion(opinionGuid, itemId, selectoptionhtml));
					}
				}
				// ****************************** }
				items.add(new SpQuesItem(itemId, nodevalue, type.name(), quesGuid, title, titleHtml, StringUtils.isBlank(combinationTimes) ? null : Integer.valueOf(combinationTimes),
						StringUtils.isBlank(reallyTimes) ? null : Integer.valueOf(reallyTimes), StringUtils.isBlank(difficultyValue) ? null : Double.valueOf(difficultyValue),
						StringUtils.isBlank(years) ? null : Integer.valueOf(years), district, itemType, diffType, quesType, sourceType, localpath));

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

			// 下一页
			if (pageNum == 1) {
				String maxpageStr = doc.select("#pchvbe").text();
				int maxpage = StringUtils.isNotBlank(maxpageStr) ? Integer.valueOf(maxpageStr) / 10 + 1 : 1;
				for (int i = 2; i <= maxpage; i++) {
					Request request = new Request(String.format(Constant.QUES_ITEM_URL + Constant.randomparam(), type.name(), nodevalue, itemTypeInt, diffTypeInt, quesTypeInt, sourceTypeInt, i));
					request.putExtra("nodevalue", nodevalue);
					request.putExtra("nodename", nodename);
					request.putExtra("subject", type);
					request.putExtra("itemType", itemTypeInt);
					request.putExtra("diffType", diffTypeInt);
					request.putExtra("quesType", quesTypeInt);
					request.putExtra("sourceType", sourceTypeInt);
					request.putExtra("pageNum", i);
					page.addTargetRequest(request);
				}

				if (page.getTargetRequests().size() == 0) {
					System.err.println("ques done...");
				}
			}
		}
	}

	// 递归转TreeNode
	private void recursiveNode(Element parent, String parentId, TreeNode parentNode, LinkedHashMap<String, String> quesNodeMap) {
		Elements parent1 = parent.select("> ul > li") == null || parent.select("> ul > li").size() < 1 ? null : parent.select("> ul > li");
		if (parent1 == null) {
			TreeNode t = parentNode;
			LinkedList<String> comp = new LinkedList<String>();
			while (t != null) {
				if (StringUtils.isBlank(t.getNodeName()))
					break;
				comp.add(t.getNodeName());
				t = t.getParentNode();
			}
			Collections.reverse(comp);
			quesNodeMap.put(StringUtils.join(comp.toArray(), " -> "), parentId);
			return;
		}
		for (Element li : parent1) {
			
			String pk = li.select(">a").attr("pk");
			String nodename = li.select(">a").text();
			TreeNode treeNode = new TreeNode();
			treeNode.setSelfId(pk);
			treeNode.setNodeName(nodename);
			treeNode.setParentNode(parentNode);
			treeNode.setParentId(parentId);
			if (!"root".equals(parentNode.getSelfId()))
				parentNode.addChildNode(treeNode);

			recursiveNode(li, treeNode.getSelfId(), treeNode, quesNodeMap);
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

	public static void main(String[] args) throws InterruptedException, SQLException, JMException {
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
		Spider sp = Spider.create(new JyeooQuesPageProcessor()).addUrl(Constant.HOMEPAGE_URL + Constant.randomparam()).thread(1);
		sp.setUUID("QuesSpider");
		sp.setDownloader(new JsoupDownloader());
		sp.setExecutorService(QuesPool.getInstance());
		SpiderMonitor.instance().register(sp);
		sp.start();
		Thread.sleep(1000 * 60 * 1);
		//抓完ques，才去抓answer，因此start阻塞线程
	}

	private static synchronized void InsertItemOpinion(LinkedList<SpQuesItemOpinion> opinions) {
		String sql = DBUtil.genInsertSql("com.wilkey.bean.SpQuesItemOpinion");
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

	private static synchronized void InsertItem(LinkedList<SpQuesItem> items) {
		String sql = DBUtil.genInsertSql("com.wilkey.bean.SpQuesItem");
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
					pstmt.setString(2, items.get(_index).getNodeId());
					
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
					pstmt.setString(8, items.get(_index).getDifficultyType());
					pstmt.setString(9, items.get(_index).getQuesType());
					pstmt.setString(10, items.get(_index).getSourceType());
					pstmt.setString(11, items.get(_index).getPicPath());
					
					if (null == items.get(_index).getReallyTimes())
						pstmt.setNull(12, Types.INTEGER);
					else
						pstmt.setInt(12, items.get(_index).getReallyTimes());
					
					pstmt.setString(13, items.get(_index).getSubject());
					pstmt.setString(14, items.get(_index).getTitle());
					pstmt.setString(15, items.get(_index).getTitleHtml());

					if (null == items.get(_index).getYears())
						pstmt.setNull(16, Types.INTEGER);
					else
						pstmt.setInt(16, items.get(_index).getYears());
					
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