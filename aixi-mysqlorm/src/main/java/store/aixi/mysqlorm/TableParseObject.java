package store.aixi.mysqlorm;
/**
 * author：zhaochengbei
 * date：2017/8/17
*/
public class TableParseObject {
	/**
	 * 
	 */
	static public final int CRATE_TABLE = 0;
	static public final int NAME = 1;
	static public final int ELEMENT = 2;//可能是column，也可能是键；可能是column，也可能是键；
	static public final int COLUMN = 3;
	static public final int DATA_TYPE = 4;
	static public final int COLUMN_ATT = 5;//发现unsigned,zerofull,not null 直接处理，不回进入第二阶段；
	static public final int CHARSET = 6;
	static public final int COLLATE = 7;
	static public final int DEFAULT_VALUE = 8;
	static public final int AUTO_INCREMENT = 9;
	static public final int COMMENT = 10;
	static public final int PRIMARY_KEY = 11;
	static public final int KEY = 12;
	static public final int FIELD = 13;
	static public final int TABLE_ATT = 14;
	static public final int ENGINE = 15;
}
