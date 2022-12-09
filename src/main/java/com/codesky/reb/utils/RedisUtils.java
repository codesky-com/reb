package com.codesky.reb.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component("redisUtils2")
public class RedisUtils implements InitializingBean {

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
				LoggerUtils.log(this.getClass(),"Redis version", reply);
			} else {
				LoggerUtils.error(this.getClass(),"checkHealth",new Throwable(),"Redis server not response");
			}
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"checkHealth",e,"error");
		}
	}

	public boolean exist(String key) {
		try {
			return redisTemplate.hasKey(key);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"exist",e,key);

		}
		return false;
	}

	public Object get(String key) {
		try {
			return redisTemplate.opsForValue().get(key);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"get",e,key);
		}
		return null;
	}

	public <T> T get(String key,Class<T> clazz) {
		try {
			T t = (T) redisTemplate.opsForValue().get(key);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"get",e,key,clazz);
		}
		return null;
	}

	public boolean set(String key, Object value) {
		try {
			redisTemplate.opsForValue().set(key, value);
			return true;
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"set",e,key,value);
		}
		return false;
	}

	public boolean set(String key,Object value,long timeout) {
		try {
			redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"set",e,key,value,timeout);
		}
		return false;
	}

	public boolean set(String key, Object value, long timeout, TimeUnit unit) {
		try {
			redisTemplate.opsForValue().set(key, value, timeout, unit);
			return true;
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"set",e,key,value,timeout,unit);

		}
		return false;
	}

	public boolean add(String key, Object value) {
		try {
			redisTemplate.opsForValue().setIfAbsent(key, value);
			return true;
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"add",e,key,value);

		}
		return false;
	}

	public boolean add(String key, Object value, long cacheSec) {
		try {
			redisTemplate.opsForValue().setIfAbsent(key, value, cacheSec, TimeUnit.SECONDS);
			return true;
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"add",e,key,value,cacheSec);

		}
		return false;
	}

	public boolean setIfAbsent(String key, Object value) {
		try {
			return redisTemplate.opsForValue().setIfAbsent(key, value);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"setIfAbsent",e,key,value);
		}
		return false;
	}

	public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
		try {
			return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"setIfAbsent",e,key,value,timeout,unit);
		}
		return false;
	}

	public Long increment(String key, long delta) {
		try {
			return redisTemplate.opsForValue().increment(key, delta);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"increment",e,key,delta);
		}
		return null;
	}
	
	public Long decrement(String key, long delta) {
		try {
			return redisTemplate.opsForValue().decrement(key, delta);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"decrement",e,key,delta);
		}
		return null;
	}

	public boolean remove(String key) {
		try {
			return redisTemplate.delete(key);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"remove",e,"remove",key);
		}
		return false;
	}

	public boolean remove(String... keys) {
		try {
			Long result = redisTemplate.delete(Arrays.asList(keys));
			return (result != null && result.longValue() == keys.length);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"remove",e,keys);
		}
		return false;
	}

	public Object hGet(String name, String key) {
		try {
			HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
			return opsForHash.get(name, key);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"hGet",e,name,key);
		}
		return  null;
	}

	public boolean hAdd(String name, Object key, Object bean) {
		try	{
			HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
			return opsForHash.putIfAbsent(name, key, bean);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"hAdd",e,name,key,bean);
		}
		return false;
	}

	public boolean hSet(String name, Object key, Object bean) {
		try	{
			HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
			opsForHash.put(name, key, bean);
			return true;
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"hSet",e,name,key,bean);
		}
		return false;
	}

	public void expire(String key,Long value) {
		try {
			redisTemplate.expire(key,value,TimeUnit.SECONDS);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"expire",e,key,value);
		}
	}

	public void leftPushAll(String key, List<Object> list) {
		try {
			ListOperations<String, Object> listOps = redisTemplate.opsForList();
			listOps.leftPushAll(key,list);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"leftPushAll",e,key,list);
		}
	}

	public Object leftPop(String key) {
		try {
			ListOperations<String, Object> listOps = redisTemplate.opsForList();
			return listOps.leftPop(key);
		} catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"leftPop",e,key);
		}
		return null;
	}

	public List<Object> leftPop(String key,Long count) {
		try {
			ListOperations<String, Object> listOps = redisTemplate.opsForList();
			return  listOps.leftPop(key,count);
		}catch (Throwable e) {
			LoggerUtils.error(this.getClass(),"leftPop",e,key,count);

		}
		return null;
	}

}
