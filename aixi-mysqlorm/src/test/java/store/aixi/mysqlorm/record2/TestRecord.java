package store.aixi.mysqlorm.record2;

import store.aixi.mysqlorm.Column;
import store.aixi.mysqlorm.Key;
import store.aixi.mysqlorm.Record;
import store.aixi.mysqlorm.Table;

/**
 * author: mysqlorm
 * date: Thu Aug 24 10:15:31 CST 2017
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
			table.columns = new Column[2];
			Column column = new Column();
			column.name = "id";
			column.columnType = "bigint(20)";
			column.charset = null;
			column.collate = null;
			column.nullAble = false;
			column.defaultValue = null;
			column.autoIncrement = false;
			column.comment = null;
			table.columns[0] = column;
			column = new Column();
			column.name = "name";
			column.columnType = "varchar(50)";
			column.charset = null;
			column.collate = "utf8_bin";
			column.nullAble = true;
			column.defaultValue = null;
			column.autoIncrement = false;
			column.comment = null;
			table.columns[1] = column;
			table.primaryKeys = new String[1];
			table.primaryKeys[0] = "id";
			table.keys = new Key[1];
			Key key = new Key();
			key.name = "name";
			key.fields = new String[1];
			key.fields[0] = "name";
			table.keys[0] = key;
			table.engine = "InnoDB";
			table.charset = "utf8";
			table.collate = "utf8_bin";
			table.comment = null;
		}
		return table;
	}
}
