package store.aixi.mysqlconnectionpool;
/**
 * author：zhaochengbei
 * date：2017年8月16日
*/
public class MysqlConnectionPoolException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public static final String NO_CONNECTION_IN_POOL = "no connection in pool";
	/**
	 * 
	 * @param message
	 */
	public MysqlConnectionPoolException(String message){
		super(message);
	}
	
}
