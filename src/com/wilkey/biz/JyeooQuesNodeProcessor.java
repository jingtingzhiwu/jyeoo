package com.wilkey.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.wilkey.bean.SpQuesNode;
import com.wilkey.common.Constant;
import com.wilkey.common.DynamicProxies;
import com.wilkey.db.DBUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author wilkey
 * @mail admin@wilkey.vip
 * @Date 2017年5月19日 下午5:45:00
 * @desc 测试jyeoo所有科目table.jc-tb，试题检索，版本，课本
 */
public class JyeooQuesNodeProcessor implements PageProcessor {
	private static volatile LinkedList<SpQuesNode> nodes = new LinkedList<SpQuesNode>();

	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {

		if (page.getUrl().toString().indexOf(Constant.HOMEPAGE_URL) > -1) {
			List<String> list = page.getHtml().$("div.sub-group a", "href").all();
			for (String string : list) {
				if (StringUtils.isBlank(string) || string.endsWith("report"))
					continue;
				page.addTargetRequest(string);
			}
		} else if (page.getUrl().regex("http://www\\.jyeoo\\.com/(\\w+)/ques/search").match()) { // 取动态章节的筛选属性
			Pattern p = Pattern.compile("http://www\\.jyeoo\\.com/(\\w+)/ques/search");
			Matcher m = p.matcher(page.getUrl().toString());
			if (m.find()) {
				Document doc = Jsoup.parse(page.getHtml().get());
				Elements trs = doc.select(".jc-tb tr");

				Element editiontr = trs.select(".JYE_EDITION").first();

				// 按章节
				for (Element element : editiontr.select("ul>li>a")) {
					String id = element.attr("data-id");
					String txt = element.text();

					Elements bookclassele = trs.select("#tr_" + id);
					if (bookclassele != null && bookclassele.size() > 0) {
						Map<String, String> map = new HashMap<String, String>();
						for (Element book : bookclassele.select("ul>li>a")) {
							String name = book.text();
							String pk = book.attr("onclick");
							pk = pk.substring(pk.lastIndexOf(",'") + 2);
							pk = pk.replaceAll("\\'.*", "");
							map.put(pk, name);
							Request request = new Request(String.format(Constant.QUES_TYPE_URL, m.group(1), pk, 0));
							request.putExtra("subject", m.group(1));
							request.putExtra("bookClass", name);
							request.putExtra("bookVersion", txt);
							request.putExtra("type", "0");
							page.addTargetRequest(request);
						}
						String result = m.group(1) + "," + id + ", " + JSONObject.toJSON(map);
						page.putField("result", result);
					}
				}
				// 按考点
				Request request = new Request(String.format(Constant.QUES_TYPE_URL, m.group(1), "undefined", 1));
				request.putExtra("subject", m.group(1));
				request.putExtra("bookClass", "");
				request.putExtra("bookVersion", "");
				request.putExtra("type", "1");
				page.addTargetRequest(request);
			}
		} else if (page.getUrl().regex("http://www\\.jyeoo\\.com/(\\w+)/ques/partialcategory.*").match()) {
			String subject = (String) page.getRequest().getExtra("subject");
			String bookClass = (String) page.getRequest().getExtra("bookClass");
			String bookVersion = (String) page.getRequest().getExtra("bookVersion");
			String type = (String) page.getRequest().getExtra("type");

			Document doc = Jsoup.parse(page.getHtml().get());
			if (doc == null || doc.select(".treeview") == null)
				return;
			Elements ulTreeChildren = doc.select(".treeview > li");
			for (Element li : ulTreeChildren) {
				addBean(li, null, type, bookVersion, bookClass, subject, 0);
			}
		}

	}

	/**
	 * 递归算法获取node
	 * @param li
	 * @param parentNodeId
	 * @param nodeType
	 * @param bookVersion
	 * @param bookClass
	 * @param subject
	 * @param level	999代表最根级目录
	 */
	public synchronized void addBean(Element li, String parentNodeId, String nodeType, String bookVersion, String bookClass, String subject, int level) {
		try {
			Element node = li.select(">a").first();
			Elements subnodes = li.select(">ul>li");
			nodes.add(new SpQuesNode(UUID.randomUUID().toString(), node.attr("pk"), node.text(), parentNodeId, nodeType, bookVersion, bookClass, subject, subnodes.size() > 0 ? level : 9999));
			if (subnodes.size() > 0) {
				for (Element subnode : subnodes) {
					addBean(subnode, node.attr("pk"), nodeType, bookVersion, bookClass, subject, (level + 1));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) throws InterruptedException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
		DruidDataSource dataSource = (DruidDataSource) context.getBean("dataSource");
		DBUtil.setDataSource(dataSource);
		context.start();
		Thread t = new Thread(new DynamicProxies());
		t.setDaemon(true);
		t.start();
		Thread.sleep(2000);
		Spider sp = Spider.create(new JyeooQuesNodeProcessor()).addUrl(Constant.HOMEPAGE_URL + Constant.randomparam()).thread(25);
		sp.setDownloader(new JsoupDownloader());
		sp.run();
		InsertNode();
		System.err.println("done...");
	}

	private static void InsertNode() {
		synchronized (nodes) {
			String sql = DBUtil.genInsertSql("com.wilkey.bean.SpQuesNode");
			Connection conn = null;
			PreparedStatement pstmt = null;
			try {
				conn = DBUtil.openConnection();
				conn.setAutoCommit(false);
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				pstmt = conn.prepareStatement(sql);
				int size = nodes.size() / 10000;
				size = nodes.size() % 10000 >= 0 ? size + 1 : size; // 5521,5
				for (int i = 0; i < size; i++) { // 6
					for (int j = 0; j < (i == size - 1 ? nodes.size() % 10000 : 10000); j++) {
						int _index = i * 10000 + j;
						pstmt.setString(1, nodes.get(_index).getId());
						pstmt.setString(2, nodes.get(_index).getNodeId());
						pstmt.setInt(3, nodes.get(_index).getNodeLevel());
						pstmt.setString(4, nodes.get(_index).getNodeName());
						if (StringUtils.isBlank(nodes.get(_index).getParentNodeId()))
							pstmt.setNull(5, Types.VARCHAR);
						else
							pstmt.setString(5, nodes.get(_index).getParentNodeId());
						pstmt.setString(6, nodes.get(_index).getNodeType());
						pstmt.setString(7, nodes.get(_index).getBookVersion());
						pstmt.setString(8, nodes.get(_index).getBookClass());
						pstmt.setString(9, nodes.get(_index).getSubject());
						pstmt.addBatch();
					}
					pstmt.executeBatch();
					pstmt.clearBatch();
					conn.commit();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(5);
			} finally {
				if (null != conn)
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
			}
			nodes.clear();
		}
	}
}