package com.wilkey.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/*
 * 网络爬虫取数据
 * 
 * */
public class PictureTest {
	public static String GetUrl(String inUrl) {
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL(inUrl);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			String temp = "";
			while ((temp = reader.readLine()) != null) {
				// System.out.println(temp);
				sb.append(temp);
			}
		} catch (MalformedURLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static List<String> GetMatcher(String str, String url) {
		List<String> result = new ArrayList<String>();
		Pattern p = Pattern.compile(url);// 获取网页地址
		Matcher m = p.matcher(str);
		while (m.find()) {
			// System.out.println(m.group(1));
			result.add(m.group(1));
		}
		return result;
	}

	public static void main(String args[]) {
		String str = GetUrl("http://www.jyeoo.com/physics/ques/search");
		List<String> ouput = GetMatcher(str, "src=\"([\\w\\s./:]+?)\"");

		for (String aurl : ouput) {
			if(aurl.matches(".*(.jpg|.png|.gif)$"))
			System.out.println(aurl);
			URL url;
			try {
				url = new URL(aurl);
				URLConnection con = (URLConnection) url.openConnection();
				InputStream input = con.getInputStream();
				FileCopyUtils.copy(input, new FileOutputStream("d:\\pic\\" + UUID.randomUUID().toString() + "." + StringUtils.getFilenameExtension(aurl)));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
