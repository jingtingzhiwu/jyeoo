package com.wilkey.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.druid.pool.DruidDataSource;
import com.wilkey.db.DBUtil;

public class JdbcTest {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
		DruidDataSource dataSource = (DruidDataSource) context.getBean("dataSource");
		DBUtil.setDataSource(dataSource);
		context.start();
		InsertNode();
	}

	private static synchronized void InsertNode() {
		String sql = DBUtil.genInsertSql("com.wilkey.bean.SpQuesNode");
		Connection conn = null;
		try {
			conn = DBUtil.openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			int size = 88994 / 10000;
			size = 88994 % 10000 >= 0 ? size + 1 : size; // 5521,5
			for (int i = 0; i < size; i++) { // 6
				for (int j = 0; j < (i == size - 1 ? 88994 % 10000 : 10000); j++) {
					int _index = i * 10000 + j;
					pstmt.setString(1, UUID.randomUUID().toString());
					pstmt.setString(2, UUID.randomUUID().toString());
					pstmt.setString(3, UUID.randomUUID().toString());
					pstmt.setString(4, UUID.randomUUID().toString());
					pstmt.setString(5, UUID.randomUUID().toString());
					pstmt.setString(6, UUID.randomUUID().toString());
					pstmt.setString(7, UUID.randomUUID().toString());
					pstmt.setString(8, UUID.randomUUID().toString());
					pstmt.addBatch();
				}
				pstmt.executeBatch();
				pstmt.clearBatch();
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
}
