package com.paipianwang.mq.consumer.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.paipianwang.mq.consumer.dao.MailDao;
import com.paipianwang.pat.common.constant.PmsConstant;
import com.paipianwang.pat.common.util.ValidateUtil;
import com.paipianwang.pat.facade.information.entity.PmsMail;
import com.paipianwang.pat.facade.right.util.RedisUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
@Repository(value = "mailDao")
public class MailDaoImpl implements MailDao{

	@Autowired
	private final JedisPool pool = null;
	
	public PmsMail getMailFromRedis(final String type) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			String str = jedis.hget(PmsConstant.MAIL_MAP, type);
			final PmsMail mail = RedisUtils.fromJson(str,PmsMail.class);
			return mail;
		} catch (Exception e) {
			// do something for logger
		} finally {
			if(jedis != null){
				jedis.disconnect();
				jedis.close();
			}
		}
		
		return null;
	}


	public void addMailByRedis(final PmsMail mail) {
		
		if(mail != null){
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				final String str = RedisUtils.toJson(mail);
				if(ValidateUtil.isValid(str)){
					Transaction t = jedis.multi();
					t.hset(PmsConstant.MAIL_MAP, mail.getMailType(), str);
					t.exec();
				}
			} catch (Exception e) {
				// do something for logger
			} finally {
				if(jedis != null){
					jedis.disconnect();
					jedis.close();
				}
			}
		}
	}

	public void removeMailFromRedis(final String type) {
		if(ValidateUtil.isValid(type)){
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				Transaction tx = jedis.multi();
				tx.hdel(PmsConstant.MAIL_MAP,type);
				tx.exec();
			} catch (Exception e) {
				// do something for logger
			} finally {
				if(jedis != null){
					jedis.disconnect();
					jedis.close();
				}
			}
		}
	}
}
