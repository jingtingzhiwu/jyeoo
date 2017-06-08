package com.wilkey.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wilkey
 * @mail admin@wilkey.vip
 * @Date 2017年1月10日 下午4:25:58
 */
public class PictureDownloadPool {
	private static volatile ThreadPoolExecutor executor = null;

	private PictureDownloadPool() {
	}

	public static ThreadPoolExecutor getInstance(int corePoolSize, int maximumPoolSize, long aliveTime, TimeUnit unit) {
		if (executor == null) {
			BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
			executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, aliveTime, unit, queue);
		}

		return executor;
	}

	public static ThreadPoolExecutor getInstance() {
		if (executor == null) {
			BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
			executor = new ThreadPoolExecutor(5, 5, Long.MAX_VALUE, TimeUnit.SECONDS, queue);
		}

		return executor;
	}
}