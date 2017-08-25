package store.aixi.mysqlorm.record;

import store.aixi.mysqlorm.Column;
import store.aixi.mysqlorm.Key;
import store.aixi.mysqlorm.Record;
import store.aixi.mysqlorm.Table;

/**
 * author: mysqlorm
 * date: Fri Aug 25 17:49:58 CST 2017
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
			column.name = "name2";
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
	public String name2 = "ddd";
	/**
	 *
	 */
	public String name;
}
