package store.aixi.mysqlorm;

import java.util.List;

import store.aixi.mysqlorm.record.TestRecord;

/**
 * Hello world!
 *
 */
public class TestCRUD 
{
	
    public static void main( String[] args )
    {
        MysqlORM orm = new MysqlORM();
        try {
			orm.init("jdbc:mysql://localhost:3306/test", "root", "", 1, "E:/git/aixi/aixi-mysqlorm/src/test/java/store/aixi/mysqlorm/record", "store.aixi.mysqlorm.record");
			TestRecord testRecord = new TestRecord();
			testRecord.id = 2;
			testRecord.name = "na";
			testRecord.nameSome = "som2";
			orm.insertRecord(testRecord);
			testRecord = orm.getRecordByPrimaryKeyValues(new String[]{"na"}, TestRecord.class);
			testRecord.nameSome = "som2";
			System.out.println(testRecord);
			orm.updateRecord(testRecord);
			orm.deleteRecord(testRecord);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
