package store.aixi.mysqlorm;
/**
 * author：zhaochengbei
 * date：2017/8/17
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
