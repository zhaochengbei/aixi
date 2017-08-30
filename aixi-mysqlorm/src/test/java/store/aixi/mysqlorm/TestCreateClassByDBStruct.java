package store.aixi.mysqlorm;

import store.aixi.mysqlorm.record.TestRecord;

/**
 * author：zhaochengbei
 * date：2017年8月28日
*/
public class TestCreateClassByDBStruct {
	public static void main( String[] args )
    {
        MysqlORM orm = new MysqlORM();
        try {
			orm.init("jdbc:mysql://localhost:3306/test", "root", "", 1, "E:/git/aixi/aixi-mysqlorm/src/test/java/store/aixi/mysqlorm/record", "store.aixi.mysqlorm.record");
	        orm.syncTableDefineToDBStructs();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
