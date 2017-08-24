package store.aixi.mysqlorm;
/**
 * author：zhaochengbei
 * date：2017/8/16
*/
public class Column {
	/**
	 * name 和 columnType必须有值，两个boolean值有默认值，其他的可以为null；
	 */
	public String name;
	/**
	 * type incloud size unsigned zerofill info
	 */
	public String columnType;
	public String charset;
	public String collate;
	public boolean nullAble = true;
	public String defaultValue;//只有nullable=false时才有效；
	public boolean autoIncrement = false;
	public String comment;
}
