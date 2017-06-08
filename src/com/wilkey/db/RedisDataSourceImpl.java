//package com.wilkey.db;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Repository;
//
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisPool;
//
//@Repository("redisDataSource")
//public class RedisDataSourceImpl implements RedisDataSource {
//
//	@Autowired
//	private JedisPool jedisPool;
//
//	public Jedis getRedisClient() {
//		try {
//			Jedis pool = jedisPool.getResource();
//			return pool;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public void returnResource(Jedis jedis) {
//		jedisPool.returnResource(jedis);
//	}
//
//	public void returnResource(Jedis jedis, boolean broken) {
//		if (broken) {
//			jedisPool.returnBrokenResource(jedis);
//		} else {
//			jedisPool.returnResource(jedis);
//		}
//	}
//}