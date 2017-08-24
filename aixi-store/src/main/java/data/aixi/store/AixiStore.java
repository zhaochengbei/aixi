package data.aixi.store;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import data.aixi.orm.Record;
import data.aixi.orm.ORM;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;



/**
 * author：zhaochengbei
 * date：2017/8/14
*/
public class AixiStore {
	/**
	 * 
	 */
	public AixiStoreLockManager lockManager =new AixiStoreLockManager();
	/**
	 * 
	 */
	public ConcurrentHashMap<String, Record> cache = new ConcurrentHashMap<String, Record>(300*100);
	/**
	 * 
	 */
	public JedisPool jedisPool = new JedisPool();
	/**
	 * 
	 */
	public ORM orm = new ORM();
	/**
	 * 
	 */
	public AixiStore(){
		
	}

	/**
	 * 
	 */
	public void init(Properties druidConfig,JedisPoolConfig jedisPoolConfig,String pojoFolder,int memCacheSize){
		
	}
	/**
	 * 
	 * @param entityClass
	 * @param primaryKeyValues
	 */
	public Record getEntityByPrimaryKeyValues(Class<Record> entityClass,String[] primaryKeyValues){
		return null;
	}
	/**
	 * 
	 */
	public List<Record> getEntitysByPrimaryKeyValues(Class<Record> entityClass,String[] primaryKeyValues){
		return null;
	}
	/**
	 * 
	 */
	public void insertEntity(Record entity){
		
	}
	/**
	 * 
	 */
	public void updateEntity(Record entity){
		
	}
	
	
}
