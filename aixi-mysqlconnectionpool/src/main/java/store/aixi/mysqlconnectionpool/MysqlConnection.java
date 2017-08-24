package store.aixi.mysqlconnectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * author：zhaochengbei
 * date：2017/8/16
*/
public class MysqlConnection {
	/**
	 * 
	 */
	private String url;
	private String user;
	private String password;
	/**
	 * 
	 */
	private Connection connection;
	private Statement statement;
	/**
	 * 
	 */
	static private final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
	/**
	 * 
	 */
	public MysqlConnection(String url,String user,String password){
		this.url = url;
		this.user = user;
		this.password = password;
	}
	/**
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * 
	 */
	public ResultSet query(String sql) throws ClassNotFoundException, SQLException{
		System.out.println(sql);
		if(connection == null){
			Class.forName(MYSQL_DRIVER_CLASS_NAME);//指定连接类型  
            connection = DriverManager.getConnection(url, user, password);//获取连接
            statement = connection.createStatement();
		}
		try {
			ResultSet resultSet = statement.executeQuery(sql);
			return resultSet;
		} catch (SQLException e) {
			close();
			Class.forName(MYSQL_DRIVER_CLASS_NAME);//指定连接类型  
            connection = DriverManager.getConnection(url, user, password);//获取连接
            statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			return resultSet;
		}
	}
	public int update(String sql) throws ClassNotFoundException, SQLException{
		System.out.println(sql);
		if(connection == null){
			Class.forName(MYSQL_DRIVER_CLASS_NAME);//指定连接类型  
            connection = DriverManager.getConnection(url, user, password);//获取连接
            statement = connection.createStatement();
		}
		try {
			int result = statement.executeUpdate(sql);
			return result;
		} catch (SQLException e) {
			close();
			Class.forName(MYSQL_DRIVER_CLASS_NAME);//指定连接类型  
            connection = DriverManager.getConnection(url, user, password);//获取连接
            statement = connection.createStatement();
			int result = statement.executeUpdate(sql);
			return result;
		}
	}
	/**
	 * @throws SQLException 
	 * 
	 */
	public boolean isClosed() throws SQLException{
		if(connection == null){
			return true;
		}else{
			return connection.isClosed();
		}
	}
	public void close() throws SQLException{
		if(statement.isClosed()==false){
			statement.close();
		}
		if(connection.isClosed()==false){
			connection.close();
		}
	}
}
