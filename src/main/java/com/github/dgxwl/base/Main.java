package com.github.dgxwl.base;

import com.github.dgxwl.base.entity.Column;
import com.github.dgxwl.base.entity.PrimaryKey;
import com.github.dgxwl.base.entity.Table;
import com.github.dgxwl.base.handler.PaginatorHandler;
import com.github.dgxwl.base.handler.TableHandler;
import com.github.dgxwl.util.DBUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * 生成代码文件
 * @author Administrator
 *
 */
public class Main {
	
	private List<Table> tables;
	private List<String> entityNames;
	private String tablesStr;
	private String oneToMany;

	private static String path;
	private static String packageName;
	private static String defaultTablesStr;
	private static String defaultOneToMany;
	static {
		Properties config = DBUtils.getConfigs();
		path = config.getProperty("path");
		packageName = config.getProperty("package");

		defaultTablesStr = config.getProperty("tables");
		defaultOneToMany = config.getProperty("one_to_many");
	}
	//映射SQL数据类型和Java数据类型
	private static Map<String, String> typeMap = new HashMap<>();
	//映射SQL数据类型和Java数据类型(resultMap用到)
	private static Map<String, String> fullNameTypeMap = new HashMap<>();
	//映射Java数据类型和import声明
	private static Map<String, String> typeImportMap = new HashMap<>();
	static {
		typeMap.put("CHAR", "String");
		typeMap.put("VARCHAR", "String");
		typeMap.put("VARCHAR2", "String");
		typeMap.put("NVARCHAR", "String");
		typeMap.put("BLOB", "Byte[]");
		typeMap.put("CLOB", "String");
		typeMap.put("TEXT", "String");
		typeMap.put("BIT", "Boolean");
		typeMap.put("BOOL", "Boolean");
		typeMap.put("TINYINT", "Integer");
		typeMap.put("SMALLINT", "Integer");
		typeMap.put("MEDIUMINT", "Integer");
		typeMap.put("INTEGER", "Integer");
		typeMap.put("INT", "Integer");
		typeMap.put("BIGINT", "Long");
		typeMap.put("REAL", "BigDecimal");
		typeMap.put("DOUBLE", "Double");
		typeMap.put("FLOAT", "Float");
		typeMap.put("DECIMAL", "BigDecimal");
		typeMap.put("NUMERIC", "BigDecimal");
		typeMap.put("DATE", "Date");
		typeMap.put("TIME", "Date");
		typeMap.put("YEAR", "Date");
		typeMap.put("DATETIME", "Date");
		typeMap.put("TIMESTAMP", "Date");
		typeMap.put("INET", "String");
		
		fullNameTypeMap.put("CHAR", "java.lang.String");
		fullNameTypeMap.put("VARCHAR", "java.lang.String");
		fullNameTypeMap.put("VARCHAR2", "java.lang.String");
		fullNameTypeMap.put("NVARCHAR", "java.lang.String");
		fullNameTypeMap.put("BLOB", "java.lang.Byte");
		fullNameTypeMap.put("TEXT", "java.lang.String");
		fullNameTypeMap.put("BIT", "java.lang.Boolean");
		fullNameTypeMap.put("BOOL", "java.lang.Boolean");
		fullNameTypeMap.put("TINYINT", "java.lang.Integer");
		fullNameTypeMap.put("SMALLINT", "java.lang.Integer");
		fullNameTypeMap.put("MEDIUMINT", "java.lang.Integer");
		fullNameTypeMap.put("INTEGER", "java.lang.Integer");
		fullNameTypeMap.put("INT", "java.lang.Integer");
		fullNameTypeMap.put("BIGINT", "java.lang.Long");
		fullNameTypeMap.put("REAL", "java.math.BigDecimal");
		fullNameTypeMap.put("DOUBLE", "java.lang.Double");
		fullNameTypeMap.put("FLOAT", "java.lang.Float");
		fullNameTypeMap.put("DECIMAL", "java.math.BigDecimal");
		fullNameTypeMap.put("NUMERIC", "java.math.BigDecimal");
		fullNameTypeMap.put("DATE", "java.util.Date");
		fullNameTypeMap.put("TIME", "java.util.Date");
		fullNameTypeMap.put("YEAR", "java.util.Date");
		fullNameTypeMap.put("DATETIME", "java.util.Date");
		fullNameTypeMap.put("TIMESTAMP", "java.util.Date");
		fullNameTypeMap.put("INET", "java.lang.String");

		typeImportMap.put("BigDecimal", "import java.math.BigDecimal;");
		typeImportMap.put("Date", "import java.util.Date;");
	}

	private static String removeSuffix;
	private static String queryFullName;
	private static String queryName;
	private static String queryVarName;
	private static String responseResultFullName;
	private static String listResponseResultFullName;
	private static String responseResultName;
	private static String listResponseResultName;
	private static String rrDiamondName;
	private static String lrrDiamondName;
	private static String responseResultVarName;
	private static String rrData;
	private static String listRrData;
	private static String rrGenericStr;
	private static boolean rrGeneric;
	private static String listRrGenericStr;
	private static boolean listRrGeneric;
	private static String successCode;
	private static String errorCode;
	private static String whereStr;
	private static String stringUtilFullName;
	private static String stringUtil;
	private static String stringIsEmpty;
	private static String getIdUtilFullName;
	private static String getIdUtil;
	private static String getIdMethod;
	private static String getIdParams;
	private static String[] getIdParamArr;
	private static String pages;
	private static String total;
	private static String paginator;
	private static String consumes;
	private static String combineAddUpdateStr;
	private static boolean combineAddUpdate;
	private static String orderField;
	private static String orderType;
	private static String orderFieldVal;
	private static String orderTypeVal;
	public static String createdDate;
	public static String createdBy;
	public static String updatedDate;
	public static String updatedBy;
	public static String active;
	public static String activeCode;
	private static String need;
	private static List<String> needs;
	private static boolean needSet;
	private static boolean needAdd;
	private static boolean needUpdate;
	private static boolean needList;
	private static boolean needGetById;
	private static boolean needUpdateActive;
	private static boolean needDelete;
	private static boolean needBatchDelete;
	static {
		try (InputStream in = Main.class.getClassLoader().getResourceAsStream("base.properties")) {
			Properties prop = new Properties();
			prop.load(in);

			removeSuffix = prop.getProperty("remove_prefix");
			queryFullName = prop.getProperty("query");
			responseResultFullName = prop.getProperty("response_result");
			listResponseResultFullName = prop.getProperty("list_response_result");
			rrData = prop.getProperty("rr_data");
			listRrData = prop.getProperty("ll_rr_data");
			rrGenericStr = prop.getProperty("rr_generic");
			listRrGenericStr = prop.getProperty("list_rr_generic");
			whereStr = prop.getProperty("where_str");
			stringUtilFullName = prop.getProperty("string_util");
			stringIsEmpty = prop.getProperty("string_is_empty_method");
			getIdUtilFullName = prop.getProperty("get_id_util");
			getIdMethod = prop.getProperty("get_id_method");
			getIdParams = prop.getProperty("get_id_params");
			successCode = prop.getProperty("success_code");
			errorCode = prop.getProperty("error_code");
			pages = prop.getProperty("pages");
			total = prop.getProperty("total");
			paginator = prop.getProperty("paginator");
			consumes = prop.getProperty("consumes");
			combineAddUpdateStr = prop.getProperty("combine_add_update");
			orderField = prop.getProperty("order_field");
			orderType = prop.getProperty("order_type");
			orderFieldVal = prop.getProperty("order_field_val");
			orderTypeVal = prop.getProperty("order_type_val");
			createdDate = prop.getProperty("created_date");
			createdBy = prop.getProperty("created_by");
			updatedDate = prop.getProperty("updated_date");
			updatedBy = prop.getProperty("updated_by");
			active = prop.getProperty("active");
			activeCode = prop.getProperty("active_code");
			need = prop.getProperty("need");
		} catch (Exception e) {
			removeSuffix = "true";
			queryFullName = "com.middle.last.domain.MyQuery";
			responseResultFullName = "com.middle.last.domain.ResponseResult";
			listResponseResultFullName = "com.middle.last.domain.ResponseResult";
			rrData = "data";
			listRrData = "data";
			rrGenericStr = "false";
			listRrGenericStr = "true";
			stringUtilFullName = "com.middle.last.util.StringUtil";
			stringIsEmpty = "isNullOrEmpty";
			getIdUtilFullName = "com.middle.last.util.GetIdUtil";
			getIdMethod = "getId";
			getIdParams = "";
			successCode = "1";
			errorCode = "-100";
			pages = "pages";
			total = "total";
			paginator = "pageHelper";
			consumes = "json";
			combineAddUpdateStr = "false";
			orderField = "orderField";
			orderType = "orderType";
			orderFieldVal = "created_date";
			orderTypeVal = "DESC";
			createdDate = "createdDate";
			createdBy = "createdBy";
			updatedDate = "updatedDate";
			updatedBy = "updatedBy";
			active = "active";
			activeCode = "1";
			need = "set,add,update,list,getById,updateActive,delete,batchDelete";
		}
		queryName = queryFullName.substring(queryFullName.lastIndexOf('.') + 1);
		queryVarName = toContentCase(queryName);
		responseResultName = responseResultFullName.substring(responseResultFullName.lastIndexOf('.') + 1);
		listResponseResultName = listResponseResultFullName.substring(listResponseResultFullName.lastIndexOf('.') + 1);
		rrDiamondName = responseResultName;
		lrrDiamondName = listResponseResultName;
		responseResultVarName = getVarNameByCamelCase(responseResultName);
		rrGeneric = "true".equalsIgnoreCase(rrGenericStr);
		if (rrGeneric) {
			rrDiamondName += "<>";
		}
		listRrGeneric = "true".equalsIgnoreCase(listRrGenericStr);
		if (listRrGeneric) {
			lrrDiamondName += "<>";
		}
		stringUtil = stringUtilFullName.substring(stringUtilFullName.lastIndexOf('.') + 1);
		getIdUtil = getIdUtilFullName.substring(getIdUtilFullName.lastIndexOf('.') + 1);
		getIdParamArr = getIdParams.split(",");
		combineAddUpdate = "true".equalsIgnoreCase(combineAddUpdateStr);
		active = Optional.ofNullable(active).orElse("");
		activeCode = Optional.ofNullable(activeCode).orElse("");
		String[] needArr = need.split(",");
		needs = Arrays.asList(needArr);
		needSet = needs.contains("set");
		needAdd = needs.contains("add");
		needUpdate = needs.contains("update");
		needList = needs.contains("list");
		needGetById = needs.contains("getById");
		needUpdateActive = needs.contains("updateActive");
		needDelete = needs.contains("delete");
		needBatchDelete = needs.contains("batchDelete");
	}

	public Main(String tablesStr, String oneToMany) {
		this.tablesStr = tablesStr;
		this.oneToMany = oneToMany;
	}

	public static void main(String[] args) {
		try {
			new Main(defaultTablesStr, defaultOneToMany).generate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generate() throws IOException {
		TableHandler tableHandler = new TableHandler();
		tableHandler.readTables(tablesStr, oneToMany);
		tables = tableHandler.getTables();
		entityNames = tableHandler.getEntityNames();

		File pathFile = new File(path.replaceAll("/{2,}", "/"));
		
		StringBuilder builder = new StringBuilder();
		builder.append(path);
		if (pathFile.exists()) {
			int i = 1;
			do {
				i++;
				if (builder.lastIndexOf("_") != -1 && builder.substring(builder.lastIndexOf("_") + 1).matches("[\\d]+")) {
					builder.delete(builder.lastIndexOf("_"), builder.length());
				}
				builder.append('_').append(i);
				pathFile = new File(builder.toString());
			} while (pathFile.exists());
		}
		
		String xmlPath = builder.toString();
		
		//拼接包名路径
		builder.append('/').append(packageName.replace('.', '/'));
		String finalParentPath = builder.toString();
		
		generateEntity(finalParentPath, packageName, tables);
		generateXmlMappers(xmlPath, packageName);
		generateMapper(finalParentPath, packageName);
		generateIService(finalParentPath, packageName);
		generateService(finalParentPath, packageName);
		generateController(finalParentPath, packageName);
	}
	
	/**
	 * 生成entity实体类文件
	 */
	private void generateEntity(String parentPath, String packageName, List<Table> tables) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			List<Table> slaveTables = table.getAllSlaves();

			String entityName;
			entityName = getEntityName(tableName);
			
			File f = new File(parentPath + "/entity");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/entity/" + entityName + ".java";
			File entity = new File(fileName);
			entity.createNewFile();
			try (
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fileName), "utf-8"));
			) {
				StringBuilder sbBeforeClass = new StringBuilder();
				sbBeforeClass.append("package ").append(packageName).append(".entity;\n\n");
				sbBeforeClass.append("import com.fasterxml.jackson.annotation.JsonIgnoreProperties;\n");
				sbBeforeClass.append("import lombok.Data;\n");
				if (!slaveTables.isEmpty()) {
					sbBeforeClass.append("import java.util.List;\n");
				}
				
				StringBuilder sbAfterClass = new StringBuilder();
				sbAfterClass.append("@Data\n");
				sbAfterClass.append("@JsonIgnoreProperties(ignoreUnknown = true)\n");
				sbAfterClass.append("public class ").append(entityName).append(" {\n");
				List<Column> fields = table.getAllColumns();
				for (Column column : fields) {
					String fieldName = underscoreCaseToCamelCase(column.getColumnName());
					String fieldType = typeMap.get(column.getType());

					switch (fieldType) {
						case "Date":
						case "BigDecimal":
							if (sbBeforeClass.indexOf(typeImportMap.get(fieldType)) == -1) {
								sbBeforeClass.append(typeImportMap.get(fieldType)).append("\n");
							}
							break;
					}
					boolean hasRemark = column.getRemarks() != null && !column.getRemarks().equals("");
					
					sbAfterClass.append("\tprivate ").append(fieldType).append(" ").append(fieldName).append(";")
							.append((hasRemark ? "  //" + column.getRemarks() : "")).append("\n");
				}
				for (Table slaveTable : slaveTables) {
					String slaveTableName = slaveTable.getTableName();
					String slaveEntityName = getEntityName(slaveTableName);
					String slaveEntityVarName = toContentCase(slaveEntityName);
					sbAfterClass.append("\tprivate List<").append(slaveEntityName).append("> ").append(slaveEntityVarName).append("s;\n");
				}
				if (!slaveTables.isEmpty()) {
					generateEntity(parentPath, packageName, slaveTables);
				}

				sbAfterClass.append("}\n");
				
				sbBeforeClass.append("\n");
				sbBeforeClass.append(sbAfterClass);
				bw.write(sbBeforeClass.toString());
				bw.flush();
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	private String getEntityName(String tableName) {
		String finalName;
		if (!tableName.contains("_")) {
			return toTitleCase(underscoreCaseToCamelCase(tableName));
		}
		finalName = tableName.substring(tableName.indexOf('_') + 1);
		if (Collections.frequency(entityNames, finalName) > 1) {
			finalName = tableName;
		}
		return toTitleCase(underscoreCaseToCamelCase(finalName));
	}

	/**
	 * 生成mapper接口文件
	 */
	private void generateMapper(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String mapperName = entityName + "Mapper";

			List<Table> slaves = table.getAllSlaves();
			boolean hasSlave = slaves != null && !slaves.isEmpty();

			StringBuilder builder = new StringBuilder();
			builder.append("package ").append(packageName).append(".mapper;\n\n");

			builder.append("import org.apache.ibatis.annotations.Param;\n");
			builder.append("import ").append(packageName).append(".entity.").append(entityName).append(";\n");
			if (hasSlave) {
				for (Table slave : slaves) {
					String slaveTableName = slave.getTableName();
					String slaveEntityName = getEntityName(slaveTableName);
					builder.append("import ").append(packageName).append(".entity.").append(slaveEntityName).append(";\n");
				}
			}
			builder.append("import ").append(queryFullName).append(";\n\n");
			builder.append("import java.util.List;\n");
			if ("mybatis-paginator".equals(paginator)) {
				builder.append(PaginatorHandler.getImports(paginator)).append("\n");
			}

			builder.append("public interface ").append(mapperName).append(" {\n\n");

			if (needAdd) {
				builder.append("\tInteger add(").append(entityName).append(" entity);\n\n");
			}

			String keyName = null;
			String idName = null;
			String camelKeyName = null;
			String byWhat = null;
			String keyType = null;
			List<PrimaryKey> keys = table.getAllPrimaryKeys();

			if (keys != null && !keys.isEmpty()) {
				PrimaryKey key = keys.get(0);
				keyName = key.getPkName();
				camelKeyName = underscoreCaseToCamelCase(keyName);
				if (keyName.endsWith("id")) {
					idName = "id";
				} else {
					idName = camelKeyName;
				}
				byWhat = toTitleCase(idName);
				keyType = typeMap.get(key.getPkType());
			}

			if (keyName != null && needUpdate) {
				builder.append("\tInteger " + "update(").append(entityName).append(" entity);\n\n");
			}

			if (needList) {
				switch (paginator) {
					case "pageHelper":
						builder.append("\tList<").append(entityName).append("> list(@Param(\"").append(queryVarName)
								.append("\") ").append(queryName).append(" ").append(queryVarName).append(");\n\n");
					case "mybatis-paginator":
						builder.append("\tPageList<").append(entityName).append("> list(@Param(\"").append(queryVarName)
								.append("\") ").append(queryName).append(" ").append(queryVarName)
								.append(", @Param(\"pageBounds\") PageBounds pageBounds);\n\n");
				}
			}

			if (keyName != null) {
				if (needGetById) {
					builder.append("\t").append(entityName).append(" getBy").append(byWhat)
							.append("(").append(keyType).append(" ").append(camelKeyName).append(");\n\n");
				}
				if (active != null && needUpdateActive) {
					String camelActive = underscoreCaseToCamelCase(active);
					String titleActive = toTitleCase(camelActive);
					String methodName = "update" + titleActive;

					builder.append("\tInteger ").append(methodName).append("(@Param(\"").append(camelKeyName).append("\") ")
							.append(keyType).append(' ').append(camelKeyName).append(", @Param(\"").append(camelActive)
							.append("\") Integer ").append(camelActive).append(");\n\n");
				}
				if (needDelete) {
					builder.append("\tInteger delete(").append(keyType).append(" ").append(camelKeyName).append(");\n\n");
				}
				if (needBatchDelete) {
					builder.append("\tInteger batchDelete(@Param(\"list\") List<").append(keyType)
							.append("> ").append(camelKeyName).append("s);\n\n");
				}

				if (hasSlave) {
					for (Table slave : slaves) {
						String slaveTableName = slave.getTableName();
						String slaveEntityName = getEntityName(slaveTableName);

						builder.append("\tInteger addAll").append(slaveEntityName)
								.append("(@Param(\"list\") List<").append(slaveEntityName).append("> list);\n\n");
						builder.append('\t').append("List<").append(slaveEntityName).append('>').append(" getAll").append(slaveEntityName)
								.append('(').append(keyType).append(' ').append(camelKeyName).append(");\n\n");
						builder.append("\tInteger deleteAll").append(slaveEntityName)
								.append('(').append(keyType).append(' ').append(camelKeyName).append(");\n\n");
					}
				}
			}

			builder.append("}\n");
			
			File f = new File(parentPath + "/mapper");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/mapper/" + mapperName + ".java";
			File mapper = new File(fileName);
			mapper.createNewFile();
			try (
					BufferedWriter bw = new BufferedWriter(
											new OutputStreamWriter(
												new FileOutputStream(fileName), StandardCharsets.UTF_8))
			) {
				bw.write(builder.toString());
				bw.flush();
			} catch (IOException e) {
				throw new IOException(e);
			}
		}
	}
	
	/**
	 * 生成mapper映射器文件
	 */
	private void generateXmlMappers(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String mapperName = entityName + "Mapper";
			Set<String> autoIncrementCols = table.getAllAutoIncrementCols();

			File f = new File(parentPath + "/mappers");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/mappers/" + mapperName + ".xml";
			File xmlMapper = new File(fileName);
			xmlMapper.createNewFile();
			try (
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fileName), StandardCharsets.UTF_8))
			) {
				StringBuilder builder = new StringBuilder();
				builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				builder.append("<!DOCTYPE mapper\n");
				builder.append("  PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n");
				builder.append("  \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n");
				
				builder.append("<mapper namespace=\"").append(packageName).append(".mapper.")
						.append(entityName).append("Mapper\">\n\n");

				String keyName = null;
				String idName = null;
				String camelKeyName = null;
				String incrementKeyName = null;
				String byWhat = null;
				String keyType = null;
				String pkIncrementStr = "";
				List<PrimaryKey> keys = table.getAllPrimaryKeys();

				if (keys != null && !keys.isEmpty()) {
					PrimaryKey key = keys.get(0);
					keyName = key.getPkName();
					camelKeyName = underscoreCaseToCamelCase(keyName);
					if (keyName.endsWith("id")) {
						idName = "id";
					} else {
						idName = camelKeyName;
					}
					byWhat = toTitleCase(idName);
					if (autoIncrementCols.contains(keyName)) {
						incrementKeyName = keyName;
						pkIncrementStr = " useGeneratedKeys=\"true\" keyProperty=\"" + camelKeyName + "\"";
					}
					keyType = key.getPkType();
				}

				List<Column> columns = table.getAllColumns();
				Map<String, String> columnMap = new LinkedHashMap<>(columns.size());
				for (Column column : columns) {
					String columnName = column.getColumnName();
					String fieldName = underscoreCaseToCamelCase(columnName);
					columnMap.put(columnName, fieldName);
				}

				List<Table> slaves = table.getAllSlaves();
				boolean hasSlaves = slaves != null && !slaves.isEmpty();

				//resultMap
				StringBuilder resultMapColBuilder = new StringBuilder();
				String listResultMap = hasSlaves ? "list_mapping" : "mapping";
				builder.append("\t<resultMap id=\"").append(listResultMap).append("\" type=\"")
						.append(packageName).append(".entity.").append(entityName).append("\">\n");
				for (Map.Entry<String, String> entry : columnMap.entrySet()) {
					String columnName = entry.getKey();
					String fieldName = entry.getValue();
					if (Objects.equals(columnName, fieldName)) {
						continue;
					}

					if (columnName.equals(incrementKeyName)) {
						resultMapColBuilder.append("\t\t<id column=\"").append(columnName).append("\" property=\"").append(fieldName)
								.append("\" javaType=\"").append(fullNameTypeMap.get(keyType)).append("\" />\n");
					} else {
						resultMapColBuilder.append("\t\t<result column=\"").append(columnName).append("\" property=\"")
								.append(fieldName).append("\" javaType=\"")
								.append(fullNameTypeMap.get(table.getTypeByColumnName(columnName))).append("\" />\n");
					}
				}
				builder.append(resultMapColBuilder);
				builder.append("\t</resultMap>\n\n");

				if (hasSlaves) {
					builder.append("\t<resultMap id=\"mapping\" type=\"")
							.append(packageName).append(".entity.").append(entityName).append("\">\n");
					builder.append(resultMapColBuilder);

					for (Table slave : slaves) {
						String slaveTableName = slave.getTableName();
						String slaveEntityName = getEntityName(slaveTableName);
						String slaveEntityVarName = toContentCase(slaveEntityName);
						builder.append("\t\t<collection property=\"").append(slaveEntityVarName).append("s\" column=\"").append(keyName)
								.append("\"\n\t\t\t\t\tofType=\"").append(packageName).append(".entity.").append(slaveEntityName)
								.append("\"\n\t\t\t\t\tselect=\"").append(packageName).append(".mapper.").append(entityName)
								.append("Mapper.getAll").append(slaveEntityName).append("\" />\n");
					}
					builder.append("\t</resultMap>\n\n");

					for (Table slave : slaves) {
						String slaveTableName = slave.getTableName();
						String slaveEntityName = getEntityName(slaveTableName);
						String mappingName = (slaveTableName.contains("_") ? slaveTableName.substring(slaveTableName.indexOf('_') + 1)
								: slaveTableName) + "_mapping";
						builder.append("\t<resultMap id=\"").append(mappingName).append("\" type=\"")
								.append(packageName).append(".entity.").append(slaveEntityName).append("\">\n");
						List<Column> slaveColumns = slave.getAllColumns();
						for (Column column : slaveColumns) {
							String columnName = column.getColumnName();
							String fieldName = underscoreCaseToCamelCase(columnName);
							if (Objects.equals(columnName, fieldName)) {
								continue;
							}
							builder.append("\t\t<result column=\"").append(columnName).append("\" property=\"")
									.append(fieldName).append("\" javaType=\"")
									.append(fullNameTypeMap.get(slave.getTypeByColumnName(columnName))).append("\" />\n");
						}
						builder.append("\t</resultMap>\n\n");
					}
				}

				//add
				if (needAdd) {
					builder.append("\t<insert id=\"add\"").append(pkIncrementStr)
							.append(" parameterType=\"").append(packageName).append(".entity.").append(entityName).append("\">\n");
					builder.append("\t\tINSERT INTO ").append(tableName).append(" (\n");
					for (String columnName : columnMap.keySet()) {
						if (autoIncrementCols.contains(columnName)) {
							continue;
						}
						builder.append("\t\t\t").append(columnName).append(",\n");
					}
					builder.deleteCharAt(builder.length() - 2);
					builder.append("\t\t) VALUES (\n");
					for (String fieldName : columnMap.values()) {
						if (autoIncrementCols.contains(camelCaseToUnderscoreCase(fieldName))) {
							continue;
						}
						builder.append("\t\t\t#{").append(fieldName).append("},\n");
					}
					builder.deleteCharAt(builder.length() - 2);
					builder.append("\t\t)\n");
					builder.append("\t</insert>\n\n");
				}

				if (keyName != null && needUpdate) {
					//update
					builder.append("\t<update id=\"update\" parameterType=\"")
							.append(packageName).append(".entity.").append(entityName).append("\">\n");
					builder.append("\t\tUPDATE ").append(tableName).append("\n");
					builder.append("\t\t<set>\n");
					for (Map.Entry<String, String> entry : columnMap.entrySet()) {
						String columnName = entry.getKey();
						if (columnName.equals(keyName)) {
							continue;
						}
						if (autoIncrementCols.contains(columnName)) {
							continue;
						}
						String fieldName = entry.getValue();

						builder.append("\t\t\t<if test=\"").append(fieldName).append(" != null\">\n");
						builder.append("\t\t\t\t").append(columnName).append(" = #{").append(fieldName).append("},\n");
						builder.append("\t\t\t</if>\n");
					}
					builder.deleteCharAt(builder.lastIndexOf(","));
					builder.append("\t\t</set>\n");
					builder.append("\t\tWHERE ").append(keyName).append(" = #{").append(camelKeyName).append("}\n");
					builder.append("\t</update>\n\n");
				}

				//list
				if (needList) {
					builder.append("\t<select id=\"list\" resultType=\"").append(packageName)
							.append(".entity.").append(entityName).append("\" resultMap=\"").append(listResultMap).append("\">\n");
					builder.append("\t\tSELECT *\n");
					builder.append("\t\tFROM ").append(tableName).append("\n");
					builder.append("\t\t").append(whereStr).append("\n");
					builder.append("\t\tORDER BY ${").append(queryVarName).append('.').append(orderField).append("} ${")
							.append(queryVarName).append('.').append(orderType).append("}\n");
					builder.append("\t</select>\n\n");
				}

				if (keyName != null) {
					//getById
					if (needGetById) {
						builder.append("\t<select id=\"getBy").append(byWhat)
								.append("\" resultType=\"").append(packageName).append(".entity.").append(entityName)
								.append("\" resultMap=\"mapping\">\n");
						builder.append("\t\tSELECT *\n");
						builder.append("\t\tFROM ").append(tableName).append("\n");
						builder.append("\t\tWHERE ").append(keyName).append(" = #{").append(camelKeyName).append("}\n");
						builder.append("\t</select>\n\n");
					}
					//updateActive
					if (active != null && needUpdateActive) {
						String camelActive = underscoreCaseToCamelCase(active);
						String titleActive = toTitleCase(camelActive);
						String methodName = "update" + titleActive;

						builder.append("\t<update id=\"").append(methodName).append("\">\n")
								.append("\t\tUPDATE ").append(tableName).append('\n')
								.append("\t\tSET ").append(active).append(" = #{").append(camelActive).append("}\n")
								.append("\t\tWHERE ").append(keyName).append(" = #{").append(camelKeyName).append("}\n")
								.append("\t</update>\n\n");
					}
					//delete
					if (needDelete) {
						builder.append("\t<delete id=\"delete\">\n");
						builder.append("\t\tDELETE FROM ").append(tableName).append("\n");
						builder.append("\t\tWHERE ").append(keyName).append(" = #{").append(camelKeyName).append("}\n");
						builder.append("\t</delete>\n\n");
					}
					//批量delete
					if (needBatchDelete) {
						builder.append("\t<delete id=\"batchDelete\">\n");
						builder.append("\t\tDELETE FROM ").append(tableName).append("\n");
						builder.append("\t\tWHERE ").append(keyName).append(" IN <foreach collection=\"list\" open=\"(\" separator=\",\" close=\")\" item=\"item\" index=\"index\">#{item}</foreach>\n");
						builder.append("\t</delete>\n\n");
					}

					if (hasSlaves) {
						for (Table slave : slaves) {
							String slaveTableName = slave.getTableName();
							String slaveEntityName = getEntityName(slaveTableName);
							String slaveMappingName = (slaveTableName.contains("_") ? slaveTableName.substring(slaveTableName.indexOf('_') + 1)
									: slaveTableName) + "_mapping";
							List<Column> slaveColumns = slave.getAllColumns();
							Set<String> slaveAutoIncrementCols = slave.getAllAutoIncrementCols();

							//add all
							builder.append("\t<insert id=\"addAll").append(slaveEntityName).append("\" parameterType=\"")
									.append(packageName).append(".entity.").append(slaveEntityName).append("\">\n");
							builder.append("\t\tINSERT INTO ").append(slaveTableName).append(" (\n");
							for (Column column : slaveColumns) {
								String columnName = column.getColumnName();
								if (slaveAutoIncrementCols.contains(columnName)) {
									continue;
								}
								builder.append("\t\t\t").append(columnName).append(",\n");
							}
							builder.deleteCharAt(builder.length() - 2);
							builder.append("\t\t) VALUES\n");
							builder.append("\t\t<foreach collection=\"list\" index=\"index\" item=\"item\" separator=\",\" >\n\t\t(\n");
							for (Column column : slaveColumns) {
								String columnName = column.getColumnName();
								if (slaveAutoIncrementCols.contains(columnName)) {
									continue;
								}
								String fieldName = underscoreCaseToCamelCase(columnName);
								builder.append("\t\t\t#{item.").append(fieldName).append("},\n");
							}
							builder.deleteCharAt(builder.length() - 2);
							builder.append("\t\t)\n\t\t</foreach>\n");
							builder.append("\t</insert>\n\n");

							//get all
							builder.append("\t<select id=\"getAll").append(slaveEntityName)
									.append("\" resultType=\"").append(packageName).append(".entity.").append(slaveEntityName)
									.append("\" resultMap=\"").append(slaveMappingName).append("\">\n");
							builder.append("\t\tSELECT *\n");
							builder.append("\t\tFROM ").append(slaveTableName).append("\n");
							builder.append("\t\tWHERE ").append(keyName).append(" = #{").append(camelKeyName).append("}\n");
							builder.append("\t</select>\n\n");

							//delete all
							builder.append("\t<delete id=\"deleteAll").append(slaveEntityName).append("\">\n");
							builder.append("\t\tDELETE FROM ").append(slaveTableName).append("\n");
							builder.append("\t\tWHERE ").append(keyName).append(" = #{").append(camelKeyName).append("}\n");
							builder.append("\t</delete>\n\n");
						}
					}
				}

				builder.append("</mapper>");
				bw.write(builder.toString());
				bw.flush();
			} catch (IOException e) {
				throw new IOException(e);
			}
		}
	}
	
	/**
	 * 生成service接口文件
	 */
	private void generateIService(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String serviceName = "I" + entityName + "Service";
			
			File f = new File(parentPath + "/service/inter");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/service/inter/" + serviceName + ".java";
			File service = new File(fileName);
			service.createNewFile();
			try (
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fileName), StandardCharsets.UTF_8))
			) {
				StringBuilder builder = new StringBuilder();
				builder.append("package ").append(packageName).append(".service.inter;\n\n");
				builder.append("import java.util.List;\n");
				builder.append("import ").append(packageName).append(".entity.").append(entityName).append(";\n");
				builder.append("import ").append(queryFullName).append(";\n");
				builder.append("import ").append(responseResultFullName).append(";\n\n");
				if (needList && !responseResultFullName.equals(listResponseResultFullName)) {
					builder.append("import ").append(listResponseResultFullName).append(";\n");
				}
				builder.append("public interface ").append(serviceName).append(" {\n\n");

				String keyName = null;
				String idName = null;
				String camelKeyName = null;
				String byWhat = null;
				String keyType = null;
				List<PrimaryKey> keys = table.getAllPrimaryKeys();

				if (keys != null && !keys.isEmpty()) {
					PrimaryKey key = keys.get(0);
					keyName = key.getPkName();
					camelKeyName = underscoreCaseToCamelCase(keyName);
					if (keyName.endsWith("id")) {
						idName = "id";
					} else {
						idName = camelKeyName;
					}
					byWhat = toTitleCase(idName);
					keyType = typeMap.get(key.getPkType());
				}

				String rrGenericName = responseResultName;
				String rrGenericIdName = responseResultName;
				if (rrGeneric) {
					rrGenericName = rrGenericName + "<" + entityName + ">";
					rrGenericIdName = rrGenericIdName + "<" + keyType + ">";
				}
				String lrrGenericName = listResponseResultName;
				if (listRrGeneric) {
					lrrGenericName = lrrGenericName + "<" + entityName + ">";
				}

				if (keyName != null && combineAddUpdate && needSet) {
					builder.append("\t").append(rrGenericIdName).append(" set(").append(entityName).append(" entity);\n\n");
				}

				if (needAdd) {
					builder.append("\t").append(rrGenericIdName).append(" add(").append(entityName).append(" entity);\n\n");
				}

				if (keyName != null && needUpdate) {
					builder.append("\t").append(rrGenericIdName).append(" update(").append(entityName).append(" entity);\n\n");
				}

				if (needList) {
					builder.append("\t").append(lrrGenericName).append(" list(").append(queryName)
							.append(" ").append(queryVarName).append(");\n\n");
				}

				if (keyName != null) {
					if (needGetById) {
						builder.append("\t").append(rrGenericName).append(" getBy").append(byWhat)
								.append("(").append(keyType).append(" ").append(camelKeyName).append(");\n\n");
					}
					if (active != null && needUpdateActive) {
						String camelActive = underscoreCaseToCamelCase(active);
						String titleActive = toTitleCase(camelActive);
						String methodName = "update" + titleActive;

						builder.append("\t").append(rrGenericName).append(' ').append(methodName)
								.append("(").append(keyType).append(" ").append(camelKeyName)
								.append(", Integer ").append(camelActive).append(");\n\n");
					}
					if (needDelete) {
						builder.append("\t").append(rrGenericName).append(" delete(")
								.append(keyType).append(" ").append(camelKeyName).append(");\n\n");
					}
					if (needBatchDelete) {
						builder.append("\t").append(rrGenericName).append(" batchDelete(List<").append(keyType).append("> ")
								.append(camelKeyName).append("s);\n\n");
					}
				}

				builder.append("}\n");
				
				bw.write(builder.toString());
				bw.flush();
			} catch (IOException e) {
				throw new IOException(e);
			}
		}
	}
	
	/**
	 * 生成service文件
	 */
	private void generateService(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String entityVarName = toContentCase(entityName);
			String iServiceName = "I" + entityName + "Service";
			String serviceName = entityName + "ServiceImpl";
			String mapperName = entityName + "Mapper";
			String mapperVarName = toContentCase(mapperName);

			List<Table> slaves = table.getAllSlaves();
			boolean hasSlave = slaves != null && !slaves.isEmpty();

			File f = new File(parentPath + "/service/impl");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/service/impl/" + serviceName + ".java";
			File service = new File(fileName);
			service.createNewFile();
			try (
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fileName), StandardCharsets.UTF_8));
			) {
				String keyName = null;
				String idName = null;
				String camelKeyName = null;
				String titleKeyName = null;
				String byWhat = null;
				String keyType = null;
				List<PrimaryKey> keys = table.getAllPrimaryKeys();

				if (keys != null && !keys.isEmpty()) {
					PrimaryKey key = keys.get(0);
					keyName = key.getPkName();
					camelKeyName = underscoreCaseToCamelCase(keyName);
					titleKeyName = toTitleCase(camelKeyName);
					if (keyName.endsWith("id")) {
						idName = "id";
					} else {
						idName = camelKeyName;
					}
					byWhat = toTitleCase(idName);
					keyType = typeMap.get(key.getPkType());
				}

				StringBuilder builder = new StringBuilder();
				builder.append("package ").append(packageName).append(".service.impl;\n\n");
				
				builder.append("import java.util.List;\n");
				builder.append("import java.util.Date;\n");
				builder.append("import java.util.Collections;\n");
				builder.append("import org.springframework.stereotype.Service;\n");
				builder.append("import javax.annotation.Resource;\n");
				builder.append("import org.springframework.transaction.annotation.Transactional;\n");
				builder.append(PaginatorHandler.getImports(paginator));
				builder.append("import ").append(packageName).append(".service.inter.").append(iServiceName).append(";\n");
				builder.append("import ").append(packageName).append(".mapper.").append(entityName).append("Mapper;\n");
				builder.append("import ").append(packageName).append(".entity.").append(entityName).append(";\n");
				if (hasSlave) {
					for (Table slave : slaves) {
						String slaveTableName = slave.getTableName();
						String slaveEntityName = getEntityName(slaveTableName);
						builder.append("import ").append(packageName).append(".entity.").append(slaveEntityName).append(";\n");
					}
					builder.append("import org.springframework.util.CollectionUtils;\n");
				}
				builder.append("import ").append(stringUtilFullName).append(";\n");
				if ("String".equals(keyType)) {
					builder.append("import ").append(getIdUtilFullName).append(";\n");
				}
				builder.append("import ").append(responseResultFullName).append(";\n");
				if (needList && !responseResultFullName.equals(listResponseResultFullName)) {
					builder.append("import ").append(listResponseResultFullName).append(";\n");
				}
				builder.append("import ").append(queryFullName).append(";\n\n");
				
				builder.append("@Service\n");
				builder.append("public class ").append(serviceName).append(" implements ").append(iServiceName).append(" {\n\n");

				String rrGenericName = responseResultName;
				String rrGenericIdName = responseResultName;
				if (rrGeneric) {
					rrGenericName = rrGenericName + "<" + entityName + ">";
					rrGenericIdName = rrGenericIdName + "<" + keyType + ">";
				}
				String lrrGenericName = listResponseResultName;
				if (listRrGeneric) {
					lrrGenericName = lrrGenericName + "<" + entityName + ">";
				}

				builder.append("\t@Resource\n");
				builder.append("\tprivate ").append(mapperName).append(" ").append(mapperVarName).append(";\n\n");

				if (keyName != null && combineAddUpdate && needSet) {
					if (hasSlave) {
						builder.append("\t@Transactional\n");
					}
					builder.append("\t@Override\n");
					builder.append("\tpublic ").append(rrGenericIdName).append(" set(").append(entityName).append(" entity) {\n");
					builder.append("\t\tif (entity == null) {\n");
					builder.append("\t\t\treturn new ").append(rrDiamondName).append("(").append(errorCode).append(", \"缺少参数\");\n");
					builder.append("\t\t}\n");
					if (hasSlave) {
						builder.append("\t\tString s = this.checkParam(entity);\n");
						builder.append("\t\tif (!").append(stringUtil).append('.').append(stringIsEmpty).append("(s)").append(") {\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName).append("(").append(errorCode).append(", s);\n");
						builder.append("\t\t}\n\n");
						builder.append("\t\tDate date = new Date();\n");
					}
					if ("String".equals(keyType)) {
						builder.append("\t\tif (").append(stringUtil).append('.').append(stringIsEmpty)
								.append('(').append("entity.get").append(toTitleCase(camelKeyName)).append("())").append(") {\n");
					} else {
						builder.append("\t\tif (").append("entity.get").append(toTitleCase(camelKeyName)).append("() == null").append(") {\n");
					}
					if (hasSlave) {
						if ("String".equals(keyType)) {
							builder.append("\t\t\tentity.set").append(toTitleCase(camelKeyName)).append('(').append(getIdUtil).append('.')
									.append(getIdMethod).append('(').append(String.join(", ", getIdParamArr)).append("));\n");
						}
						if (!"".equals(createdDate)) {
							builder.append("\t\t\tentity.set").append(toTitleCase(createdDate)).append("(date);\n");
						}
						if (!"".equals(createdBy)) {
							builder.append("\t\t\t//TODO entity.set").append(toTitleCase(createdBy)).append("(by whom);\n");
						}
						if (!"".equals(active)) {
							builder.append("\t\t\tentity.set").append(toTitleCase(active)).append("(").append(activeCode).append(");\n");
						}
						builder.append("\t\t\tif (").append(mapperVarName).append(".add(entity) < 1) {\n");
						builder.append("\t\t\t\treturn new ").append(rrDiamondName).append("(")
								.append(errorCode).append(", \"保存失败\");\n");
						builder.append("\t\t\t}\n");
					} else {
						builder.append("\t\t\treturn this.add(entity);\n");
					}
					builder.append("\t\t} else {\n");
					if (hasSlave) {
						if (!"".equals(updatedDate)) {
							builder.append("\t\t\tentity.set").append(toTitleCase(updatedDate)).append("(date);\n");
						}
						if (!"".equals(updatedBy)) {
							builder.append("\t\t\t//TODO entity.set").append(toTitleCase(updatedBy)).append("(by whom);\n");
						}
						builder.append("\t\t\t").append(keyType).append(' ').append(camelKeyName).append(" = entity.get")
								.append(titleKeyName).append("();\n");
						for (Table slave : slaves) {
							String slaveTableName = slave.getTableName();
							String slaveEntityName = getEntityName(slaveTableName);
							builder.append("\t\t\t").append(mapperVarName).append(".deleteAll").append(slaveEntityName).append('(')
									.append(camelKeyName).append(");\n");
						}
						builder.append("\t\t\tif (").append(mapperVarName).append(".update(entity) < 1) {\n");
						builder.append("\t\t\t\tthrow new IllegalStateException(\"编辑失败\");\n");
						builder.append("\t\t\t}\n");
					} else {
						builder.append("\t\t\treturn this.update(entity);\n");
					}
					builder.append("\t\t}\n");
					if (hasSlave) {
						builder.append('\n');
						for (Table slave : slaves) {
							String slaveTableName = slave.getTableName();
							String slaveEntityName = getEntityName(slaveTableName);
							String listName = toContentCase(slaveEntityName) + 's';
							builder.append("\t\tList<").append(slaveEntityName).append("> ").append(listName)
									.append(" = entity.get").append(slaveEntityName).append("s();\n");
							builder.append("\t\tif (!CollectionUtils.isEmpty(").append(listName).append(")) {\n");
							builder.append("\t\t\tfor (").append(slaveEntityName).append(" line : ").append(listName).append(") {\n");
							builder.append("\t\t\t\t//TODO setId()\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(camelKeyName)).append("(entity.get")
									.append(toTitleCase(camelKeyName)).append("());\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(createdDate)).append("(date);\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(updatedDate)).append("(date);\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(createdBy))
									.append("(entity.get").append(toTitleCase(createdBy)).append("());\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(updatedBy))
									.append("(entity.get").append(toTitleCase(updatedBy)).append("());\n");
							builder.append("\t\t\t}\n");
							builder.append("\t\t\t").append(mapperVarName).append(".addAll").append(slaveEntityName).append('(')
									.append(listName).append(");\n");
							builder.append("\t\t}\n\n");
						}
						builder.append("\t\t").append(rrGenericIdName).append(' ').append(responseResultVarName).append(" = new ")
												.append(rrDiamondName).append("(\"操作成功\");\n");
						builder.append("\t\t").append(responseResultVarName)
												.append(".setData(entity.get").append(titleKeyName).append("());\n");
						builder.append("\t\treturn ").append(responseResultVarName).append(";\n");
					}
					builder.append("\t}\n\n");
				}

				if (needAdd) {
					if (hasSlave) {
						builder.append("\t@Transactional\n");
					}
					builder.append("\t@Override\n");
					builder.append("\tpublic ").append(rrGenericIdName).append(" add(").append(entityName).append(" entity) {\n");
					builder.append("\t\tString s = this.checkParam(entity);\n");
					builder.append("\t\tif (!").append(stringUtil).append('.').append(stringIsEmpty).append("(s)").append(") {\n");
					builder.append("\t\t\treturn new ").append(rrDiamondName).append("(").append(errorCode).append(", s);\n");
					builder.append("\t\t}\n");
					if ("String".equals(keyType)) {
						builder.append("\t\tentity.set").append(toTitleCase(camelKeyName)).append('(').append(getIdUtil).append('.')
								.append(getIdMethod).append('(').append(String.join(", ", getIdParamArr)).append("));\n");
					}
					if (!"".equals(createdDate)) {
						builder.append("\t\tDate date = new Date();\n");
						builder.append("\t\tentity.set").append(toTitleCase(createdDate)).append("(date);\n");
					}
					if (!"".equals(createdBy)) {
						builder.append("\t\t//TODO entity.set").append(toTitleCase(createdBy)).append("(by whom);\n");
					}
					if (!"".equals(active)) {
						builder.append("\t\tentity.set").append(toTitleCase(active)).append("(").append(activeCode).append(");\n");
					}
					builder.append("\t\tif (").append(mapperVarName).append(".add(entity) < 1) {\n");
					builder.append("\t\t\treturn new ").append(rrDiamondName).append("(")
							.append(errorCode).append(", \"保存失败\");\n");
					builder.append("\t\t}\n\n");
					if (hasSlave) {
						for (Table slave : slaves) {
							String slaveTableName = slave.getTableName();
							String slaveEntityName = getEntityName(slaveTableName);

							String listName = toContentCase(slaveEntityName) + 's';
							builder.append("\t\tList<").append(slaveEntityName).append("> ").append(listName)
									.append(" = entity.get").append(slaveEntityName).append("s();\n");
							builder.append("\t\tif (!CollectionUtils.isEmpty(").append(listName).append(")) {\n");
							builder.append("\t\t\tfor (").append(slaveEntityName).append(" line : ").append(listName).append(") {\n");
							builder.append("\t\t\t\t//TODO setId()\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(camelKeyName)).append("(entity.get")
									.append(toTitleCase(camelKeyName)).append("());\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(createdDate)).append("(date);\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(updatedDate)).append("(date);\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(createdBy))
									.append("(entity.get").append(toTitleCase(createdBy)).append("());\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(updatedBy))
									.append("(entity.get").append(toTitleCase(updatedBy)).append("());\n");
							builder.append("\t\t\t}\n");
							builder.append("\t\t\t").append(mapperVarName).append(".addAll").append(slaveEntityName).append('(')
									.append(listName).append(");\n");
							builder.append("\t\t}\n\n");
						}
						builder.append('\n');
					}
					builder.append("\t\t").append(rrGenericIdName).append(' ').append(responseResultVarName).append(" = new ")
											.append(rrDiamondName).append("(\"保存成功\");\n");
					builder.append("\t\t").append(responseResultVarName)
											.append(".setData(entity.get").append(titleKeyName).append("());\n");
					builder.append("\t\treturn ").append(responseResultVarName).append(";\n");
					builder.append("\t}\n\n");
				}

				if (keyName != null && needUpdate) {
					if (hasSlave) {
						builder.append("\t@Transactional\n");
					}
					builder.append("\t@Override\n");
					builder.append("\tpublic ").append(rrGenericIdName).append(" update(").append(entityName).append(" entity) {\n");
					builder.append("\t\t").append(keyType).append(' ').append(camelKeyName).append(" = entity.get")
							.append(toTitleCase(camelKeyName)).append("();\n");
					if ("String".equals(keyType)) {
						builder.append("\t\tif (").append(stringUtil).append('.').append(stringIsEmpty)
								.append('(').append(camelKeyName).append(")) {\n");
					} else {
						builder.append("\t\tif (").append(camelKeyName).append(" == null) {\n");
					}
					builder.append("\t\t\treturn new ").append(rrDiamondName).append("(").append(errorCode).append(", \"缺少id\");\n");
					builder.append("\t\t}\n");
					builder.append("\t\tString s = this.checkParam(entity);\n");
					builder.append("\t\tif (!").append(stringUtil).append('.').append(stringIsEmpty).append("(s)").append(") {\n");
					builder.append("\t\t\treturn new ").append(rrDiamondName).append("(").append(errorCode).append(", s);\n");
					builder.append("\t\t}\n");
					if (!"".equals(updatedDate)) {
						builder.append("\t\tDate date = new Date();\n");
						builder.append("\t\tentity.set").append(toTitleCase(updatedDate)).append("(date);\n");
					}
					if (!"".equals(updatedBy)) {
						builder.append("\t\t//TODO entity.set").append(toTitleCase(updatedBy)).append("(by whom);\n");
					}
					if (hasSlave) {
						builder.append('\n');
						for (Table slave : slaves) {
							String slaveTableName = slave.getTableName();
							String slaveEntityName = getEntityName(slaveTableName);
							builder.append("\t\t").append(mapperVarName).append(".deleteAll").append(slaveEntityName).append('(')
									.append(camelKeyName).append(");\n");
							String listName = toContentCase(slaveEntityName) + 's';
							builder.append("\t\tList<").append(slaveEntityName).append("> ").append(listName)
									.append(" = entity.get").append(slaveEntityName).append("s();\n");
							builder.append("\t\tif (!CollectionUtils.isEmpty(").append(listName).append(")) {\n");
							builder.append("\t\t\tfor (").append(slaveEntityName).append(" line : ").append(listName).append(") {\n");
							builder.append("\t\t\t\t//TODO setId()\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(camelKeyName)).append("(entity.get")
									.append(toTitleCase(camelKeyName)).append("());\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(createdDate)).append("(date);\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(updatedDate)).append("(date);\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(createdBy))
									.append("(entity.get").append(toTitleCase(createdBy)).append("());\n");
							builder.append("\t\t\t\tline.set").append(toTitleCase(updatedBy))
									.append("(entity.get").append(toTitleCase(updatedBy)).append("());\n");
							builder.append("\t\t\t}\n");
							builder.append("\t\t\t").append(mapperVarName).append(".addAll").append(slaveEntityName).append('(')
									.append(listName).append(");\n");
							builder.append("\t\t}\n\n");
						}
					}
					builder.append("\t\tif (").append(mapperVarName).append(".update(entity) < 1) {\n");
					builder.append("\t\t\treturn new ").append(rrDiamondName)
							.append("(").append(errorCode).append(", \"编辑失败\");\n");
					builder.append("\t\t}\n");
					builder.append("\t\treturn new ").append(rrDiamondName).append("(\"编辑成功\");\n");
					builder.append("\t}\n\n");
				}

				if (needList) {
					builder.append("\t@Override\n");
					builder.append("\tpublic ").append(lrrGenericName).append(" list(").append(queryName)
							.append(" ").append(queryVarName).append(") {\n");
					builder.append("\t\tif (").append(queryVarName).append(" == null) {\n")
							.append("\t\t\t").append(queryVarName).append(" = new ").append(queryName).append("();\n")
							.append("\t\t}\n");
					if (orderField != null && orderFieldVal != null) {
						builder.append("\t\tif (").append(stringUtil).append('.').append(stringIsEmpty).append('(').append(queryVarName)
								.append(".get").append(toTitleCase(orderField)).append("())) {\n");
						builder.append("\t\t\t").append(queryVarName).append(".set").append(toTitleCase(orderField))
								.append("(\"").append(orderFieldVal).append("\");\n");
						orderTypeVal = (orderTypeVal != null && !orderTypeVal.trim().equals("")) ? orderTypeVal.toUpperCase() : "ASC";
						builder.append("\t\t\t").append(queryVarName).append(".set").append(toTitleCase(orderType))
								.append("(\"").append(orderTypeVal).append("\");\n");
						builder.append("\t\t}\n");
					}

					String titleTotal = toTitleCase(total);
					String titlePages = toTitleCase(pages);
					switch (paginator) {
						case "pageHelper":
							builder.append("\t\tPageHelper.startPage(").append(queryVarName).append(".getPage(), ")
									.append(queryVarName).append(".getLimit(), true);\n");
							builder.append("\t\tList<").append(entityName).append("> list = ").append(mapperVarName)
									.append(".list(").append(queryVarName).append(");\n");
							builder.append("\t\t").append(lrrGenericName).append(" ")
									.append(lrrGenericName).append(" = new ").append(lrrDiamondName).append("();\n");
							builder.append("\t\t").append(responseResultVarName).append(".set").append(toTitleCase(listRrData)).append("(list);\n");
							builder.append("\t\t").append(responseResultVarName)
									.append(".set").append(titleTotal)
									.append("((int)((Page<").append(entityName).append(">)list).getTotal());\n");
							builder.append("\t\t").append(responseResultVarName)
									.append(".set").append(titlePages)
									.append("((int)((Page<").append(entityName).append(">)list).getPages());\n");
							break;
						case "mybatis-paginator":
							builder.append("\t\tPageList<").append(entityName).append("> list = ").append(mapperVarName)
									.append(".list(").append(queryVarName).append(", new PageBounds(").append(queryVarName)
									.append(".getPage(), ").append(queryVarName).append(".getLimit()));\n");
							builder.append("\t\t").append(lrrGenericName).append(" ")
									.append(responseResultVarName).append(" = new ").append(lrrDiamondName).append("();\n");
							builder.append("\t\t").append(responseResultVarName).append(".set").append(toTitleCase(listRrData)).append("(list);\n");
							builder.append("\t\t").append(responseResultVarName).append(".set").append(titleTotal)
									.append("(list.getPaginator().getTotalCount());\n");
							builder.append("\t\t").append(responseResultVarName).append(".set").append(titlePages)
									.append("(list.getPaginator().getTotalPages());\n");
					}
					builder.append("\t\treturn ").append(responseResultVarName).append(";\n");
					builder.append("\t}\n\n");
				}

				if (keyName != null) {
					if (needGetById) {
						builder.append("\t@Override\n");
						builder.append("\tpublic ").append(rrGenericName).append(" getBy").append(byWhat).append("(").append(keyType).append(" ").append(camelKeyName).append(") {\n");
						builder.append("\t\t").append(entityName).append(" ").append(entityVarName).append(" = ").append(mapperVarName).append(".getBy").append(byWhat).append("(").append(camelKeyName).append(");\n");
						builder.append("\t\tif (").append(entityVarName).append(" == null) {\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName).append("(").append(errorCode).append(", \"找不到该记录\");\n");
						builder.append("\t\t}\n");
						builder.append("\t\t").append(rrGenericName).append(" ").append(responseResultVarName).append(" = new ").append(rrDiamondName).append("();\n");
						builder.append("\t\t").append(responseResultVarName).append(".set").append(toTitleCase(rrData)).append("(").append(entityVarName).append(");\n");
						builder.append("\t\treturn ").append(responseResultVarName).append(";\n");
						builder.append("\t}\n\n");
					}

					if (active != null && needUpdateActive) {
						String camelActive = underscoreCaseToCamelCase(active);
						String titleActive = toTitleCase(camelActive);
						String methodName = "update" + titleActive;

						builder.append("\t@Override\n");
						builder.append("\tpublic ").append(rrGenericName).append(' ').append(methodName).append("(").append(keyType).append(" ").append(camelKeyName).append(", Integer ").append(camelActive).append(") {\n");
						builder.append("\t\tif (").append(mapperVarName).append('.').append(methodName).append("(").append(camelKeyName).append(", ").append(camelActive).append(") < 1) {\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName).append("(").append(errorCode).append(", \"操作失败\");\n");
						builder.append("\t\t}\n");
						builder.append("\t\treturn new ").append(rrDiamondName).append("(").append("\"操作成功\");\n");
						builder.append("\t}\n\n");
					}

					if (needDelete) {
						if (hasSlave) {
							builder.append("\t@Transactional\n");
						}
						builder.append("\t@Override\n");
						builder.append("\tpublic ").append(rrGenericName).append(" delete(").append(keyType).append(" ").append(camelKeyName).append(") {\n");
						builder.append("\t\t").append(entityName).append(" ").append(entityVarName).append(" = ").append(mapperVarName).append(".getBy").append(byWhat).append("(").append(camelKeyName).append(");\n");
						builder.append("\t\tif (").append(entityVarName).append(" == null) {\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName).append("(")
								.append(errorCode).append(", \"删除失败，该记录不存在\");\n");
						builder.append("\t\t}\n");
						if (hasSlave) {
							builder.append('\n');
							for (Table slave : slaves) {
								String slaveTableName = slave.getTableName();
								String slaveEntityName = getEntityName(slaveTableName);
								builder.append("\t\t").append(mapperVarName).append(".deleteAll").append(slaveEntityName).append('(')
										.append(camelKeyName).append(");\n");
							}
						}
						builder.append("\t\tif (").append(mapperVarName).append(".delete(").append(camelKeyName).append(") < 1) {\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName).append("(")
								.append(errorCode).append(", \"删除失败\");\n");
						builder.append("\t\t}\n");
						builder.append("\t\t").append(rrGenericIdName).append(' ').append(responseResultVarName).append(" = new ")
								.append(rrDiamondName).append("(\"删除成功\");\n");
						builder.append("\t\t").append(responseResultVarName)
								.append(".setData(").append(entityVarName).append(");\n");
						builder.append("\t\treturn ").append(responseResultVarName).append(";\n");
						builder.append("\t}\n\n");
					}

					if (needBatchDelete) {
						builder.append("\t@Transactional\n");
						builder.append("\t@Override\n");
						builder.append("\tpublic ").append(rrGenericName).append(" batchDelete(List<").append(keyType).append("> ")
								.append(camelKeyName).append("s) {\n");

						builder.append("\t\tif (").append(camelKeyName).append("s == null || ").append(camelKeyName).append("s.isEmpty()) {\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName).append("(").append(errorCode).append(", \"请选择待删除的记录\");\n");
						builder.append("\t\t}\n");
						if (hasSlave) {
							builder.append('\n');
							builder.append("\t\tfor (").append(keyType).append(' ').append(camelKeyName).append(" : ")
									.append(camelKeyName).append("s) {\n");
							for (Table slave : slaves) {
								String slaveTableName = slave.getTableName();
								String slaveEntityName = getEntityName(slaveTableName);

								builder.append("\t\t\t").append(mapperVarName).append(".deleteAll").append(slaveEntityName).append("(")
										.append(camelKeyName).append(");\n");
							}
							builder.append("\t\t}\n");
							builder.append('\n');
						}
						builder.append("\t\tif (").append(mapperVarName).append(".batchDelete(").append(camelKeyName).append("s) != ")
								.append(camelKeyName).append("s.size()) {\n");
						builder.append("\t\t\tthrow new IllegalStateException(\"批量删除失败\");\n");
						builder.append("\t\t}\n");
						builder.append("\t\treturn new ").append(rrDiamondName).append("(\"批量删除成功\");\n");
						builder.append("\t}\n\n");
					}
				}

				if (needSet || needAdd || needUpdate) {
					builder.append("\tprivate String checkParam(").append(entityName).append(" entity) {\n");
					builder.append("\t\t//TODO 校验参数逻辑,返回错误提示\n");
					builder.append("\t\treturn \"\";\n");
					builder.append("\t}\n\n");
				}
				
				builder.append("}");
				
				bw.write(builder.toString());
				bw.flush();
			} catch (IOException e) {
				throw new IOException(e);
			}
		}
	}
	
	/**
	 * 生成controller文件
	 */
	private void generateController(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String controllerName = entityName + "Controller";
			String serviceName = entityName + "Service";
			String serviceVarName = toContentCase(serviceName);

			List<Table> slaves = table.getAllSlaves();
			boolean hasSlave = slaves != null && !slaves.isEmpty();
			
			File f = new File(parentPath + "/controller");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/controller/" + controllerName + ".java";
			File controller = new File(fileName);
			controller.createNewFile();
			try (
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fileName), StandardCharsets.UTF_8))
			) {
				StringBuilder builder = new StringBuilder();
				
				builder.append("package ").append(packageName).append(".controller;\n\n");

				builder.append("import java.util.List;\n");
				builder.append("import java.util.Arrays;\n");
				builder.append("import org.springframework.web.bind.annotation.RestController;\n");
				builder.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
				builder.append("import org.springframework.web.bind.annotation.RequestMethod;\n");
				builder.append("import org.springframework.web.bind.annotation.RequestBody;\n");
				builder.append("import javax.annotation.Resource;\n");
				builder.append("import org.slf4j.Logger;\n");
				builder.append("import org.slf4j.LoggerFactory;\n");
				builder.append("import ").append(packageName).append(".service.inter.I").append(serviceName).append(";\n");
				builder.append("import ").append(packageName).append(".entity.").append(entityName).append(";\n");
				builder.append("import ").append(responseResultFullName).append(";\n");
				if (needList && !responseResultFullName.equals(listResponseResultFullName)) {
					builder.append("import ").append(listResponseResultFullName).append(";\n");
				}
				builder.append("import ").append(queryFullName).append(";\n\n");
				
				builder.append("@RestController\n");
				builder.append("@RequestMapping(\"/").append(entityName.toLowerCase()).append("\")\n");
				builder.append("public class ").append(controllerName).append(" {\n\n");
				
				builder.append("\t@Resource\n");
				builder.append("\tprivate I").append(serviceName).append(" ").append(serviceVarName).append(";\n\n");

				builder.append("\tprivate Logger logger = LoggerFactory.getLogger(this.getClass());\n\n");

				String keyName = null;
				String idName = null;
				String camelKeyName = null;
				String byWhat = null;
				String keyType = null;
				List<PrimaryKey> keys = table.getAllPrimaryKeys();

				if (keys != null && !keys.isEmpty()) {
					PrimaryKey key = keys.get(0);
					keyName = key.getPkName();
					camelKeyName = underscoreCaseToCamelCase(keyName);
					if (keyName.endsWith("id")) {
						idName = "id";
					} else {
						idName = camelKeyName;
					}
					byWhat = toTitleCase(idName);
					keyType = typeMap.get(key.getPkType());
				}

				String rrGenericName = responseResultName;
				String rrGenericIdName = responseResultName;
				if (rrGeneric) {
					rrGenericName = rrGenericName + "<" + entityName + ">";
					rrGenericIdName = rrGenericIdName + "<" + keyType + ">";
				}
				String lrrGenericName = listResponseResultName;
				if (listRrGeneric) {
					lrrGenericName = lrrGenericName + "<" + entityName + ">";
				}
				String consumeStr = "";
				if ("json".equalsIgnoreCase(consumes)) {
					consumeStr = "@RequestBody ";
				}

				if (keyName != null && combineAddUpdate && needSet) {
					builder.append("\t@RequestMapping(value = \"/set\", method = RequestMethod.POST)\n");
					builder.append("\tpublic ").append(rrGenericIdName).append(" set(")
							.append(consumeStr).append(entityName).append(" entity) {\n");
					builder.append("\t\ttry {\n");
					builder.append("\t\t\treturn ").append(serviceVarName).append(".set(entity);\n");
					if (hasSlave) {
						builder.append("\t\t} catch (IllegalStateException e) {\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName)
								.append("(").append(errorCode).append(", e.getMessage());\n");
					}
					builder.append("\t\t} catch (Exception e) {\n");
					builder.append("\t\t\tlogger.error(\"\", e);\n");
					builder.append("\t\t\treturn new ").append(rrDiamondName)
							.append("(").append(errorCode).append(", ").append("\"服务器异常\");\n");
					builder.append("\t\t}\n");
					builder.append("\t}\n\n");
				} else if (!combineAddUpdate) {
					if (needAdd) {
						builder.append("\t@RequestMapping(value = \"/add\", method = RequestMethod.POST)\n");
						builder.append("\tpublic ").append(rrGenericIdName).append(" add(")
								.append(consumeStr).append(entityName).append(" entity) {\n");
						builder.append("\t\ttry {\n");
						builder.append("\t\t\treturn ").append(serviceVarName).append(".add(entity);\n");
						if (hasSlave) {
							builder.append("\t\t} catch (IllegalStateException e) {\n");
							builder.append("\t\t\treturn new ").append(rrDiamondName)
									.append("(").append(errorCode).append(", e.getMessage());\n");
						}
						builder.append("\t\t} catch (Exception e) {\n");
						builder.append("\t\t\tlogger.error(\"\", e);\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName)
								.append("(").append(errorCode).append(", ").append("\"服务器异常\");\n");
						builder.append("\t\t}\n");
						builder.append("\t}\n\n");
					}

					if (keyName != null && needUpdate) {
						builder.append("\t@RequestMapping(value = \"/update\", method = RequestMethod.POST)\n");
						builder.append("\tpublic ").append(rrGenericIdName).append(" update(").append(consumeStr).append(entityName).append(" entity) {\n");
						builder.append("\t\ttry {\n");
						builder.append("\t\t\treturn ").append(serviceVarName).append(".update(entity);\n");
						if (hasSlave) {
							builder.append("\t\t} catch (IllegalStateException e) {\n");
							builder.append("\t\t\treturn new ").append(rrDiamondName)
									.append("(").append(errorCode).append(", e.getMessage());\n");
						}
						builder.append("\t\t} catch (Exception e) {\n");
						builder.append("\t\t\tlogger.error(\"\", e);\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName)
								.append("(").append(errorCode).append(", ").append("\"服务器异常\");\n");
						builder.append("\t\t}\n");
						builder.append("\t}\n\n");
					}
				}

				if (needList) {
					builder.append("\t@RequestMapping(\"/list\")\n");
					builder.append("\tpublic ").append(lrrGenericName)
							.append(" list(@RequestBody(required = false) ").append(queryName).append(" ").append(queryVarName).append(") {\n");
					builder.append("\t\ttry {\n");
					builder.append("\t\t\treturn ").append(serviceVarName).append(".list(").append(queryVarName).append(");\n");
					builder.append("\t\t} catch (Exception e) {\n");
					builder.append("\t\t\tlogger.error(\"\", e);\n");
					builder.append("\t\t\treturn new ").append(lrrDiamondName)
							.append("(").append(errorCode).append(", ").append("\"服务器异常\");\n");
					builder.append("\t\t}\n");
					builder.append("\t}\n\n");
				}

				if (keyName != null) {
					if (needGetById) {
						builder.append("\t@RequestMapping(\"/getBy").append(byWhat).append("\")\n");
						builder.append("\tpublic ").append(rrGenericName).append(" getBy").append(byWhat)
								.append('(').append(keyType).append(" ").append(camelKeyName).append(") {\n");
						builder.append("\t\ttry {\n");
						builder.append("\t\t\treturn ").append(serviceVarName).append(".getBy").append(byWhat).append('(').append(camelKeyName).append(");\n");
						builder.append("\t\t} catch (Exception e) {\n");
						builder.append("\t\t\tlogger.error(\"\", e);\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName)
								.append("(").append(errorCode).append(", ").append("\"服务器异常\");\n");
						builder.append("\t\t}\n");
						builder.append("\t}\n\n");
					}

					if (active != null && needUpdateActive) {
						String camelActive = underscoreCaseToCamelCase(active);
						String titleActive = toTitleCase(camelActive);
						String methodName = "update" + titleActive;
						builder.append("\t@RequestMapping(\"/").append(methodName).append("\")\n");
						builder.append("\tpublic ").append(rrGenericName).append(' ').append(methodName)
								.append('(').append(keyType).append(" ").append(camelKeyName)
								.append(", Integer ").append(camelActive).append(") {\n");
						builder.append("\t\ttry {\n");
						builder.append("\t\t\treturn ").append(serviceVarName).append('.').append(methodName)
								.append('(').append(camelKeyName).append(", ").append(camelActive).append(");\n");
						builder.append("\t\t} catch (Exception e) {\n");
						builder.append("\t\t\tlogger.error(\"\", e);\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName)
								.append("(").append(errorCode).append(", ").append("\"服务器异常\");\n");
						builder.append("\t\t}\n");
						builder.append("\t}\n\n");
					}

					if (needDelete) {
						builder.append("\t@RequestMapping(value = \"/delete\", method = RequestMethod.POST)\n");
						builder.append("\tpublic ").append(rrGenericName).append(" delete(").append(keyType).append(" ").append(camelKeyName).append(") {\n");
						builder.append("\t\ttry {\n");
						builder.append("\t\t\treturn ").append(serviceVarName).append(".delete(").append(camelKeyName).append(");\n");
						if (hasSlave) {
							builder.append("\t\t} catch (IllegalStateException e) {\n");
							builder.append("\t\t\treturn new ").append(rrDiamondName)
									.append("(").append(errorCode).append(", e.getMessage());\n");
						}
						builder.append("\t\t} catch (Exception e) {\n");
						builder.append("\t\t\tlogger.error(\"\", e);\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName)
								.append("(").append(errorCode).append(", ").append("\"服务器异常\");\n");
						builder.append("\t\t}\n");
						builder.append("\t}\n\n");
					}

					if (needBatchDelete) {
						builder.append("\t@RequestMapping(value = \"/batchDelete\", method = RequestMethod.POST)\n");
						builder.append("\tpublic ").append(rrGenericName).append(" batchDelete(").append(keyType).append("[] ").append(camelKeyName).append("s) {\n");
						builder.append("\t\ttry {\n");
						builder.append("\t\t\treturn ").append(serviceVarName).append(".batchDelete(Arrays.asList(").append(camelKeyName).append("s));\n");
						builder.append("\t\t} catch (IllegalStateException e) {\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName)
								.append("(").append(errorCode).append(", e.getMessage());\n");
						builder.append("\t\t} catch (Exception e) {\n");
						builder.append("\t\t\tlogger.error(\"\", e);\n");
						builder.append("\t\t\treturn new ").append(rrDiamondName)
								.append("(").append(errorCode).append(", ").append("\"服务器异常\");\n");
						builder.append("\t\t}\n");
						builder.append("\t}\n\n");
					}
				}
				
				builder.append("}");
				
				bw.write(builder.toString());
				bw.flush();
			} catch (IOException e) {
				throw new IOException(e);
			}
		}
	}
	
	/**
	 * 首字母变大写
	 * @return 首字母大写的字符串
	 */
	private static String toTitleCase(String str) {
		char[] chs = str.toCharArray();
		if (chs[0] >= 97 && chs[0] <= 122) {
			chs[0] -= 32;
		}
		return String.valueOf(chs);
	}
	
	private static String toContentCase(String str) {
		char[] chs = str.toCharArray();
		if (chs[0] >= 65 && chs[0] <= 90) {
			chs[0] += 32;
		}
		return String.valueOf(chs);
	}

	private static String getVarNameByCamelCase(String str) {
		StringBuilder builder = new StringBuilder();
		char[] chs = str.toCharArray();
		for (char ch : chs) {
			if (ch >= 65 && ch <= 90) {
				ch += 32;
				builder.append(ch);
			}
		}
		return builder.toString();
	}

	public static String underscoreCaseToCamelCase(String str) {
		if (str == null || "".equals(str)) {
			return str;
		}
		String[] data = str.split("[_]+");
		if (data.length > 1) {
			StringBuilder sb = new StringBuilder();
			sb.append(data[0]);
			for (int i = 1; i < data.length; i++) {
				data[i] = toTitleCase(data[i]);
				sb.append(data[i]);
			}
			return sb.toString();
		} else {
			return str;
		}
	}

	public static String camelCaseToUnderscoreCase(String str) {
		if (str == null || "".equals(str)) {
			return str;
		}
		StringBuilder builder = new StringBuilder(str);
		char first = builder.charAt(0);
		if (first >= 'A' && first <= 'Z') {
			builder.replace(0, 1, String.valueOf(first + 32));
		}
		for (int i = builder.length() - 1; i > 0; i--) {
			char ch = builder.charAt(i);
			if (ch >= 'A' && ch <= 'Z') {
				builder.replace(i, i + 1, "_" + (char)(ch+32));
			}
		}
		return builder.toString();
	}
}
