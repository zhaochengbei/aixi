package store.aixi.mysqlorm;
/**
 * author：zhaochengbei
 * date：2017/8/16
*/
public class Column {
	/**
	 * name and  columnType must has value,boolean property has default value false,other property has default value null.
	 */
	public String name;
	/**
	 * type incloud size unsigned zerofill info
	 */
	public String columnType;
	public String charset;
	public String collate;
	public boolean nullAble = true;
	public String defaultValue;//only nullable=false has effect.
	public boolean autoIncrement = false;
	public String comment;
}
