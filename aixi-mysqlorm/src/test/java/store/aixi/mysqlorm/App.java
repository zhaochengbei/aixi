package store.aixi.mysqlorm;

import java.util.List;

import store.aixi.mysqlorm.record.TestRecord;

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
//	        orm.syncTableDefineToDBStructs();
//	        orm.generateRecordClassByDBStructs();
//			TestRecord testRecord = new TestRecord();
//			testRecord.id = 2;
//			testRecord.name = "na";
//			testRecord.nameSome = "som2";
//			orm.insertRecord(testRecord);
			TestRecord testRecord = orm.getRecordByPrimaryKeyValues(new String[]{"2"}, TestRecord.class);
			testRecord.nameSome = "som2";
			System.out.println(testRecord);
			orm.updateRecord(testRecord);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
