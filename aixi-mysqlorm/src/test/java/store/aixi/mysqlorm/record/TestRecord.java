package store.aixi.mysqlorm.record;

import store.aixi.mysqlorm.Column;
import store.aixi.mysqlorm.Key;
import store.aixi.mysqlorm.Record;
import store.aixi.mysqlorm.Table;

import java.sql.ResultSet;

import java.sql.SQLException;

/**
 * author: mysqlorm
 * date: Mon Aug 28 17:19:43 CST 2017
 */
public class TestRecord extends Record {
	/**
	 *
	 */
	static private Table table;
	/**
	 *
	 */
	public Table getTable(){
		if(table == null){
			table = new Table();
			table.name = "t_test";
			table.columns = new Column[3];
			Column column = new Column();
			column.name = "id";
			column.columnType = "bigint(20)";
			column.charset = null;
			column.collate = null;
			column.nullAble = false;
			column.defaultValue = "100";
			column.autoIncrement = false;
			column.comment = null;
			table.columns[0] = column;
			column = new Column();
			column.name = "name_some";
			column.columnType = "varchar(50)";
			column.charset = "utf8";
			column.collate = "utf8_bin";
			column.nullAble = false;
			column.defaultValue = "ddd";
			column.autoIncrement = false;
			column.comment = null;
			table.columns[1] = column;
			column = new Column();
			column.name = "name";
			column.columnType = "varchar(50)";
			column.charset = "utf8";
			column.collate = "utf8_bin";
			column.nullAble = false;
			column.defaultValue = null;
			column.autoIncrement = false;
			column.comment = null;
			table.columns[2] = column;
			table.primaryKeys = new String[1];
			table.primaryKeys[0] = "name";
			table.keys = new Key[1];
			Key key = new Key();
			key.keyType = "UNIQUE KEY";
			key.name = "name";
			key.fields = new String[1];
			key.fields[0] = "name";
			key.indexMethod = "BTREE";
			table.keys[0] = key;
			table.engine = "MyISAM";
			table.charset = "utf8";
			table.collate = null;
			table.comment = null;
		}
		return table;
	}
	/**
	 *
	 */
	public long id = 100;
	/**
	 *
	 */
	public String nameSome = "ddd";
	/**
	 *
	 */
	public String name;
	/**
	 *
	 */
	public String getInsertSql(){
		String sql="INSERT INTO `t_test` (`id`,`name_some`,`name`) VALUES('"+id+"','"+nameSome+"','"+name+"');";
		return sql;
	}
	/**
	 *
	 */
	public String getUpdateSql(){
		String sql="UPDATE `t_test` SET `id`='"+id+"',`name_some`='"+nameSome+"',`name`='"+name+"' WHERE `name`='"+name+"';";
		return sql;
	}
	/**
	 *
	 */
	public String getDeleteSql(){
		String sql="DELETE FROM `t_test` WHERE `name`='"+name+"';";
		return sql;
	}
	/**
	 *
	 */
	public void initValue(ResultSet resultSet) throws SQLException{
		id=resultSet.getLong("id");
		nameSome=resultSet.getString("name_some");
		name=resultSet.getString("name");
	}
	/**
	 *
	 */
	public String toString(){
		String str="TestRecord@"+Integer.toHexString(hashCode())+"[id="+id+",nameSome="+nameSome+",name="+name+"]";
		return str;
	}
}
