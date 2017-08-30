package store.aixi.mysqlorm;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * author：zhaochengbei
 * date：2017/8/14
*/
public abstract class Record {
	/**
	 * 
	 */
	public Record(){
		
	}
	/**
	 * 
	 */
	abstract public Table getTable();
	/**
	 * 
	 */
	abstract public String getInsertSql();
	/**
	 * 
	 * @return
	 */
	abstract public String getUpdateSql();
	/**
	 * 
	 * @return
	 */
	abstract public String getDeleteSql();
	
	/**
	 * 
	 */
	abstract public void initValue(ResultSet resultSet) throws SQLException;
}
