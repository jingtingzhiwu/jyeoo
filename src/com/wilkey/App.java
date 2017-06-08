package com.wilkey;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;
import com.wilkey.biz.JyeooReportDetailProcessor;
import com.wilkey.biz.JyeooReportTypeProcessor;
import com.wilkey.common.DynamicProxies;
import com.wilkey.db.DBUtil;

import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.scheduler.RedisScheduler;

@Component
public class App {
	private static RedisScheduler redisScheduler;

	public static void main(String[] args) throws IOException, InterruptedException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
		DruidDataSource dataSource = (DruidDataSource) context.getBean("dataSource");
		DBUtil.setDataSource(dataSource);
		JedisPool jedisPool = (JedisPool) context.getBean("jedisPool");
		App.redisScheduler = new RedisScheduler(jedisPool);
		context.start();
		Thread t = new Thread(new DynamicProxies());
		t.setDaemon(true);
		t.start();
		System.err.println("starting......");
		System.in.read();
	}

	@Scheduled(fixedRate = 1000 * 60 * 60 * 24 * 30L)
	public void run() {
		try {
//			JyeooQuesPageProcessor.main(null);
//			JyeooAnswerPageProcessor.main(null);
			JyeooReportTypeProcessor.main(null);
			JyeooReportDetailProcessor.main(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static synchronized RedisScheduler getRedisScheduler() {
		return redisScheduler;
	}

}
