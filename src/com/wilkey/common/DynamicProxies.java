package com.wilkey.common;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DynamicProxies implements Runnable {
	private final static String order = "";
	private static volatile long gettimes = 0;
	private static List<Proxy> lastIP = new ArrayList<Proxy>();

	public synchronized static Proxy getAProxy() {
		if (lastIP.size() > 0) {
			Proxy ip = lastIP.get(new Random().nextInt(lastIP.size()));
			return ip;
		}
		return null;
	}

	public synchronized static Proxy getAProxy(Integer proxyIndex) {
		if (lastIP.size() > 0) {
			Proxy ip = lastIP.size() < proxyIndex ? lastIP.get(proxyIndex) : lastIP.get(new Random().nextInt(lastIP.size()));
			return ip;
		}
		return null;
	}

	@Override
	public void run() {
		while (true) {

			try {
				java.net.URL url = new java.net.URL("http://api.ip.data5u.com/dynamic/get.html?order=" + order + "&ttl");
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(3000);
				connection.setReadTimeout(3000);
				connection = (HttpURLConnection) url.openConnection();

				InputStream raw = connection.getInputStream();
				InputStream in = new BufferedInputStream(raw);
				byte[] data = new byte[in.available()];
				int bytesRead = 0;
				int offset = 0;
				while (offset < data.length) {
					bytesRead = in.read(data, offset, data.length - offset);
					if (bytesRead == -1) {
						break;
					}
					offset += bytesRead;
				}
				gettimes++;
				in.close();
				raw.close();
				String[] res = new String(data, "UTF-8").split("\n");
				List<Proxy> newip = new ArrayList<Proxy>();
				for (String ip : res) {
					String[] parts = ip.split(",");
					parts = parts[0].split(":");
					if (parts.length == 2) {
						Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(parts[0], Integer.valueOf(parts[1])));
						// System.err.println("new :" + proxy);
						newip.add(proxy);
					}
				}

				if (null != newip && !newip.isEmpty()) {
					lastIP.clear();
					lastIP.addAll(newip);
					// System.out.println(">>>>>>>>>>>>>>第" + gettimes +
					// "次获取动态IP " + lastIP.size() + " 个");
				} else {
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					TimeUnit.SECONDS.sleep(8);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
