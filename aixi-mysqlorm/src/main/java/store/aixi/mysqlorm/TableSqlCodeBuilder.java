package store.aixi.mysqlorm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import javax.print.attribute.standard.PrinterMakeAndModel;

/**
 * author：zhaochengbei
 * date：2017/8/17
*/
public class TableSqlCodeBuilder {

	/**
	 * 根据sql语句构造Table;
	 * @throws TableParseException 
	 */
	static public Table buildTableBySql(String sql) throws TableParseException{
		Table table = new Table();
		//承装中间结果；
		List<Column> columns = new ArrayList<Column>();
		List<String> primaryKeys = new ArrayList<String>();
		List<Key> keys = new ArrayList<Key>();
		List<String> fieldInKey = new ArrayList<String>();
		Column column = null;
		Key key = null;
		//解析对象栈；
		Stack<Integer> parseObjectStack = new Stack<Integer>();
		//缓冲区；
		StringBuilder bufferArea = new StringBuilder();
		//解析阶段；
		int parsePhase = TableParseObjectPhase.WAIT_START;
		//从Create Table关键字开始；
		parseObjectStack.push(TableParseObject.CRATE_TABLE);
		for (int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			//根据解析对象与阶段做出处理；
			switch(parseObjectStack.get(parseObjectStack.size()-1)){
			case TableParseObject.CRATE_TABLE:
				if(c == '`'){
					if(bufferArea.toString().indexOf("CREATE TABLE") == -1){
						throw new TableParseException(TableParseException.UNKOWN);
					}
					//清空缓冲区
					bufferArea.delete(0, bufferArea.length());
					//进入下一个阶段
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
						//清空缓冲区
						bufferArea.delete(0, bufferArea.length());
						if(name.isEmpty()){
							throw new TableParseException(TableParseException.UNKOWN);
						}
						if(parseObjectStack.size() == 1){
							table.name = name; 
							//进入下一个阶段
							parseObjectStack.pop();
							parseObjectStack.push(TableParseObject.ELEMENT);
						}else if (parseObjectStack.get(parseObjectStack.size()-2) ==TableParseObject.COLUMN){
							column.name = name;
							//解析完名字，开始解析数据类型；
							parseObjectStack.pop();
							parseObjectStack.push(TableParseObject.DATA_TYPE);
							parsePhase = TableParseObjectPhase.WAIT_START;
						}else if(parseObjectStack.get(parseObjectStack.size()-2) == TableParseObject.KEY){
							key.name = name;
							parseObjectStack.pop();
							//解析key中的字段；
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
				//如果出现'，代表当前正在解析column的名字；
				if(c == '`'){
					parseObjectStack.push(TableParseObject.COLUMN);
					column = new Column();
					columns.add(column);
					parseObjectStack.push(TableParseObject.NAME);
					parsePhase = TableParseObjectPhase.WAIT_END;
				}
				if(c == 'P'){
					parseObjectStack.push(TableParseObject.PRIMARY_KEY);
					parsePhase = TableParseObjectPhase.WAIT_START;
				}
				if(c == 'K'){
					parseObjectStack.push(TableParseObject.KEY);
					key = new Key();
					keys.add(key);
					parseObjectStack.push(TableParseObject.NAME);
					parsePhase = TableParseObjectPhase.WAIT_START;
				}
				if(c == ')'){
					//开始解析表属性；
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
						//解析其他属性;
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
						//检查是哪个属性
						String columnAttName = bufferArea.toString();
						if(columnAttName.equals("unsigned")||columnAttName.equals("zerofill")){
							column.columnType+=" "+columnAttName;
							bufferArea.delete(0, bufferArea.length());
							//准备读取下一格属性
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
							//解析字符集；
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
						//没有匹配上,说明空格是关键字中的一部分；
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
							column.defaultValue = defaultValue.substring(1,defaultValue.length()-2);
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
						//字段列表解析结束；
						table.primaryKeys = primaryKeys.toArray(new String[0]);
						//返回上一级；
						parseObjectStack.pop();
					}
				}else{
					if(c == '`'){
						String primaryKey = bufferArea.toString();
						bufferArea.delete(0, bufferArea.length());
						primaryKeys.add(primaryKey);
						//继续解析下一个；
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
						//字段列表解析结束；
						key.fields = fieldInKey.toArray(new String[0]);
						//返回上上级；
						parseObjectStack.pop();
						parseObjectStack.pop();
					}
				
				}else{
					if(c == '`'){
						String field = bufferArea.toString();
						bufferArea.delete(0, bufferArea.length());
						fieldInKey.add(field);
						//继续解析下一个；
						parsePhase = TableParseObjectPhase.WAIT_START;
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
							//略过这个属性
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
			//如果接下来要解析COLUMN_ATT,而当前最后字符是，\n,则向上两级
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
	 * 根据Table构造sql;
	 */
	static public String buildSqlByTable(Table table){
		//生成sql语句
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("CREATE TABLE `"+table.name+"` (\n  ");
		//遍历字段生成创建字段的 语句；
		for (int j = 0; j < table.columns.length; j++) {
			if(j != 0){
				stringBuilder.append(",\n  ");
			}
			Column column = table.columns[j];
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
			//如果nulable == true,default一定等于null
			//如果nullable==false,default=null代表没有指定值；
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
		}
		if(table.primaryKeys.length>0){
			//添加主键
			stringBuilder.append(",\n  ");
			addPrimaryKeySql(stringBuilder, table.primaryKeys);
		}
		
		//添加key;
		if(table.keys.length>0){
			for (int i = 0; i < table.keys.length; i++) {
				stringBuilder.append(",\n  ");
				Key key = table.keys[i];
				stringBuilder.append("KEY ");
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
		//返回sql
		return stringBuilder.toString();
	}
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
		//如果nulable == true,default一定等于null
		//如果nullable==false,default=null代表没有指定值；
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
	static public void addKeySql(StringBuilder stringBuilder,Key key){
		stringBuilder.append("`");
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
		stringBuilder.append(")");
	}
	
	/**
	 * 根据table生成Record代码；
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
		stringBuilder.append("/**\n * author: mysqlorm\n * date: "+(new Date())+"\n */\n");
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
			stringBuilder.append("\t\t\tkey.name = \""+key.name+"\";\n");
			stringBuilder.append("\t\t\tkey.fields = new String["+key.fields.length+"];\n");
			for (int j = 0; j < key.fields.length; j++) {
				String field = key.fields[j];
				stringBuilder.append("\t\t\tkey.fields["+j+"] = \""+field+"\";\n");
			}
			stringBuilder.append("\t\t\ttable.keys["+i+"] = key;\n");
		}
		stringBuilder.append("\t\t\ttable.engine = \""+table.engine+"\";\n");
		stringBuilder.append("\t\t\ttable.charset = \""+table.charset+"\";\n");
		stringBuilder.append("\t\t\ttable.collate = "+(table.collate==null?"null":"\""+table.collate+"\"")+";\n");
		stringBuilder.append("\t\t\ttable.comment = "+(table.comment==null?"null":"\""+table.comment+"\"")+";\n");
		
		stringBuilder.append("\t\t}\n");
		stringBuilder.append("\t\treturn table;\n");
		stringBuilder.append("\t}\n");
		stringBuilder.append("}\n");
		return stringBuilder.toString();
	}
	/**
	 * 
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
}