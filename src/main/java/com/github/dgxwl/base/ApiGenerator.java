package com.github.dgxwl.base;

import com.github.dgxwl.base.entity.Column;
import com.github.dgxwl.base.entity.PrimaryKey;
import com.github.dgxwl.base.entity.Table;
import com.github.dgxwl.base.handler.paginator.PaginatorHandler;
import com.github.dgxwl.base.handler.TableHandler;
import com.github.dgxwl.base.handler.paginator.strategy.IPaginatorStrategy;
import com.github.dgxwl.util.DBUtils;
import com.github.dgxwl.util.FileUtil;
import com.github.dgxwl.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * 生成代码文件
 * @author dgxwl
 *
 */
public class ApiGenerator {
	
	private List<Table> tables;
	private List<String> entityNames;
	private Set<String> tableSet;
	private String oneToMany;

	private static String path;
	private static String packageName;
	private static String defaultTablesStr;
	private static Set<String> defaultTableSet;
	private static String defaultOneToMany;
	static {
		Properties config = DBUtils.getConfigs();
		path = config.getProperty("path");
		packageName = config.getProperty("package");

		defaultTableSet = new HashSet<>();
		defaultTablesStr = Optional.ofNullable(config.getProperty("tables")).orElse("");
		String[] dtsSplit = defaultTablesStr.split(",");
		defaultTableSet.addAll(Arrays.asList(dtsSplit));
		defaultOneToMany = Optional.ofNullable(config.getProperty("one_to_many")).orElse("");
		dtsSplit = defaultOneToMany.split("[\\s]*[,，][\\s]*");
		for (String s : dtsSplit) {
			String[] spl = s.split("[\\s]*:[\\s]*");
			defaultTableSet.add(spl[0]);
		}
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
		typeMap.put("JSON", "String");
		typeMap.put("JSONB", "String");
		
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
		fullNameTypeMap.put("JSON", "java.lang.String");
		fullNameTypeMap.put("JSONB", "java.lang.String");

		typeImportMap.put("BigDecimal", "import java.math.BigDecimal;");
		typeImportMap.put("Date", "import java.util.Date;");
	}

	private static String removePrefix;
	private static String mapperPackageName;
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
	private static String urlPrefix;
	private static String urlSuffix;
	private static String baseController;
	private static String baseControllerShortName;
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
	private static boolean needSet;
	private static boolean needAdd;
	private static boolean needUpdate;
	private static boolean needList;
	private static boolean needGetById;
	private static boolean needUpdateActive;
	private static boolean needDelete;
	private static boolean needBatchDelete;
	
	static {
		try (InputStream in = ApiGenerator.class.getClassLoader().getResourceAsStream("base.properties")) {
			Properties prop = new Properties();
			prop.load(in);
			//读取配置参数
			removePrefix = Optional.ofNullable(prop.getProperty("remove_prefix")).orElse("true");
			mapperPackageName = Optional.ofNullable(prop.getProperty("mapper_package_pame")).orElse("mapper");
			queryFullName = Optional.ofNullable(prop.getProperty("query")).orElse("");
			responseResultFullName = Optional.ofNullable(prop.getProperty("response_result")).orElse("");
			listResponseResultFullName = Optional.ofNullable(prop.getProperty("list_response_result")).orElse("");
			rrData = Optional.ofNullable(prop.getProperty("rr_data")).orElse("");
			listRrData = Optional.ofNullable(prop.getProperty("ll_rr_data")).orElse("");
			rrGenericStr = Optional.ofNullable(prop.getProperty("rr_generic")).orElse("");
			listRrGenericStr = Optional.ofNullable(prop.getProperty("list_rr_generic")).orElse("");
			whereStr = Optional.ofNullable(prop.getProperty("where_str")).orElse("");
			stringUtilFullName = Optional.ofNullable(prop.getProperty("string_util")).orElse("");
			stringIsEmpty = Optional.ofNullable(prop.getProperty("string_is_empty_method")).orElse("");
			getIdUtilFullName = Optional.ofNullable(prop.getProperty("get_id_util")).orElse("");
			getIdMethod = Optional.ofNullable(prop.getProperty("get_id_method")).orElse("");
			getIdParams = Optional.ofNullable(prop.getProperty("get_id_params")).orElse("");
			successCode = Optional.ofNullable(prop.getProperty("success_code")).orElse("");
			errorCode = Optional.ofNullable(prop.getProperty("error_code")).orElse("");
			pages = Optional.ofNullable(prop.getProperty("pages")).orElse("");
			total = Optional.ofNullable(prop.getProperty("total")).orElse("");
			paginator = Optional.ofNullable(prop.getProperty("paginator")).orElse("pageHelper");
			urlPrefix = Optional.ofNullable(prop.getProperty("url_prefix")).orElse("");
			urlSuffix = Optional.ofNullable(prop.getProperty("url_suffix")).orElse("");
			baseController = Optional.ofNullable(prop.getProperty("base_controller")).orElse("");
			consumes = Optional.ofNullable(prop.getProperty("consumes")).orElse("");
			combineAddUpdateStr = Optional.ofNullable(prop.getProperty("combine_add_update")).orElse("");
			orderField = Optional.ofNullable(prop.getProperty("order_field")).orElse("");
			orderType = Optional.ofNullable(prop.getProperty("order_type")).orElse("");
			orderFieldVal = Optional.ofNullable(prop.getProperty("order_field_val")).orElse("");
			orderTypeVal = Optional.ofNullable(prop.getProperty("order_type_val")).orElse("");
			createdDate = Optional.ofNullable(prop.getProperty("created_date")).orElse("");
			createdBy = Optional.ofNullable(prop.getProperty("created_by")).orElse("");
			updatedDate = Optional.ofNullable(prop.getProperty("updated_date")).orElse("");
			updatedBy = Optional.ofNullable(prop.getProperty("updated_by")).orElse("");
			active = Optional.ofNullable(prop.getProperty("active")).orElse("");
			activeCode = Optional.ofNullable(prop.getProperty("active_code")).orElse("");
			need = Optional.ofNullable(prop.getProperty("need")).orElse("");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		queryName = StringUtil.getSimpleClassName(queryFullName);
		queryVarName = StringUtil.classNameToVarName(queryName);
		responseResultName = StringUtil.getSimpleClassName(responseResultFullName);
		listResponseResultName = StringUtil.getSimpleClassName(listResponseResultFullName);
		rrDiamondName = responseResultName;
		lrrDiamondName = listResponseResultName;
		responseResultVarName = StringUtil.classNameToSimpleVarName(responseResultName);
		rrGeneric = "true".equalsIgnoreCase(rrGenericStr);
		if (rrGeneric) {
			rrDiamondName += "<>";
		}
		listRrGeneric = "true".equalsIgnoreCase(listRrGenericStr);
		if (listRrGeneric) {
			lrrDiamondName += "<>";
		}
		stringUtil = StringUtil.getSimpleClassName(stringUtilFullName);
		getIdUtil = StringUtil.getSimpleClassName(getIdUtilFullName);
		getIdParamArr = getIdParams.split(",");
		baseControllerShortName = StringUtil.getSimpleClassName(baseController);
		combineAddUpdate = "true".equalsIgnoreCase(combineAddUpdateStr);
		active = Optional.ofNullable(active).orElse("");
		activeCode = Optional.ofNullable(activeCode).orElse("");
		List<String> needs = Arrays.asList(need.split("[\\s]*,[\\s]*"));
		needSet = needs.contains("set");
		needAdd = needs.contains("add");
		needUpdate = needs.contains("update");
		needList = needs.contains("list");
		needGetById = needs.contains("getById");
		needUpdateActive = needs.contains("updateActive");
		needDelete = needs.contains("delete");
		needBatchDelete = needs.contains("batchDelete");
	}

	public ApiGenerator(Set<String> tableSet, String oneToMany) {
		this.tableSet = tableSet;
		this.oneToMany = oneToMany;
	}

	public static void main(String[] args) {
		try {
			new ApiGenerator(defaultTableSet, defaultOneToMany).generate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generate() throws IOException {
		TableHandler tableHandler = new TableHandler();
		tableHandler.readTables(tableSet, oneToMany);
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
			
			StringBuilder packageAndImportBuilder = new StringBuilder();
			packageAndImportBuilder.append("package ").append(packageName).append(".entity;\n\n");
			packageAndImportBuilder.append("import com.fasterxml.jackson.annotation.JsonIgnoreProperties;\n");
			packageAndImportBuilder.append("import lombok.Data;\n");
			if (!slaveTables.isEmpty()) {
				packageAndImportBuilder.append("import java.util.List;\n");
			}
			
			StringBuilder builder = new StringBuilder();
			builder.append("@Data\n");
			builder.append("@JsonIgnoreProperties(ignoreUnknown = true)\n");
			builder.append("public class ").append(entityName).append(" {\n");
			List<Column> fields = table.getAllColumns();
			for (Column column : fields) {
				String fieldName = StringUtil.underscoreCaseToCamelCase(column.getColumnName());
				String fieldType = typeMap.get(column.getType());

				switch (fieldType) {
					case "Date":
					case "BigDecimal":
						if (packageAndImportBuilder.indexOf(typeImportMap.get(fieldType)) == -1) {
							packageAndImportBuilder.append(typeImportMap.get(fieldType)).append("\n");
						}
						break;
				}
				
				builder.append("\tprivate ").append(fieldType).append(" ").append(fieldName).append(";");
				
				boolean hasRemark = column.getRemarks() != null && !column.getRemarks().equals("");
				if (hasRemark) {
					builder.append("  //").append(column.getRemarks());
				}
				builder.append("\n");
			}
			for (Table slaveTable : slaveTables) {
				String slaveTableName = slaveTable.getTableName();
				String slaveEntityName = getEntityName(slaveTableName);
				String slaveEntityVarName = StringUtil.classNameToVarName(slaveEntityName);
				builder.append("\tprivate List<").append(slaveEntityName).append("> ").append(StringUtil.toPlural(slaveEntityVarName)).append(";\n");
			}
			if (!slaveTables.isEmpty()) {
				generateEntity(parentPath, packageName, slaveTables);
			}

			builder.append("}\n");
			
			packageAndImportBuilder.append("\n");
			packageAndImportBuilder.append(builder);
			
			String fileName = parentPath + "/entity/" + entityName + ".java";
			FileUtil.mkdirIfNotExists(parentPath + "/entity");
			FileUtil.writeTextToFile(fileName, packageAndImportBuilder.toString());
		}
	}

	private String getEntityName(String tableName) {
		String finalName;
		if (!tableName.contains("_")) {
			return StringUtil.toTitleCase(StringUtil.underscoreCaseToCamelCase(tableName));
		}
		finalName = tableName.substring(tableName.indexOf('_') + 1);
		if (Collections.frequency(entityNames, finalName) > 1) {
			finalName = tableName;
		}
		return StringUtil.toTitleCase(StringUtil.underscoreCaseToCamelCase(finalName));
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
			builder.append("package ").append(packageName).append('.').append(mapperPackageName).append(";\n\n");

			builder.append("import org.apache.ibatis.annotations.Param;\n");
			builder.append("import ").append(packageName).append(".entity.").append(entityName).append(";\n");
			if (hasSlave) {
				for (Table slave : slaves) {
					String slaveTableName = slave.getTableName();
					String slaveEntityName = getEntityName(slaveTableName);
					builder.append("import ").append(packageName).append(".entity.").append(slaveEntityName).append(";\n");
				}
			}
			if (!StringUtil.isEmpty(queryFullName)) {
				builder.append("import ").append(queryFullName).append(";\n\n");
			}
			if (needList || hasSlave) {
				builder.append("import java.util.List;\n");
			}
            IPaginatorStrategy strategy = PaginatorHandler.getStrategy(paginator);
			if (strategy != null) {
                builder.append(strategy.getMapperImport()).append("\n");
            }

			builder.append("public interface ").append(mapperName).append(" {\n\n");

			//add
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
				camelKeyName = StringUtil.underscoreCaseToCamelCase(keyName);
				if (keyName.endsWith("id")) {
					idName = "id";
				} else {
					idName = camelKeyName;
				}
				byWhat = StringUtil.toTitleCase(idName);
				keyType = typeMap.get(key.getPkType());
			}

			//update
			if (keyName != null && needUpdate) {
				builder.append("\tInteger " + "update(").append(entityName).append(" entity);\n\n");
			}

			//list
			if (needList) {
				PaginatorHandler.getStrategy(paginator).getMapperListDefines(builder, entityName, queryName, queryVarName);
			}

			if (keyName != null) {
				//getById
				if (needGetById) {
					builder.append("\t").append(entityName).append(" getBy").append(byWhat)
							.append("(").append(keyType).append(" ").append(camelKeyName).append(");\n\n");
				}
				//updateActive
				if (active != null && needUpdateActive) {
					String camelActive = StringUtil.underscoreCaseToCamelCase(active);
					String titleActive = StringUtil.toTitleCase(camelActive);
					String methodName = "update" + titleActive;

					builder.append("\tInteger ").append(methodName).append("(@Param(\"").append(camelKeyName).append("\") ")
							.append(keyType).append(' ').append(camelKeyName).append(", @Param(\"").append(camelActive)
							.append("\") Integer ").append(camelActive).append(");\n\n");
				}
				//delete
				if (needDelete) {
					builder.append("\tInteger delete(").append(keyType).append(" ").append(camelKeyName).append(");\n\n");
				}
				//batchDelete
				if (needBatchDelete) {
					builder.append("\tInteger batchDelete(@Param(\"list\") List<").append(keyType)
							.append("> ").append(camelKeyName).append("s);\n\n");
				}

				//addAll, getAll, deleteAll slaves
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
			
			String fileName = parentPath + "/" + mapperPackageName + "/" + mapperName + ".java";
			FileUtil.mkdirIfNotExists(parentPath + "/" + mapperPackageName);
			FileUtil.writeTextToFile(fileName, builder.toString());
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
			
			StringBuilder builder = new StringBuilder();
			builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			builder.append("<!DOCTYPE mapper\n");
			builder.append("  PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n");
			builder.append("  \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n");
			
			builder.append("<mapper namespace=\"").append(packageName).append('.').append(mapperPackageName)
				.append('.').append(entityName).append("Mapper\">\n\n");

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
				camelKeyName = StringUtil.underscoreCaseToCamelCase(keyName);
				if (keyName.endsWith("id")) {
					idName = "id";
				} else {
					idName = camelKeyName;
				}
				byWhat = StringUtil.toTitleCase(idName);
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
				String fieldName = StringUtil.underscoreCaseToCamelCase(columnName);
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
				//collection of slaves
				builder.append("\t<resultMap id=\"mapping\" type=\"")
						.append(packageName).append(".entity.").append(entityName).append("\">\n");
				builder.append(resultMapColBuilder);

				for (Table slave : slaves) {
					String slaveTableName = slave.getTableName();
					String slaveEntityName = getEntityName(slaveTableName);
					String slaveEntityVarName = StringUtil.classNameToVarName(slaveEntityName);
					builder.append("\t\t<collection property=\"").append(StringUtil.toPlural(slaveEntityVarName))
							.append("\" column=\"").append(keyName)
							.append("\"\n\t\t\t\t\tofType=\"").append(packageName).append(".entity.").append(slaveEntityName)
							.append("\"\n\t\t\t\t\tselect=\"").append(packageName).append(".mapper.").append(entityName)
							.append("Mapper.getAll").append(slaveEntityName).append("\" />\n");
				}
				builder.append("\t</resultMap>\n\n");

				//slave resultMap
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
						String fieldName = StringUtil.underscoreCaseToCamelCase(columnName);
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
			
			//selectAllColumns
			builder.append("\t<sql id=\"selectCols\">\n");
			builder.append("\t\tSELECT\n");
			for (int i = 0, size = columns.size(); i < size; i++) {
				Column column = columns.get(i);
				builder.append("\t\t\t").append(column.getColumnName());
				if (i < size - 1) {
					builder.append(',');
				}
				builder.append('\n');
			}
			builder.append("\t\tFROM ").append(tableName).append('\n');
			builder.append("\t</sql>\n\n");

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
					if (autoIncrementCols.contains(StringUtil.camelCaseToUnderscoreCase(fieldName))) {
						continue;
					}
					builder.append("\t\t\t#{").append(fieldName).append("},\n");
				}
				builder.deleteCharAt(builder.length() - 2);
				builder.append("\t\t)\n");
				builder.append("\t</insert>\n\n");
			}

			//update
			if (keyName != null && needUpdate) {
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
				builder.append("\t\t<include refid=\"selectCols\" />\n");
				if (!StringUtil.isEmpty(whereStr)) {
					builder.append("\t\t").append(whereStr).append("\n");
				}
				if (!StringUtil.isEmpty(queryVarName) && !StringUtil.isEmpty(orderField) && !StringUtil.isEmpty(orderType)) {
					builder.append("\t\tORDER BY ${").append(queryVarName).append('.').append(orderField).append("} ${")
					.append(queryVarName).append('.').append(orderType).append("}\n");
				}
				builder.append("\t</select>\n\n");
			}

			if (keyName != null) {
				//getById
				if (needGetById) {
					builder.append("\t<select id=\"getBy").append(byWhat)
							.append("\" resultType=\"").append(packageName).append(".entity.").append(entityName)
							.append("\" resultMap=\"mapping\">\n");
					builder.append("\t\t<include refid=\"selectCols\" />\n");
					builder.append("\t\tWHERE ").append(keyName).append(" = #{").append(camelKeyName).append("}\n");
					builder.append("\t</select>\n\n");
				}
				//updateActive
				if (active != null && needUpdateActive) {
					String camelActive = StringUtil.underscoreCaseToCamelCase(active);
					String titleActive = StringUtil.toTitleCase(camelActive);
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
							String fieldName = StringUtil.underscoreCaseToCamelCase(columnName);
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

			String fileName = parentPath + "/mappers/" + mapperName + ".xml";
			FileUtil.mkdirIfNotExists(parentPath + "/mappers");
			FileUtil.writeTextToFile(fileName, builder.toString());
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
			
			StringBuilder builder = new StringBuilder();
			builder.append("package ").append(packageName).append(".service.inter;\n\n");
			builder.append("import java.util.List;\n");
			builder.append("import ").append(packageName).append(".entity.").append(entityName).append(";\n");
			if (!StringUtil.isEmpty(queryFullName)) {
				builder.append("import ").append(queryFullName).append(";\n");
			}
			if (!StringUtil.isEmpty(responseResultFullName)) {
				builder.append("import ").append(responseResultFullName).append(";\n\n");
			}
			if (needList && !StringUtil.isEmpty(listResponseResultFullName) && !responseResultFullName.equals(listResponseResultFullName)) {
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
				camelKeyName = StringUtil.underscoreCaseToCamelCase(keyName);
				if (keyName.endsWith("id")) {
					idName = "id";
				} else {
					idName = camelKeyName;
				}
				byWhat = StringUtil.toTitleCase(idName);
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
					String camelActive = StringUtil.underscoreCaseToCamelCase(active);
					String titleActive = StringUtil.toTitleCase(camelActive);
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
			
			String fileName = parentPath + "/service/inter/" + serviceName + ".java";
			FileUtil.mkdirIfNotExists(parentPath + "/service/inter");
			FileUtil.writeTextToFile(fileName, builder.toString());
		}
	}
	
	/**
	 * 生成service文件
	 */
	private void generateService(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String entityVarName = StringUtil.classNameToVarName(entityName);
			String iServiceName = "I" + entityName + "Service";
			String serviceName = entityName + "ServiceImpl";
			String mapperName = entityName + "Mapper";
			String mapperVarName = StringUtil.classNameToVarName(mapperName);

			List<Table> slaves = table.getAllSlaves();
			boolean hasSlave = slaves != null && !slaves.isEmpty();
			
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
				camelKeyName = StringUtil.underscoreCaseToCamelCase(keyName);
				titleKeyName = StringUtil.toTitleCase(camelKeyName);
				if (keyName.endsWith("id")) {
					idName = "id";
				} else {
					idName = camelKeyName;
				}
				byWhat = StringUtil.toTitleCase(idName);
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
            IPaginatorStrategy strategy = PaginatorHandler.getStrategy(paginator);
            if (strategy != null) {
                builder.append(strategy.getServiceImport());
            }
			builder.append("import ").append(packageName).append(".service.inter.").append(iServiceName).append(";\n");
			builder.append("import ").append(packageName).append('.').append(mapperPackageName).append('.').append(entityName).append("Mapper;\n");
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
							.append('(').append("entity.get").append(StringUtil.toTitleCase(camelKeyName)).append("())").append(") {\n");
				} else {
					builder.append("\t\tif (").append("entity.get").append(StringUtil.toTitleCase(camelKeyName)).append("() == null").append(") {\n");
				}
				if (hasSlave) {
					if ("String".equals(keyType)) {
						builder.append("\t\t\tentity.set").append(StringUtil.toTitleCase(camelKeyName)).append('(').append(getIdUtil).append('.')
								.append(getIdMethod).append('(').append(String.join(", ", getIdParamArr)).append("));\n");
					}
					if (!"".equals(createdDate)) {
						builder.append("\t\t\tentity.set").append(StringUtil.toTitleCase(createdDate)).append("(date);\n");
					}
					if (!"".equals(createdBy)) {
						builder.append("\t\t\t//TODO entity.set").append(StringUtil.toTitleCase(createdBy)).append("(by whom);\n");
					}
					if (!"".equals(active)) {
						builder.append("\t\t\tentity.set").append(StringUtil.toTitleCase(active)).append("(").append(activeCode).append(");\n");
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
						builder.append("\t\t\tentity.set").append(StringUtil.toTitleCase(updatedDate)).append("(date);\n");
					}
					if (!"".equals(updatedBy)) {
						builder.append("\t\t\t//TODO entity.set").append(StringUtil.toTitleCase(updatedBy)).append("(by whom);\n");
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
						String listName = StringUtil.classNameToVarName(slaveEntityName) + 's';
						builder.append("\t\tList<").append(slaveEntityName).append("> ").append(listName)
								.append(" = entity.get").append(slaveEntityName).append("s();\n");
						builder.append("\t\tif (!CollectionUtils.isEmpty(").append(listName).append(")) {\n");
						builder.append("\t\t\tfor (").append(slaveEntityName).append(" line : ").append(listName).append(") {\n");
						builder.append("\t\t\t\t//TODO setId()\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(camelKeyName)).append("(entity.get")
								.append(StringUtil.toTitleCase(camelKeyName)).append("());\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(createdDate)).append("(date);\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(updatedDate)).append("(date);\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(createdBy))
								.append("(entity.get").append(StringUtil.toTitleCase(createdBy)).append("());\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(updatedBy))
								.append("(entity.get").append(StringUtil.toTitleCase(updatedBy)).append("());\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(active)).append('(').append(activeCode).append(");\n");
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
					builder.append("\t\tentity.set").append(StringUtil.toTitleCase(camelKeyName)).append('(').append(getIdUtil).append('.')
							.append(getIdMethod).append('(').append(String.join(", ", getIdParamArr)).append("));\n");
				}
				if (!"".equals(createdDate)) {
					builder.append("\t\tDate date = new Date();\n");
					builder.append("\t\tentity.set").append(StringUtil.toTitleCase(createdDate)).append("(date);\n");
				}
				if (!"".equals(createdBy)) {
					builder.append("\t\t//TODO entity.set").append(StringUtil.toTitleCase(createdBy)).append("(by whom);\n");
				}
				if (!"".equals(active)) {
					builder.append("\t\tentity.set").append(StringUtil.toTitleCase(active)).append("(").append(activeCode).append(");\n");
				}
				builder.append("\t\tif (").append(mapperVarName).append(".add(entity) < 1) {\n");
				builder.append("\t\t\treturn new ").append(rrDiamondName).append("(")
						.append(errorCode).append(", \"保存失败\");\n");
				builder.append("\t\t}\n\n");
				if (hasSlave) {
					for (Table slave : slaves) {
						String slaveTableName = slave.getTableName();
						String slaveEntityName = getEntityName(slaveTableName);

						String listName = StringUtil.classNameToVarName(slaveEntityName) + 's';
						builder.append("\t\tList<").append(slaveEntityName).append("> ").append(listName)
								.append(" = entity.get").append(slaveEntityName).append("s();\n");
						builder.append("\t\tif (!CollectionUtils.isEmpty(").append(listName).append(")) {\n");
						builder.append("\t\t\tfor (").append(slaveEntityName).append(" line : ").append(listName).append(") {\n");
						builder.append("\t\t\t\t//TODO setId()\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(camelKeyName)).append("(entity.get")
								.append(StringUtil.toTitleCase(camelKeyName)).append("());\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(createdDate)).append("(date);\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(updatedDate)).append("(date);\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(createdBy))
								.append("(entity.get").append(StringUtil.toTitleCase(createdBy)).append("());\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(updatedBy))
								.append("(entity.get").append(StringUtil.toTitleCase(updatedBy)).append("());\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(active)).append('(').append(activeCode).append(");\n");
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
						.append(StringUtil.toTitleCase(camelKeyName)).append("();\n");
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
					builder.append("\t\tentity.set").append(StringUtil.toTitleCase(updatedDate)).append("(date);\n");
				}
				if (!"".equals(updatedBy)) {
					builder.append("\t\t//TODO entity.set").append(StringUtil.toTitleCase(updatedBy)).append("(by whom);\n");
				}
				if (hasSlave) {
					builder.append('\n');
					for (Table slave : slaves) {
						String slaveTableName = slave.getTableName();
						String slaveEntityName = getEntityName(slaveTableName);
						builder.append("\t\t").append(mapperVarName).append(".deleteAll").append(slaveEntityName).append('(')
								.append(camelKeyName).append(");\n");
						String listName = StringUtil.classNameToVarName(slaveEntityName) + 's';
						builder.append("\t\tList<").append(slaveEntityName).append("> ").append(listName)
								.append(" = entity.get").append(slaveEntityName).append("s();\n");
						builder.append("\t\tif (!CollectionUtils.isEmpty(").append(listName).append(")) {\n");
						builder.append("\t\t\tfor (").append(slaveEntityName).append(" line : ").append(listName).append(") {\n");
						builder.append("\t\t\t\t//TODO setId()\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(camelKeyName)).append("(entity.get")
								.append(StringUtil.toTitleCase(camelKeyName)).append("());\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(createdDate)).append("(date);\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(updatedDate)).append("(date);\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(createdBy))
								.append("(entity.get").append(StringUtil.toTitleCase(createdBy)).append("());\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(updatedBy))
								.append("(entity.get").append(StringUtil.toTitleCase(updatedBy)).append("());\n");
						builder.append("\t\t\t\tline.set").append(StringUtil.toTitleCase(active)).append('(').append(activeCode).append(");\n");
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
							.append(".get").append(StringUtil.toTitleCase(orderField)).append("())) {\n");
					builder.append("\t\t\t").append(queryVarName).append(".set").append(StringUtil.toTitleCase(orderField))
							.append("(\"").append(orderFieldVal).append("\");\n");
					orderTypeVal = (orderTypeVal != null && !orderTypeVal.trim().equals("")) ? orderTypeVal.toUpperCase() : "ASC";
					builder.append("\t\t\t").append(queryVarName).append(".set").append(StringUtil.toTitleCase(orderType))
							.append("(\"").append(orderTypeVal).append("\");\n");
					builder.append("\t\t}\n");
				}

				String titleTotal = StringUtil.toTitleCase(total);
				String titlePages = StringUtil.toTitleCase(pages);
				PaginatorHandler.getStrategy(paginator).getServiceListBusinessLogic(
				        builder, queryVarName, entityName, mapperVarName, lrrGenericName, lrrDiamondName,
                        responseResultVarName, listRrData, titleTotal, titlePages
                );
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
					builder.append("\t\t").append(responseResultVarName).append(".set").append(StringUtil.toTitleCase(rrData)).append("(").append(entityVarName).append(");\n");
					builder.append("\t\treturn ").append(responseResultVarName).append(";\n");
					builder.append("\t}\n\n");
				}

				if (active != null && needUpdateActive) {
					String camelActive = StringUtil.underscoreCaseToCamelCase(active);
					String titleActive = StringUtil.toTitleCase(camelActive);
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

					builder.append("\t\tif (CollectionUtils.isEmpty(").append(camelKeyName).append(")) {\n");
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

			String fileName = parentPath + "/service/impl/" + serviceName + ".java";
			FileUtil.mkdirIfNotExists(parentPath + "/service/impl");
			FileUtil.writeTextToFile(fileName, builder.toString());
		}
	}
	
	/**
	 * 生成controller文件
	 */
	private void generateController(String parentPath, String packageName) throws IOException {
		String classNamePrefix = urlPrefix.replaceAll("/", "");
		String classNameSuffix = urlSuffix.replaceAll("/", "");
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String controllerName = String.format("%s%s%sController",
						StringUtil.urlToTitleCamelCase(classNamePrefix),
						entityName,
						StringUtil.urlToTitleCamelCase(classNameSuffix));
			String serviceName = entityName + "Service";
			String serviceVarName = StringUtil.classNameToVarName(serviceName);

			List<Table> slaves = table.getAllSlaves();
			boolean hasSlave = slaves != null && !slaves.isEmpty();
			
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
			builder.append("import ").append(queryFullName).append(";\n");
			if (!StringUtil.isEmpty(baseController)) {
				builder.append("import ").append(baseController).append(";\n");
			}
			builder.append('\n');
			
			builder.append("@RestController\n");
			builder.append("@RequestMapping(\"");
			if (!StringUtil.isEmpty(urlPrefix)) {
				if (!urlPrefix.startsWith("/")) {
					builder.append('/');
				}
				builder.append(urlPrefix);
			}
			if (builder.charAt(builder.length() - 1) != '/') {
				builder.append('/');
			}
			builder.append(StringUtil.classNameToVarName(entityName));
			if (!StringUtil.isEmpty(classNameSuffix)) {
				if (!urlSuffix.startsWith("/")) {
					builder.append('/');
				}
				builder.append(urlSuffix);
				if (builder.charAt(builder.length() - 1) == '/') {
					builder.deleteCharAt(builder.length() - 1);
				}
			}
			builder.append("\")\n");
			
			builder.append("public class ").append(controllerName);
			if (!StringUtil.isEmpty(baseControllerShortName)) {
				builder.append(" extends ").append(baseControllerShortName);
			}
			builder.append(" {\n\n");
			
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
				camelKeyName = StringUtil.underscoreCaseToCamelCase(keyName);
				if (keyName.endsWith("id")) {
					idName = "id";
				} else {
					idName = camelKeyName;
				}
				byWhat = StringUtil.toTitleCase(idName);
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
					String camelActive = StringUtil.underscoreCaseToCamelCase(active);
					String titleActive = StringUtil.toTitleCase(camelActive);
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
			
			String fileName = parentPath + "/controller/" + controllerName + ".java";
			FileUtil.mkdirIfNotExists(parentPath + "/controller");
			FileUtil.writeTextToFile(fileName, builder.toString());
		}
	}
	
}
