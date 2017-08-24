package store.aixi.mysqlorm;
/**
 * author：zhaochengbei
 * date：2017年8月17日
*/
public class TableParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	static public final String UNKOWN = "unkown";
	/**
	 * 
	 */
	public TableParseException(String message){
		super(message);
	}

}
