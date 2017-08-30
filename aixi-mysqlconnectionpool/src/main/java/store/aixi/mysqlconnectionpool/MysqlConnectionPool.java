package store.aixi.mysqlconnectionpool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * author：zhaochengbei
 * date：2017/8/16
*/
public class MysqlConnectionPool {

	/**
	 * 
	 */
	public String url;
	public String user;
	public String password;
	/**
	 * 
	 */
	private int poolSize;
	/**
	 * 
	 */
	public MysqlConnection[] allConnections;
	public ArrayList<MysqlConnection> notInUsedConnections = new ArrayList<MysqlConnection>();
	
	/**
	 * 
	 */
	public MysqlConnectionPool(){
		
	}
	/**
	 * return until all connected
	 */
	public synchronized void init(String url,String user,String password,int poolSize){
		this.url = url;
		this.user = user;
		this.password = password;
		this.poolSize = poolSize;
		allConnections = new MysqlConnection[poolSize];
		for (int i = 0; i < allConnections.length; i++) {
			allConnections[i] = new MysqlConnection(url, user, password);
			notInUsedConnections.add(allConnections[i]);
		}
	}
	/**
	 * @throws SQLException 
	 * 
	 */
	public synchronized void distory() throws SQLException{
		for (int i = 0; i < allConnections.length; i++) {
			if(allConnections[i].isClosed() == false){
				allConnections[i].close();
			}
			allConnections = null;
			notInUsedConnections = new ArrayList<MysqlConnection>();
		}
	}
	/**
	 * outside throw exceptions
	 * @throws IOException 
	 * @throws MysqlConnectionPoolException 
	 */
	public synchronized MysqlConnection getNotInUseConnection() throws IOException, MysqlConnectionPoolException{
		if(notInUsedConnections.size()>0){
			return notInUsedConnections.remove(0);
		}
		throw new MysqlConnectionPoolException(MysqlConnectionPoolException.NO_CONNECTION_IN_POOL);
	}
	/**
	 * use finally make true reclaim socket;
	 */
	public synchronized void laybackConncetion(MysqlConnection mysqlConnection){
		notInUsedConnections.add(mysqlConnection);
	}
}
