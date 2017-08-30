package store.aixi.mysqlorm;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import javax.print.attribute.standard.PrinterMakeAndModel;

import com.mysql.jdbc.Blob;

/**
 * author：zhaochengbei
 * date：2017/8/17
*/
public class TableSqlCodeBuilder {

	/**
	 * @throws TableParseException 
	 */
	static public Table buildTableBySql(String sql) throws TableParseException{
		Table table = new Table();

		List<Column> columns = new ArrayList<Column>();
		List<String> primaryKeys = new ArrayList<String>();
		List<Key> keys = new ArrayList<Key>();
		List<String> fieldInKey = new ArrayList<String>();
		Column column = null;
		Key key = null;
		Stack<Integer> parseObjectStack = new Stack<Integer>();
		StringBuilder bufferArea = new StringBuilder();
		int parsePhase = TableParseObjectPhase.WAIT_START;
		//first parse CRATE TABLE.
		parseObjectStack.push(TableParseObject.CRATE_TABLE);
		for (int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			
			switch(parseObjectStack.get(parseObjectStack.size()-1)){
			case TableParseObject.CRATE_TABLE:
				if(c == '`'){
					if(bufferArea.toString().indexOf("CREATE TABLE") == -1){
						throw new TableParseException(TableParseException.UNKOWN);
					}
					
					bufferArea.delete(0, bufferArea.length());
					
					parseObjectStack.pop();
					parseObjectStack.push(TableParseObject.NAME);
					parsePhase = TableParseObjectPhase.WAIT_END;
				}else{
					bufferArea.append(c);
				}
				break;
			case TableParseObject.NAME:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == '`'){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
				}else{
					if(c == '`'){
						String name = bufferArea.toString();
						bufferArea.delete(0, bufferArea.length());
						if(name.isEmpty()){
							throw new TableParseException(TableParseException.UNKOWN);
						}
						if(parseObjectStack.size() == 1){
							table.name = name; 
							parseObjectStack.pop();
							parseObjectStack.push(TableParseObject.ELEMENT);
						}else if (parseObjectStack.get(parseObjectStack.size()-2) ==TableParseObject.COLUMN){
							column.name = name;
							parseObjectStack.pop();
							parseObjectStack.push(TableParseObject.DATA_TYPE);
							parsePhase = TableParseObjectPhase.WAIT_START;
						}else if(parseObjectStack.get(parseObjectStack.size()-2) == TableParseObject.KEY){
							key.name = name;
							parseObjectStack.pop();
							fieldInKey.clear();
							parseObjectStack.push(TableParseObject.FIELD);
							parsePhase = TableParseObjectPhase.WAIT_START;
						}
					}else{
						bufferArea.append(c);
					}
				}
				break;
			case TableParseObject.ELEMENT:
				if(c == '`'){
					parseObjectStack.push(TableParseObject.COLUMN);
					column = new Column();
					columns.add(column);
					parseObjectStack.push(TableParseObject.NAME);
					parsePhase = TableParseObjectPhase.WAIT_END;
				}
				if(c =='P'){
					parseObjectStack.push(TableParseObject.PRIMARY_KEY);
					parsePhase = TableParseObjectPhase.WAIT_START;
				}
				if(c == 'K'){
					parseObjectStack.push(TableParseObject.KEY);
					key = new Key();
					key.keyType = "KEY";
					keys.add(key);
					parseObjectStack.push(TableParseObject.NAME);
					parsePhase = TableParseObjectPhase.WAIT_START;
				}
				if(c == 'U'){
					parseObjectStack.push(TableParseObject.KEY);
					key = new Key();
					key.keyType = "UNIQUE KEY";
					keys.add(key);
					parseObjectStack.push(TableParseObject.NAME);
					parsePhase = TableParseObjectPhase.WAIT_START;
				}
				if(c == 'F'){
					parseObjectStack.push(TableParseObject.KEY);
					key = new Key();
					key.keyType = "FULLTEXT";
					keys.add(key);
					parseObjectStack.push(TableParseObject.NAME);
					parsePhase = TableParseObjectPhase.WAIT_START;
				}
				if(c == ')'){
					parseObjectStack.push(TableParseObject.TABLE_ATT);
					parsePhase = TableParseObjectPhase.WAIT_START;
				}
				break;
			case TableParseObject.DATA_TYPE:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == ' '){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
				}else{
					if(c == ' '||c== ','||c=='\n'){
						column.columnType = bufferArea.toString();
						bufferArea.delete(0, bufferArea.length());
						parseObjectStack.pop();
						parseObjectStack.push(TableParseObject.COLUMN_ATT);
						parsePhase = TableParseObjectPhase.WAIT_END;
					}else{
						bufferArea.append(c);
					}
				}
				break;
			case TableParseObject.COLUMN_ATT:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == ' '){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
				}else{
					if(c == ' '||c== ','||c=='\n'){
						String columnAttName = bufferArea.toString();
						if(columnAttName.equals("unsigned")||columnAttName.equals("zerofill")){
							column.columnType+=" "+columnAttName;
							bufferArea.delete(0, bufferArea.length());
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
						if(columnAttName.equals("NOT NULL")){
							column.nullAble = false;
							bufferArea.delete(0, bufferArea.length());
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
						if(columnAttName.equals("AUTO_INCREMENT")){
							column.autoIncrement = true;
							bufferArea.delete(0, bufferArea.length());
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
						if(columnAttName.equals("CHARACTER SET")){
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.push(TableParseObject.CHARSET);
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
						if(columnAttName.equals("COLLATE")){
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.push(TableParseObject.COLLATE);
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
						if(columnAttName.equals("COMMENT")){
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.push(TableParseObject.COMMENT);
							parsePhase = TableParseObjectPhase.WAIT_START;
						}
						if(columnAttName.equals("DEFAULT")){
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.push(TableParseObject.DEFAULT_VALUE);
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
						if(bufferArea.length() != 0){
							bufferArea.append(c);
						}
					}else{
						bufferArea.append(c);
					}
				}
				break;
			case TableParseObject.CHARSET:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == ' '){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
				}else{
					if(c == ' '||c== ',' || c== '\n'||i==sql.length()-1){
						if(i==sql.length()-1){
							bufferArea.append(c);
						}
						if(parseObjectStack.get(parseObjectStack.size()-2) == TableParseObject.COLUMN_ATT){
							column.charset = bufferArea.toString();
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.pop();
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
						if(parseObjectStack.get(parseObjectStack.size()-2) == TableParseObject.TABLE_ATT){
							table.charset = bufferArea.toString();
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.pop();
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
					}else{
						bufferArea.append(c);
					}
				}
				break;

			case TableParseObject.COLLATE:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == ' '){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
				}else{
					if(c == ' '||c== ',' || c== '\n'||i==sql.length()-1){
						if(i==sql.length()-1){
							bufferArea.append(c);
						}
						if(parseObjectStack.get(parseObjectStack.size()-2) == TableParseObject.COLUMN_ATT){
							column.collate = bufferArea.toString();
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.pop();
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
						if(parseObjectStack.get(parseObjectStack.size()-2) == TableParseObject.TABLE_ATT){
							table.collate = bufferArea.toString();
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.pop();
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
					}else{
						bufferArea.append(c);
					}
				}
				break;
			case TableParseObject.DEFAULT_VALUE:{
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == '\''){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
				}else{
					if(c == ' '||c == ','||c=='\n'){
						String defaultValue = bufferArea.toString();
						bufferArea.delete(0, bufferArea.length());
						if(defaultValue.equals("NULL")){
							column.defaultValue = null;
							column.nullAble = true;
							parseObjectStack.pop();
						}else if(defaultValue.charAt(0) == '\''&& defaultValue.charAt(defaultValue.length()-1) == '\''){
							column.defaultValue = defaultValue.substring(1,defaultValue.length()-1);
							parseObjectStack.pop();
						}else{
							throw new TableParseException(TableParseException.UNKOWN);
						}
					}else{
						bufferArea.append(c);
					}
				}
			}
			break;
			case TableParseObject.COMMENT:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == '\''){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
				}else{
					if(c == '\''){
						if(parseObjectStack.get(parseObjectStack.size()-2) == TableParseObject.COLUMN_ATT){
							column.comment = bufferArea.toString();
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.pop();
							parsePhase = TableParseObjectPhase.WAIT_START;
						}
						if(parseObjectStack.get(parseObjectStack.size()-2) == TableParseObject.TABLE_ATT){
							table.comment = bufferArea.toString();
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.pop();
							parsePhase = TableParseObjectPhase.WAIT_START;
						}
					}else{
						bufferArea.append(c);
					}
				}
				break;
				
			case TableParseObject.PRIMARY_KEY:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == '`'){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
					if(c == ')'){
						table.primaryKeys = primaryKeys.toArray(new String[0]);
						parseObjectStack.pop();
					}
				}else{
					if(c == '`'){
						String primaryKey = bufferArea.toString();
						bufferArea.delete(0, bufferArea.length());
						primaryKeys.add(primaryKey);
						parsePhase = TableParseObjectPhase.WAIT_START;
					}else{
						bufferArea.append(c);
					}
				}
				break;
			case TableParseObject.FIELD:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == '`'){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
					
					if(c == ')'){
						key.fields = fieldInKey.toArray(new String[0]);
						parseObjectStack.pop();
						parseObjectStack.push(TableParseObject.KEY_INDEX_METHOD);
						parsePhase = TableParseObjectPhase.WAIT_START;
					}
				
				}else{
					if(c == '`'){
						String field = bufferArea.toString();
						bufferArea.delete(0, bufferArea.length());
						fieldInKey.add(field);
						parsePhase = TableParseObjectPhase.WAIT_START;
					}else{
						bufferArea.append(c);
					}
				}
				break;
			case TableParseObject.KEY_INDEX_METHOD:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == ' '){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
					if(c == '\n'){
						parseObjectStack.pop();
						parseObjectStack.pop();
					}
				
				}else{
					if(c == ','||c=='\n'){
						String indexMethod = bufferArea.toString();
						bufferArea.delete(0, bufferArea.length());
						key.indexMethod = indexMethod.split(" ")[1];
						parseObjectStack.pop();
						parseObjectStack.pop();
					}else{
						bufferArea.append(c);
					}
				}
				break;	
			case TableParseObject.TABLE_ATT:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == ' '){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
				}else{
					if(c == '='){
						String attName = bufferArea.toString();	
						if(attName.equals("ENGINE")){
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.add(TableParseObject.ENGINE);
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
						if(attName.equals("AUTO_INCREMENT")){
							bufferArea.delete(0, bufferArea.length());
							parsePhase = TableParseObjectPhase.WAIT_START;
						}
						if(attName.equals("DEFAULT CHARSET")){
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.add(TableParseObject.CHARSET);
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
						if(attName.equals("COLLATE")){
							bufferArea.delete(0, bufferArea.length());
							parseObjectStack.add(TableParseObject.COLLATE);
							parsePhase = TableParseObjectPhase.WAIT_END;
						}
					}else{
						bufferArea.append(c);
					}
				}
				break;
			case TableParseObject.ENGINE:
				if(parsePhase == TableParseObjectPhase.WAIT_START){
					if(c == ' '){
						parsePhase = TableParseObjectPhase.WAIT_END;
					}
				}else{
					if(c == ' '){
						table.engine = bufferArea.toString();
						bufferArea.delete(0, bufferArea.length());
						parseObjectStack.pop();
						parsePhase = TableParseObjectPhase.WAIT_END;
					}else{
						bufferArea.append(c);
					}
				}
				break;
			}
			//if next parse COLUMN_ATT, and current char is \n,up 2 level.
			if(parseObjectStack.get(parseObjectStack.size()-1) == TableParseObject.COLUMN_ATT
					&&(c==','||c=='\n')){
				parseObjectStack.pop();
				parseObjectStack.pop();
			}
			
		}
		table.columns = columns.toArray(new Column[0]);
		table.primaryKeys = primaryKeys.toArray(new String[0]);
		table.keys = keys.toArray(new Key[0]);
		return table;
	}
	/**
	 * 
	 * @param table
	 * @return
	 */
	static public String buildSqlByTable(Table table){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("CREATE TABLE `"+table.name+"` (\n  ");
		for (int j = 0; j < table.columns.length; j++) {
			if(j != 0){
				stringBuilder.append(",\n  ");
			}
			Column column = table.columns[j];
			addColumnSql(stringBuilder, column);
		}
		if(table.primaryKeys.length>0){
			stringBuilder.append(",\n  ");
			addPrimaryKeySql(stringBuilder, table.primaryKeys);
		}
		
		if(table.keys.length>0){
			for (int i = 0; i < table.keys.length; i++) {
				stringBuilder.append(",\n  ");
				Key key = table.keys[i];
				addKeySql(stringBuilder, key);
			}
		}
		stringBuilder.append("\n) ENGINE=");
		stringBuilder.append(table.engine);
		stringBuilder.append(" DEFAULT CHARSET=");
		stringBuilder.append(table.charset);
		if(table.collate!=null){
			stringBuilder.append(" COLLATE=");
			stringBuilder.append(table.collate);
		}
		if(table.comment!=null){
			stringBuilder.append(" COMMENT='");
			stringBuilder.append(table.comment);
			stringBuilder.append("'");
		}
		stringBuilder.append(";");
		return stringBuilder.toString();
	}
	/**
	 * 
	 * @param stringBuilder
	 * @param column
	 */
	static public void addColumnSql(StringBuilder stringBuilder,Column column){
		stringBuilder.append("`");
		stringBuilder.append(column.name);
		stringBuilder.append("` ");
		stringBuilder.append(column.columnType);
		if(column.charset != null){
			stringBuilder.append(" CHARACTER SET ");
			stringBuilder.append(column.charset);
		}
		if(column.collate != null){
			stringBuilder.append(" COLLATE ");
			stringBuilder.append(column.collate);
		}
		// if nullable equal true, default value will equal null.
		// if nullable equal false,default equal null,stand for not point value.
		if(column.nullAble == true){
			stringBuilder.append(" DEFAULT NULL");
		}
		if(column.nullAble == false){
			stringBuilder.append(" NOT NULL");
			if(column.defaultValue !=null){
				stringBuilder.append(" DEFAULT '");
				stringBuilder.append(column.defaultValue);
				stringBuilder.append("'");
			}
		}
		if(column.autoIncrement){
			stringBuilder.append(" AUTO_INCREMENT");
		}
		if(column.comment != null){
			stringBuilder.append("COMMENT '"+column.comment+"'");
		}
	}
	/**
	 * 
	 * @param stringBuilder
	 * @param primaryKeys
	 */
	static public void addPrimaryKeySql(StringBuilder stringBuilder,String[] primaryKeys){
		stringBuilder.append("PRIMARY KEY (");
		for (int j = 0; j < primaryKeys.length; j++) {
			if(j!=0){
				stringBuilder.append(",");
			}
			stringBuilder.append("`");
			stringBuilder.append(primaryKeys[j]);
			stringBuilder.append("`");
		}
		stringBuilder.append(")");
	}
	/**
	 * 
	 * @param stringBuilder
	 * @param key
	 */
	static public void addKeySql(StringBuilder stringBuilder,Key key){
		stringBuilder.append(key.keyType);
		stringBuilder.append(" `");
		stringBuilder.append(key.name);
		stringBuilder.append("` (");
		for (int j = 0; j < key.fields.length; j++) {
			if(j!=0){
				stringBuilder.append(",");
			}
			stringBuilder.append("`");
			stringBuilder.append(key.fields[j]);
			stringBuilder.append("`");
		}
		stringBuilder.append(") USING ");
		stringBuilder.append(key.indexMethod);
	}
	
	/**
	 * 
	 * @param table
	 * @param packageName
	 * @return
	 */
	static public String generateRecordCodeByTable(Table table,String packageName){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("package ");
		stringBuilder.append(packageName);
		stringBuilder.append(";\n\n");
		stringBuilder.append("import store.aixi.mysqlorm.Column;\n");
		stringBuilder.append("import store.aixi.mysqlorm.Key;\n");
		stringBuilder.append("import store.aixi.mysqlorm.Record;\n");
		stringBuilder.append("import store.aixi.mysqlorm.Table;\n\n");
		stringBuilder.append("import java.sql.ResultSet;\n\n");
		stringBuilder.append("import java.sql.SQLException;\n\n");stringBuilder.append("/**\n * author: mysqlorm\n * date: "+(new Date())+"\n */\n");
		String className = getClassNameByTableName(table.name);
		stringBuilder.append("public class "+className+" extends Record {\n");
		stringBuilder.append("\t/**\n\t *\n\t */\n");
		stringBuilder.append("\tstatic private Table table;\n");
		stringBuilder.append("\t/**\n\t *\n\t */\n");
		stringBuilder.append("\tpublic Table getTable(){\n");
		stringBuilder.append("\t\tif(table == null){\n");
		stringBuilder.append("\t\t\ttable = new Table();\n");
		stringBuilder.append("\t\t\ttable.name = \""+table.name+"\";\n");
		stringBuilder.append("\t\t\ttable.columns = new Column["+table.columns.length+"];\n");
		for(int i=0;i<table.columns.length;i++){
			Column column = table.columns[i];
			
			stringBuilder.append("\t\t\t");
			if(i==0){
				stringBuilder.append("Column ");
			}
			stringBuilder.append("column = new Column();\n");
			stringBuilder.append("\t\t\tcolumn.name = \""+column.name+"\";\n");
			stringBuilder.append("\t\t\tcolumn.columnType = \""+column.columnType+"\";\n");
			stringBuilder.append("\t\t\tcolumn.charset = "+(column.charset==null?"null":"\""+column.charset+"\"")+";\n");
			stringBuilder.append("\t\t\tcolumn.collate = "+(column.collate==null?"null":"\""+column.collate+"\"")+";\n");
			stringBuilder.append("\t\t\tcolumn.nullAble = "+column.nullAble+";\n");
			stringBuilder.append("\t\t\tcolumn.defaultValue = "+(column.defaultValue==null?"null":"\""+column.defaultValue+"\"")+";\n");
			stringBuilder.append("\t\t\tcolumn.autoIncrement = "+column.autoIncrement+";\n");
			stringBuilder.append("\t\t\tcolumn.comment = "+(column.comment==null?"null":"\""+column.comment+"\"")+";\n");
			stringBuilder.append("\t\t\ttable.columns["+i+"] = column;\n");
		}
		stringBuilder.append("\t\t\ttable.primaryKeys = new String["+table.primaryKeys.length+"];\n");
		for (int i = 0; i < table.primaryKeys.length; i++) {
			stringBuilder.append("\t\t\ttable.primaryKeys["+i+"] = \""+table.primaryKeys[i]+"\";\n");
		}
		stringBuilder.append("\t\t\ttable.keys = new Key["+table.primaryKeys.length+"];\n");
		for (int i = 0; i < table.keys.length; i++) {
			Key key = table.keys[i];
			stringBuilder.append("\t\t\t");
			if(i==0){
				stringBuilder.append("Key ");
			}
			stringBuilder.append("key = new Key();\n");
			stringBuilder.append("\t\t\tkey.keyType = \""+key.keyType+"\";\n");
			stringBuilder.append("\t\t\tkey.name = \""+key.name+"\";\n");
			stringBuilder.append("\t\t\tkey.fields = new String["+key.fields.length+"];\n");
			for (int j = 0; j < key.fields.length; j++) {
				String field = key.fields[j];
				stringBuilder.append("\t\t\tkey.fields["+j+"] = \""+field+"\";\n");
			}
			stringBuilder.append("\t\t\tkey.indexMethod = \""+key.indexMethod+"\";\n");
			stringBuilder.append("\t\t\ttable.keys["+i+"] = key;\n");
		}
		stringBuilder.append("\t\t\ttable.engine = \""+table.engine+"\";\n");
		stringBuilder.append("\t\t\ttable.charset = \""+table.charset+"\";\n");
		stringBuilder.append("\t\t\ttable.collate = "+(table.collate==null?"null":"\""+table.collate+"\"")+";\n");
		stringBuilder.append("\t\t\ttable.comment = "+(table.comment==null?"null":"\""+table.comment+"\"")+";\n");
		
		stringBuilder.append("\t\t}\n");
		stringBuilder.append("\t\treturn table;\n");
		stringBuilder.append("\t}\n");
		//process column
		for (int i = 0; i < table.columns.length; i++) {
			Column column = table.columns[i];
			stringBuilder.append("\t/**\n\t *\n\t */\n");
			stringBuilder.append("\tpublic ");
			//tinyint,smallint,mediumint,int,integer match int;
			String dataTypeInJava = "UnknowType";
			if((column.columnType.indexOf("int")!=-1&&column.columnType.indexOf("bigint")==-1)||column.columnType.indexOf("integer")!=-1){
				dataTypeInJava = "int";
			}
			//bigint match long；
			if(column.columnType.indexOf("bigint")!=-1){
				dataTypeInJava = "long";
			}
			//bit match boolean
			if(column.columnType.equals("bit")){
				dataTypeInJava = "boolean";
			}
			//double is double,float is float
			if(column.columnType.equals("double")){
				dataTypeInJava = "double";
			}
			if(column.columnType.equals("float")){
				dataTypeInJava = "float";
			}
			//char,varchar,tinytext,text,longtext,mediumtext,match string;
			if(column.columnType.indexOf("char")!= -1||column.columnType.indexOf("text")!=-1){
				dataTypeInJava = "String";
			}
			//binary,varbinary,tinyblob,mediumblob,blob,longblob;
			if(column.columnType.indexOf("binary")!= -1||column.columnType.indexOf("blob")!=-1){
				dataTypeInJava = "byte[]";
			}
			stringBuilder.append(dataTypeInJava);
			stringBuilder.append(" ");
			stringBuilder.append(getFieldNameByColumnName(column.name));

			if(column.defaultValue != null){
				stringBuilder.append(" = ");
				if(dataTypeInJava.equals("String")){
					stringBuilder.append('"');
				}
				stringBuilder.append(column.defaultValue);
				if(dataTypeInJava.equals("String")){
					stringBuilder.append('"');
				}
			}
			stringBuilder.append(";\n");
		}
		//general getInsertSql()；
		stringBuilder.append("\t/**\n\t *\n\t */\n");
		stringBuilder.append("\tpublic String getInsertSql(){\n");
		stringBuilder.append("\t\tString sql=\"INSERT INTO `");
		stringBuilder.append(table.name);
		stringBuilder.append("` (");
		for (int i = 0; i < table.columns.length; i++) {
			Column column = table.columns[i];
			stringBuilder.append("`");
			stringBuilder.append(column.name);
			stringBuilder.append("`");
			if(i<table.columns.length-1){
				stringBuilder.append(",");
			}
		}
		stringBuilder.append(") VALUES(");
		for (int i = 0; i < table.columns.length; i++) {
			Column column = table.columns[i];
			stringBuilder.append("'\"+");
			stringBuilder.append(getFieldNameByColumnName(column.name));
			stringBuilder.append("+\"'");
			if(i<table.columns.length-1){
				stringBuilder.append(",");
			}
		}
		stringBuilder.append(");\";\n\t\treturn sql;\n");
		stringBuilder.append("\t}\n");
		//getUpdateSql();
		stringBuilder.append("\t/**\n\t *\n\t */\n");
		stringBuilder.append("\tpublic String getUpdateSql(){\n");
		stringBuilder.append("\t\tString sql=\"UPDATE `");
		stringBuilder.append(table.name);
		stringBuilder.append("` SET ");
		for (int i = 0; i < table.columns.length; i++) {
			Column column = table.columns[i];
			stringBuilder.append("`");
			stringBuilder.append(column.name);
			stringBuilder.append("`=");
			stringBuilder.append("'\"+");
			stringBuilder.append(getFieldNameByColumnName(column.name));
			stringBuilder.append("+\"'");
			if(i<table.columns.length-1){
				stringBuilder.append(",");
			}
		}
		stringBuilder.append(" WHERE ");
		for (int i = 0; i < table.primaryKeys.length; i++) {
			String primaryKey = table.primaryKeys[i];
			stringBuilder.append("`");
			stringBuilder.append(primaryKey);
			stringBuilder.append("`=");
			stringBuilder.append("'\"+");
			stringBuilder.append(getFieldNameByColumnName(primaryKey));
			stringBuilder.append("+\"'");
			if(i<table.primaryKeys.length-1){
				stringBuilder.append(" and ");
			}
		}
		stringBuilder.append(";\";\n\t\treturn sql;\n");
		stringBuilder.append("\t}\n");
		
		//getDeleteSql();
		stringBuilder.append("\t/**\n\t *\n\t */\n");
		stringBuilder.append("\tpublic String getDeleteSql(){\n");
		stringBuilder.append("\t\tString sql=\"DELETE FROM `");
		stringBuilder.append(table.name);
		stringBuilder.append("` WHERE ");
		for (int i = 0; i < table.primaryKeys.length; i++) {
			String primaryKey = table.primaryKeys[i];
			stringBuilder.append("`");
			stringBuilder.append(primaryKey);
			stringBuilder.append("`=");
			stringBuilder.append("'\"+");
			stringBuilder.append(getFieldNameByColumnName(primaryKey));
			stringBuilder.append("+\"'");
			if(i<table.primaryKeys.length-1){
				stringBuilder.append(" and ");
			}
		}
		stringBuilder.append(";\";\n\t\treturn sql;\n");
		stringBuilder.append("\t}\n");
		
		//initValue()
		stringBuilder.append("\t/**\n\t *\n\t */\n");
		stringBuilder.append("\tpublic void initValue(ResultSet resultSet) throws SQLException{");
		for (int i = 0; i < table.columns.length; i++) {
			Column column = table.columns[i];
			stringBuilder.append("\n\t\t");
			stringBuilder.append(getFieldNameByColumnName(column.name));
			stringBuilder.append("=");
			stringBuilder.append("resultSet.");
			String getDataMethod = "UnknowMthod";
			if((column.columnType.indexOf("int")!=-1&&column.columnType.indexOf("bigint")==-1)||column.columnType.indexOf("integer")!=-1){
				getDataMethod = "getInt";
			}
			//bigint为long；
			if(column.columnType.indexOf("bigint")!=-1){
				getDataMethod = "getLong";
			}
			//bit为boolean
			if(column.columnType.equals("bit")){
				getDataMethod = "getBoolean";
			}
			//double is double,float is float
			if(column.columnType.equals("double")){
				getDataMethod = "getDouble";
			}
			if(column.columnType.equals("float")){
				getDataMethod = "getFloat";
			}
			//char,varchar,tinytext,text,longtext,mediumtext,为string;
			if(column.columnType.indexOf("char")!= -1||column.columnType.indexOf("text")!=-1){
				getDataMethod = "getString";
			}
			//binary,varbinary,tinyblob,mediumblob,blob,longblob;
			if(column.columnType.indexOf("binary")!= -1||column.columnType.indexOf("blob")!=-1){
				getDataMethod = "getBytes";
			}
			stringBuilder.append(getDataMethod);
			stringBuilder.append("(\"");
			stringBuilder.append(column.name);
			stringBuilder.append("\");");
		}
		stringBuilder.append("\n\t}\n");
		//toString

		stringBuilder.append("\t/**\n\t *\n\t */\n");
		stringBuilder.append("\tpublic String toString(){");
		stringBuilder.append("\n\t\tString str=\"");
		stringBuilder.append(className);
		stringBuilder.append("@\"+Integer.toHexString(hashCode())+\"[");
		for (int i = 0; i < table.columns.length; i++) {
			Column column = table.columns[i];
			stringBuilder.append(getFieldNameByColumnName(column.name));
			stringBuilder.append("=");
			stringBuilder.append("\"+");
			stringBuilder.append(getFieldNameByColumnName(column.name));
			stringBuilder.append("+\"");
			if(i<table.columns.length-1){
				stringBuilder.append(",");
			}
		}
		stringBuilder.append("]\";\n\t\treturn str;");
		stringBuilder.append("\n\t}\n");
		
		
		stringBuilder.append("}\n");
		return stringBuilder.toString();
	}
	/**
	 * 
	 * @param tableName
	 * @return
	 */
	static public String getClassNameByTableName(String tableName){
		String className = "";
		String[] classSubNames =tableName.split("_");
		for (int i = 1; i < classSubNames.length; i++) {
			className +=classSubNames[i].substring(0, 1).toUpperCase();
			className += classSubNames[i].substring(1);
		}
		className +="Record";
		return className;
	}
	/**
	 * 
	 * @param columnName
	 * @return
	 */
	static public String getFieldNameByColumnName(String columnName){
		String fieldName = "";
		String[] classSubNames =columnName.split("_");
		fieldName = classSubNames[0];
		for (int i = 1; i < classSubNames.length; i++) {
			fieldName +=classSubNames[i].substring(0, 1).toUpperCase();
			fieldName += classSubNames[i].substring(1);
		}
		return fieldName;
	}
}