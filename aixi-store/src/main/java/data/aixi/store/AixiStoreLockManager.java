package data.aixi.store;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * author：zhaochengbei
 * date：2017/8/14
*/
public class AixiStoreLockManager {
	/**
	 * 
	 */
	private Map<String, Lock> stringKeyLocks = new HashMap<String, Lock>();
	private Map<Long, Lock> longKeyLocks = new HashMap<Long, Lock>();
	/**
	 * 
	 */
	public AixiStoreLockManager(){
		
	}
	
	/**
	 * 
	 */
	public Lock getLock(String id){
		Lock lock = stringKeyLocks.get(id);
		if(lock == null){
			lock = new ReentrantLock();
			stringKeyLocks.put(id, lock);
		}
		return lock;
	}
	/**
	 * 
	 */
	public Lock getLock(Long id){
		Lock lock = longKeyLocks.get(id);
		if(lock == null){
			lock = new ReentrantLock();
			longKeyLocks.put(id, lock);
		}
		return lock;
	}
}
