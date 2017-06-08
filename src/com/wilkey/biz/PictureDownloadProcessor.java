package com.wilkey.biz;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.util.FileCopyUtils;

/**
 * 保存图片，可以不用Proxy ?
 * 
 * @author wilkey
 * @mail admin@wilkey.vip
 * @Date 2017年5月24日 下午6:13:02
 */
public class PictureDownloadProcessor implements Runnable {

	private String srcpath, destpath;

	public PictureDownloadProcessor() {
	}

	public PictureDownloadProcessor(String srcpath, String destpath) {
		this.srcpath = srcpath;
		this.destpath = destpath;
	}

	@Override
	public void run() {
		if (!srcpath.matches(".*(.jpg|.png|.gif|.bmp)$"))
			return;
		System.out.println(srcpath);
		URL url = null;
		try {
			url = new URL(srcpath);
			URLConnection con = (URLConnection) url.openConnection();
			InputStream input = con.getInputStream();
			FileCopyUtils.copy(input, new FileOutputStream(destpath));
			TimeUnit.SECONDS.sleep(new Random().nextInt(30));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
