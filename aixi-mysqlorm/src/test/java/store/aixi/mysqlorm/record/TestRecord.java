package store.aixi.mysqlorm.record;

import store.aixi.mysqlorm.Column;
import store.aixi.mysqlorm.Key;
import store.aixi.mysqlorm.Record;
import store.aixi.mysqlorm.Table;

/**
 * author：zhaochengbei
 * date：2017/8/15
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
			column.nullAble = false;
			table.columns[0] = column;
			column = new Column();
			column.name = "name";
			column.columnType = "varchar(50)";
			column.nullAble = false;
			table.columns[1] = column;
			table.primaryKeys = new String[1];
			table.primaryKeys[0] = "id";
			table.keys = new Key[1];
			Key key = new Key();
			key.name = "name";
			key.fields = new String[1];
			key.fields[0] = "name";
			table.keys[0] = key;
			table.engine = "InnoDB";//表属性必须有值；
			table.charset = "utf8";
			table.collate = "utf8_bin";
		}
		return table;
	}
}
