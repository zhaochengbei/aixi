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
	private Map<Class<? extends Record>, Table> classToInstance = new HashMap<Class<? extends Record>, Table>();
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
		//get connection,get all classes,get all tables name.
		MysqlConnection mysqlConnection = this.mysqlConnectionPool.getNotInUseConnection();
		List<Class<?>> classes = ClassUtil.getClassList(this.recordClassPackage);
		Map<String, Boolean> hasTable = new HashMap<String, Boolean>();
		ResultSet resultSet = mysqlConnection.query("show tables;");
		while (resultSet.next()) {
			String tableName = resultSet.getString(1);
			hasTable.put(tableName, true);			
		}
		resultSet.close();
		//loop classes ,according table object and table struct create sql and execute sql.
		for (int i = 0; i < classes.size(); i++) {
			Class<Record> clazz = (Class<Record>)classes.get(i);
			Record record = clazz.newInstance();
			Table table = record.getTable();
			System.out.println("table object one-to-one match create sql:");
			System.out.println(TableSqlCodeBuilder.buildSqlByTable(table));
			//if not match struct. create sql by table.
			if(hasTable.containsKey(table.name) == false){
				String createSql = TableSqlCodeBuilder.buildSqlByTable(table);
				mysqlConnection.update(createSql);
			}else{
				//if has match table struct ,create sql by different between table object and table struct.
				resultSet = mysqlConnection.query("SHOW CREATE TABLE `"+table.name+"`;");
				resultSet.next();
				String createSql = resultSet.getString(2);
				System.out.println("table struct in database create sql:");
				System.out.println(createSql);
				resultSet.close();
				Table tableInDB = TableSqlCodeBuilder.buildTableBySql(createSql);
				//loop table property and column and primary key and keys. general sql by different.
				if(strEquals(table.engine, tableInDB.engine) == false
						||strEquals(table.charset, tableInDB.charset) == false
						||strEquals(table.collate, tableInDB.collate) == false
						||strEquals(table.comment, tableInDB.comment) == false){
					String modifySql = "ALTER TABLE "+table.name+" ENGINE "+table.engine+" DEFAULT CHARSET "+table.charset;
					if(table.collate!= null){
						modifySql+=" COLLATE "+table.collate;
					}
					if(table.comment!=null){
						modifySql+=" COMMENT '"+table.comment+"';";
					}
					mysqlConnection.update(modifySql);
				}
				//primary key effect column modify,so Advance process. 
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
					if(tableInDB.primaryKeys.length !=0){
						String primarySql = "ALTER TABLE `"+table.name+"` DROP PRIMARY KEY;";
						mysqlConnection.update(primarySql);
					}
				}
				//process columns
				Map<String, Column> columnsInDBTable = new HashMap<String, Column>();
				for (int j = 0; j < tableInDB.columns.length; j++) {
					columnsInDBTable.put(tableInDB.columns[j].name, tableInDB.columns[j]);
				}
				for (int j = 0; j < table.columns.length; j++) {
					Column column = table.columns[j];
					if(columnsInDBTable.containsKey(column.name) == false){
						String addSql = "ALTER TABLE `"+table.name+"` ADD " +generateColumnSql(column)+";";
						mysqlConnection.update(addSql);
					}else{
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
				//process keys
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
	/**
	 * 
	 * @param value1
	 * @param value2
	 * @return
	 */
	private boolean strEquals(String value1,String value2){
		if(value1 != null&&value2 != null){
			return value1.equals(value2);
		}
		return value1 == value2;
	}
	/**
	 * 
	 * @param column
	 * @return
	 */
	private String generateColumnSql(Column column){
		StringBuilder stringBuilder = new StringBuilder();
		TableSqlCodeBuilder.addColumnSql(stringBuilder, column);
		return stringBuilder.toString();
	}
	/**
	 * 
	 * @param table
	 * @param key
	 * @return
	 */
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
		// get all tables name.
		MysqlConnection mysqlConnection = this.mysqlConnectionPool.getNotInUseConnection();
		ResultSet resultSet = mysqlConnection.query("show tables;");
		List<String> tableNames = new ArrayList<String>();
		while (resultSet.next()) {
			String tableName = resultSet.getString(1);
			tableNames.add(tableName);
		}
		resultSet.close();
		// loop all tables name.
		Iterator<String> iterator = tableNames.iterator();
		while (iterator.hasNext()) {
			String tableName = (String) iterator.next();
			resultSet = mysqlConnection.query("SHOW CREATE TABLE `"+tableName+"`;");
			resultSet.next();
			String createSql = resultSet.getString(2);
			resultSet.close();
			System.out.println("create sql of table struct in database:");
			System.out.println(createSql);
			//sql convert to table object,table object convert to record class string. 
			Table tableInDB = TableSqlCodeBuilder.buildTableBySql(createSql);
			String recordClassStr = TableSqlCodeBuilder.generateRecordCodeByTable(tableInDB, recordClassPackage);
			//save in desk.
			String className = TableSqlCodeBuilder.getClassNameByTableName(tableName);
			File file = new File(recordClassFolder+"/"+className+".java");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(recordClassStr);
			fileWriter.close();
		}
		mysqlConnectionPool.laybackConncetion(mysqlConnection);
	}

	/**
	 * @throws MysqlConnectionPoolException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	public void insertRecord(Record record) throws IOException, MysqlConnectionPoolException, ClassNotFoundException, SQLException{
		//general code has best proformance.
		String sql = record.getInsertSql();
		MysqlConnection mysqlConnection = mysqlConnectionPool.getNotInUseConnection();
		mysqlConnection.update(sql);
		mysqlConnectionPool.laybackConncetion(mysqlConnection);

	}
	/**
	 * @throws MysqlConnectionPoolException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	public void updateRecord(Record record) throws IOException, MysqlConnectionPoolException, ClassNotFoundException, SQLException{
		String sql = record.getUpdateSql();
		MysqlConnection mysqlConnection = mysqlConnectionPool.getNotInUseConnection();
		mysqlConnection.update(sql);
		mysqlConnectionPool.laybackConncetion(mysqlConnection);

	}
	/**
	 * 
	 */
	public void deleteRecord(Record record) throws IOException, MysqlConnectionPoolException, ClassNotFoundException, SQLException{
		String sql = record.getDeleteSql();
		MysqlConnection mysqlConnection = mysqlConnectionPool.getNotInUseConnection();
		mysqlConnection.update(sql);
		mysqlConnectionPool.laybackConncetion(mysqlConnection);
	}
	/**
	 * @throws MysqlORMException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SQLException 
	 * @throws MysqlConnectionPoolException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	public <T extends Record> T getRecordByPrimaryKeyValues(String[] primaryKeyValues,Class<T> recordClass) throws MysqlORMException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, MysqlConnectionPoolException, SQLException{
		List<T> records= getRecordsByPrimaryKeyPartValues(primaryKeyValues, recordClass);
		if(records.size()>1){
			throw new MysqlORMException("not only one record");
		}
		if(records.size() == 0){
			return null;
		}
		return records.get(0);
	}
	/**
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SQLException 
	 * @throws MysqlConnectionPoolException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	public <T extends Record> List<T> getRecordsByPrimaryKeyPartValues(String[] primaryKeyPartValues,Class<T> recordClass) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, MysqlConnectionPoolException, SQLException{
		//only can use loop ,because not know how many part will transmit.
		Table table = getTableByClass(recordClass);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT * FROM `");
		stringBuilder.append(table.name);
		stringBuilder.append("` WHERE ");
		for (int i = 0; i < primaryKeyPartValues.length; i++) {
			String primaryKey = table.primaryKeys[i];
			stringBuilder.append("`");
			stringBuilder.append(primaryKey);
			stringBuilder.append("`='");
			stringBuilder.append(primaryKeyPartValues[i]);
			stringBuilder.append("'");
			//
			if(i<primaryKeyPartValues.length-1){
				stringBuilder.append(" and ");
			}
		}
		stringBuilder.append(";");
		List<T> records = getRecordsBySql(stringBuilder.toString(), recordClass);
		return records;
	}
	/**
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * 
	 */
	private <T extends Record>  Table getTableByClass(Class<T> recordClass) throws InstantiationException, IllegalAccessException{
		if(classToInstance.containsKey(recordClass) == false){
			classToInstance.put(recordClass, recordClass.newInstance().getTable());
		}
		return classToInstance.get(recordClass);
	}
	/**
	 * @throws MysqlORMException 
	 * @throws SQLException 
	 * @throws MysqlConnectionPoolException 
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	public <T extends Record> T getRecordBySql(String sql,Class<T> recordClass) throws MysqlORMException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, MysqlConnectionPoolException, SQLException{
		List<T> records = getRecordsBySql(sql, recordClass);
		if(records.size()>1){
			throw new MysqlORMException("not only one record");
		}
		if(records.size() == 0){
			return null;
		}
		return records.get(0);
	}
	/**
	 * @throws MysqlConnectionPoolException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * 
	 */
	public <T extends Record> List<T> getRecordsBySql(String sql,Class<T> recordClass) throws IOException, MysqlConnectionPoolException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException{
		MysqlConnection mysqlConnection = mysqlConnectionPool.getNotInUseConnection();
		ResultSet resultSet = mysqlConnection.query(sql);
		List<T> records = resultSetToRecordList(resultSet,recordClass);
		mysqlConnectionPool.laybackConncetion(mysqlConnection);
		return records;
	}
	/**
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * 
	 */
	private <T extends Record>  List<T> resultSetToRecordList(ResultSet resultSet,Class<T> recordClass) throws InstantiationException, SQLException, IllegalAccessException{
		List<T> list = new ArrayList<T>();
		while(resultSet.next()){
			T record = recordClass.newInstance();
			record.initValue(resultSet);
			list.add(record);
		}
		return list;
	}
}
