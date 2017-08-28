package store.aixi.mysqlorm;

/**
 * Hello world!
 *
 */
public class App 
{
	
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        MysqlORM orm = new MysqlORM();
        try {
			orm.init("jdbc:mysql://localhost:3306/test", "root", "", 1, "E:/git/aixi/aixi-mysqlorm/src/test/java/store/aixi/mysqlorm/record", "store.aixi.mysqlorm.record");
	        orm.syncTableDefineToDBStructs();
//	        orm.generateRecordClassByDBStructs();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
