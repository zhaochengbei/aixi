package store.aixi.mysqlorm;

import java.util.List;

import store.aixi.mysqlorm.record.TestRecord;

/**
 * Hello world!
 *
 */
public class TestSyncClassDefineToDBStructAndCRUD 
{
	
    public static void main( String[] args )
    {
        MysqlORM orm = new MysqlORM();
        try {
			orm.init("jdbc:mysql://localhost:3306/test", "root", "", 2, "E:/git/aixi/aixi-mysqlorm/src/test/java/store/aixi/mysqlorm/record", "store.aixi.mysqlorm.record");
			//
	        orm.syncTableDefineToDBStructs();
	        int dataCount = 1000*1000;
			long time = System.currentTimeMillis();
			for (int i = 0; i < dataCount; i++) {
				//insert
				TestRecord testRecord = new TestRecord();
				testRecord.id = i;
				testRecord.name = String.valueOf(i);
				testRecord.nameSome = "som2";
				orm.insertRecord(testRecord);
			}
			System.out.println("insert cost time="+(System.currentTimeMillis()-time));
			Thread.currentThread().sleep(5000);
			time = System.currentTimeMillis();
			for (int i = 0; i < dataCount; i++) {
				//update
				TestRecord testRecord = orm.getRecordByPrimaryKeyValues(new String[]{String.valueOf(i)}, TestRecord.class);
			}
			System.out.println("query cost time="+(System.currentTimeMillis()-time));
			Thread.currentThread().sleep(5000);
			time = System.currentTimeMillis();
			for (int i = 0; i < dataCount; i++) {
				//update
				TestRecord testRecord = new TestRecord();
				testRecord.name = String.valueOf(i);
				testRecord.nameSome = "som3";
				testRecord.id = testRecord.id;
				orm.updateRecord(testRecord);
			}
			System.out.println("update cost time="+(System.currentTimeMillis()-time));
			Thread.currentThread().sleep(5000);
			time = System.currentTimeMillis();
			for (int i = 0; i < dataCount; i++) {
				TestRecord testRecord = new TestRecord();
				testRecord.name = String.valueOf(i);
				//delete
				orm.deleteRecord(testRecord);
			}
			System.out.println("delete cost time="+(System.currentTimeMillis()-time));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
