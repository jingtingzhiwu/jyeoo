package com.wilkey.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.wilkey.common.Constant;
import com.wilkey.common.Constant.QuestionTypes;
import com.wilkey.db.DBUtil;

public class RegxTest {
	public static void main(String[] args) {
		Pattern p1 = Pattern.compile("(&p=\\d+)");
		Matcher m1 = p1.matcher("http://www.jyeoo.com/search?c=1&t=0&qb=2016&rn=122788&sp=2&s=all&p=5");
		if(m1.find()){
			System.err.println(m1.group(0));
		}
		
	    
		String sql = DBUtil.genInsertSql("com.wilkey.bean.SpReportItem");
		System.err.println(sql);

		Map<String, String> ctkv = JSONObject.parseObject(Constant.QuestionTypes.chinese2.getItemTypeJson(), Map.class);
		List<String> ctvalues = new ArrayList<String>();
		for (Map.Entry<String, String> ctmap : ctkv.entrySet()) {
			String ct = ctmap.getKey();
			if(!"0".equals(ct))
				ctvalues.add(ct);
		}
		System.err.println(StringUtils.join(ctvalues,","));
		
		Pattern p = Pattern.compile("http://www\\.jyeoo\\.com/(\\w+)/ques/partialcategory\\?a=undefined.*");
		Matcher m = p.matcher("http://www.jyeoo.com/math2/ques/partialcategory?a=undefined&f=1&r=0.22942021370986754");
		if(m.find()){
			System.err.println(m.group(1));
		}
		String area = "2014•天津1模";
		 area = area.indexOf("•") != -1 ? area.substring(area.indexOf("•")+1) : area;
		 System.err.println(area);
		while(area.length() >= 2){
			if(Constant.ALL_AREA_STR.indexOf(area) != -1){
				area = Constant.ALL_AREA_STR.substring(Constant.ALL_AREA_STR.indexOf(area), Constant.ALL_AREA_STR.indexOf(area) + 20);
				area = area.substring(0, area.indexOf(","));
				System.err.println(area);
				break;
			}
			area = area.substring(0,area.length()-1);
		}
		
		
		Map map = JSONObject.parseObject(Constant.QuestionTypes.chinese2.getItemTypeJson(), Map.class);
		System.err.println(map.size());
		System.err.println(map.get("1"));
	}
}
