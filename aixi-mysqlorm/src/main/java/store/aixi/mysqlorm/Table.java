package store.aixi.mysqlorm;
/**
 * author：zhaochengbei
 * date：2017年8月17日
*/
public class Table {
	/**
	 * collate和comment可以为null，其他的都不可以；
	 */
	public String name;
	public Column[] columns = new Column[0];
	public String[] primaryKeys = new String[0];
	public Key[] keys = new Key[0];
	public String engine="MyISAM";
	public String charset="utf8";
	public String collate;
	public String comment;
}
