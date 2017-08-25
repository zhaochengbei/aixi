package store.aixi.mysqlorm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import store.aixi.mysqlconnectionpool.MysqlConnection;
import store.aixi.mysqlconnectionpool.MysqlConnectionPool;
import store.aixi.mysqlconnectionpool.MysqlConnectionPoolException;

/**
 * author：zhaochengbei
 * date：2017/8/14
*/
public class MysqlORM {

	/**
	 * 
	 */
	public MysqlConnectionPool mysqlConnectionPool = new MysqlConnectionPool();//we logic data db；
	/**
	 * 
	 */
	private String recordClassFolder;
	/**
	 * 
	 */
	private String recordClassPackage;
	/**
	 * 
	 */
	public MysqlORM(){
		
	}
	/**
	 * 
	 */
	public void init(String url,String user,String password,int connectionPoolSize,String recordClassFolder,String recordClassPackage)throws Exception{
		this.mysqlConnectionPool.init(url, user, password, connectionPoolSize);
		this.recordClassFolder = recordClassFolder;
		this.recordClassPackage = recordClassPackage;
	}
	/**
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws MysqlConnectionPoolException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws TableParseException 
	 * 
	 */
	public void syncTableDefineToDBStructs() throws SQLException, ClassNotFoundException, IOException, MysqlConnectionPoolException, InstantiationException, IllegalAccessException, TableParseException{
		//连接数据库，拿到所有类，遍历类，拿到对应表名，检查是否包含此表，不包含就创建；
		//拿到表结构遍历类字段，检查表是否包含该字段，将字段放入到对应的位置上；
		MysqlConnection mysqlConnection = this.mysqlConnectionPool.getNotInUseConnection();
		List<Class<?>> classes = ClassUtil.getClassList(this.recordClassPackage);
		//拿到库里有哪些表；
		Map<String, Boolean> hasTable = new HashMap<String, Boolean>();
		//查询表；
		ResultSet resultSet = mysqlConnection.query("show tables;");
		//遍历行,遍历字段,一次打印;
		while (resultSet.next()) {
			String tableName = resultSet.getString(1);
			hasTable.put(tableName, true);			
		}
		resultSet.close();
		//遍历记录类，没有对应的表就创建，如果有表；
		for (int i = 0; i < classes.size(); i++) {
			Class<Record> clazz = (Class<Record>)classes.get(i);
			Record record = clazz.newInstance();
			Table table = record.getTable();
			System.out.println(TableSqlCodeBuilder.buildSqlByTable(table));
			
			if(hasTable.containsKey(table.name) == false){
				//生成sql语句
				String createSql = TableSqlCodeBuilder.buildSqlByTable(table);
				//执行sql
				mysqlConnection.update(createSql);
			}else{
				//拿到建表语句；
				resultSet = mysqlConnection.query("show create table `"+table.name+"`;");
				//根据语句创建表对象；
				resultSet.next();
				String createSql = resultSet.getString(2);
				System.out.println(createSql);
				resultSet.close();
				Table tableInDB = TableSqlCodeBuilder.buildTableBySql(createSql);
				//遍历表属性，栏目，主键，索引，计算差异生成sql；
				//如果表属性有变化，修改表属性；
				if(strEquals(table.engine, tableInDB.engine) == false
						||strEquals(table.charset, tableInDB.charset) == false
						||strEquals(table.collate, tableInDB.collate) == false
						||strEquals(table.comment, tableInDB.comment) == false){
					//生成修改属性语句，并执行；
					String modifySql = "ALTER TABLE "+table.name+" ENGINE "+table.engine+" DEFAULT CHARSET "+table.charset;
					if(table.collate!= null){
						modifySql+=" COLLATE "+table.collate;
					}
					if(table.comment!=null){
						modifySql+=" COMMENT '"+table.comment+"';";
					}
					mysqlConnection.update(modifySql);
				}
				//主键会妨碍修改栏目，所以要先处理；
				//检查主键是否完全相等；
				boolean primaryKeyEquals = true;
				if(table.primaryKeys.length != tableInDB.primaryKeys.length){
					primaryKeyEquals = false;
				}else{
					for (int j = 0; j < table.primaryKeys.length; j++) {
						if(table.primaryKeys[j].equals(tableInDB.primaryKeys[j]) == false){
							primaryKeyEquals = false;
						}
					}
				}
				if(primaryKeyEquals == false){
					//如果之前存在;
					if(tableInDB.primaryKeys.length !=0){
						String primarySql = "ALTER TABLE `"+table.name+"` DROP PRIMARY KEY;";
						mysqlConnection.update(primarySql);
					}
				}
				
				//遍历栏目，如果栏目有变化，修改栏目；
				Map<String, Column> columnsInDBTable = new HashMap<String, Column>();
				for (int j = 0; j < tableInDB.columns.length; j++) {
					columnsInDBTable.put(tableInDB.columns[j].name, tableInDB.columns[j]);
				}
				for (int j = 0; j < table.columns.length; j++) {
					Column column = table.columns[j];
					//如果库里没有这个字段则增加，如果有则修改；
					if(columnsInDBTable.containsKey(column.name) == false){
						//name type 必须有值，其他不必有值；
						String addSql = "ALTER TABLE `"+table.name+"` ADD " +generateColumnSql(column)+";";
						mysqlConnection.update(addSql);
					}else{
						//如果字段不一样则修改；
						Column columnInDBTable = columnsInDBTable.get(column.name);
						if(strEquals(column.columnType, columnInDBTable.columnType) == false
								||strEquals(column.charset, columnInDBTable.charset) == false
								||strEquals(column.collate, columnInDBTable.collate) == false
								||column.nullAble != columnInDBTable.nullAble
								||strEquals(column.defaultValue, columnInDBTable.defaultValue) == false
								||column.autoIncrement != columnInDBTable.autoIncrement
								||strEquals(column.comment, columnInDBTable.comment) ==false){
							String modifySql = "ALTER TABLE `"+table.name+"` CHANGE `"+column.name+"` "+generateColumnSql(column)+";";
							mysqlConnection.update(modifySql);
						}
					}
				}
				if(primaryKeyEquals == false){
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("ALTER TABLE `"+table.name +"` ADD ");
					TableSqlCodeBuilder.addPrimaryKeySql(stringBuilder, table.primaryKeys);
					stringBuilder.append(";");
					mysqlConnection.update(stringBuilder.toString());
				}
				//遍历每个key，如果现在没有对应的key创建，如果有修改;
				Map<String, Key> keysInDBTable = new HashMap<String, Key>();
				for (int j = 0; j < tableInDB.keys.length; j++) {
					keysInDBTable.put(tableInDB.keys[j].name, tableInDB.keys[j]);
				}
				for (int j = 0; j < table.keys.length; j++) {
					Key key = table.keys[j];
					if(keysInDBTable.containsKey(key.name) == false){
						String keySql = generateAddKeySql(table, key);
						mysqlConnection.update(keySql);
					}else{
						Key keyInDBTable = keysInDBTable.get(key.name);
						boolean keyEquals = true;
						if(key.keyType.equals(keyInDBTable.keyType) == false||key.indexMethod.equals(keyInDBTable.indexMethod) == false){
							keyEquals = false;
						}else if(key.fields.length != keyInDBTable.fields.length){
							keyEquals = false;
						}else{
							for (int k = 0; k < key.fields.length; k++) {
								if(key.fields[k].equals(keyInDBTable.fields[k]) == false){
									keyEquals = false;
								}
							}
						}
						if(keyEquals == false){
							String keySql = "ALTER TABLE `"+table.name+"` DROP INDEX `"+key.name+"`;";
							mysqlConnection.update(keySql);
							keySql = generateAddKeySql(table, key);
							mysqlConnection.update(keySql);
						}
					}	
				}	
			}
		}
		mysqlConnectionPool.laybackConncetion(mysqlConnection);
	}
	
	private boolean strEquals(String value1,String value2){
		if(value1 != null&&value2 != null){
			return value1.equals(value2);
		}
		//有一个为null，或者两个都为null；
		return value1 == value2;
	}
	private String generateColumnSql(Column column){
		//name type 必须有值，其他不必有值；
		StringBuilder stringBuilder = new StringBuilder();
		TableSqlCodeBuilder.addColumnSql(stringBuilder, column);
		return stringBuilder.toString();
	}
	private String generateAddKeySql(Table table,Key key){

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("ALTER TABLE `"+table.name +"` ADD ");
		TableSqlCodeBuilder.addKeySql(stringBuilder, key);
		stringBuilder.append(";");
		return stringBuilder.toString();
	}
	/**
	 * @throws MysqlConnectionPoolException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws TableParseException 
	 * 
	 */
	public void generateRecordClassByDBStructs() throws IOException, MysqlConnectionPoolException, ClassNotFoundException, SQLException, TableParseException{
		//读取表结构信息，遍历表，拿到表创建语句，根据语句生成table对象，在根据table对象生成Record类字符串；然后保存；
		MysqlConnection mysqlConnection = this.mysqlConnectionPool.getNotInUseConnection();
		//查询表；
		ResultSet resultSet = mysqlConnection.query("show tables;");
		List<String> tableNames = new ArrayList<String>();
		//遍历行,遍历字段,一次打印;
		while (resultSet.next()) {
			String tableName = resultSet.getString(1);
			tableNames.add(tableName);
		}
		resultSet.close();
		Iterator<String> iterator = tableNames.iterator();
		while (iterator.hasNext()) {
			String tableName = (String) iterator.next();
			resultSet = mysqlConnection.query("show create table `"+tableName+"`;");
			//根据语句创建表对象；
			resultSet.next();
			String createSql = resultSet.getString(2);
			System.out.println(createSql);
			resultSet.close();
			Table tableInDB = TableSqlCodeBuilder.buildTableBySql(createSql);
			String recordClassStr = TableSqlCodeBuilder.generateRecordCodeByTable(tableInDB, recordClassPackage);
			//存储在制定文件夹；
			String className = TableSqlCodeBuilder.getClassNameByTableName(tableName);
			File file = new File(recordClassFolder+"/"+className+".java");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(recordClassStr);
			fileWriter.close();
		}
		resultSet.close();
		mysqlConnectionPool.laybackConncetion(mysqlConnection);
	}

	/**
	 * 
	 */
	public void insertRecord(Record record){
		
	}
	/**
	 * 
	 */
	public void updateRecord(Record record){
		
	}
	/**
	 * 
	 */
	public <T extends Record> T getRecordByPrimaryKeyValues(String[] primaryKeyValues,T recordClass){
		//内部使用getRecordsBySql函数实现；
		return null;
	}
	/**
	 * 
	 */
	public <T extends Record> T getRecordsByPrimaryKeyPartValues(String[] primaryKeyPartValues,T recordClass){
		//内部使用getRecordBySql函数实现；
		return null;
	}
	/**
	 * 
	 */
	public <T extends Record> T getRecordBySql(String sql,T recordClass){
		//内部使用getRecordsBySql函数实现；
		return null;
	}
	/**
	 * 
	 */
	public <T extends Record> T getRecordsBySql(String sql,T recordClass){
		return null;
	}
	/**
	 * 
	 */
	private List<? extends Record> resultSetToRecordList(ResultSet resultSet){
		return null;
	}
	//如果只使用这个库，是不会有这个需求的；
//	/**
//	 * 
//	 */
//	public List<String[]> getPrimaryKeyValuesBySql(String sql){
//		return null;
//	}
}
