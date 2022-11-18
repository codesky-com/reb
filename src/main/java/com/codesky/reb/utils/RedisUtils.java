package com.codesky.reb.utils;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Component
public class RedisUtils implements InitializingBean {

	private final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		checkHealth();
	}
	
	private void checkHealth() {
		try {
			String reply = redisTemplate.execute(new RedisCallback<String>() {
				@Override
				public String doInRedis(RedisConnection connection) throws DataAccessException {
					Properties props = connection.info("Server");
					return (props != null) ? props.getProperty("redis_version") : null;
				}
			});
			
			if (reply != null) {
				logger.info("Redis version {}", reply);
			} else {
				logger.error("Redis server not response");
			}
		} catch (Throwable e) {
			logger.error(e.getMessage());
		}
	}

	public boolean exist(String key) {
		try {
			return redisTemplate.hasKey(key);
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}

	public Object get(String key) {
		try {
			return redisTemplate.opsForValue().get(key);
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public boolean set(String key, Object value) {
		try {
			redisTemplate.opsForValue().set(key, value);
			return true;
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}

	public boolean set(String key, Object value, long timeout, TimeUnit unit) {
		try {
			redisTemplate.opsForValue().set(key, value, timeout, unit);
			return true;
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}

	public boolean setIfAbsent(String key, Object value) {
		try {
			return redisTemplate.opsForValue().setIfAbsent(key, value);
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}

	public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
		try {
			return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}

	public Long increment(String key, long delta) {
		try {
			return redisTemplate.opsForValue().increment(key, delta);
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}
	
	public Long decrement(String key, long delta) {
		try {
			return redisTemplate.opsForValue().decrement(key, delta);
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public boolean remove(String key) {
		try {
			return redisTemplate.delete(key);
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}

	public boolean remove(String... keys) {
		try {
			Long result = redisTemplate.delete(Arrays.asList(keys));
			return (result != null && result.longValue() == keys.length);
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}

}
