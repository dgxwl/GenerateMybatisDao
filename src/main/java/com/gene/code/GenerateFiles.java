package com.gene.code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 生成代码文件
 * @author Administrator
 *
 */
public class GenerateFiles {
	
	private static List<Table> tables = TableHandler.getTables();
	private static List<String> entityNames = TableHandler.getEntityNames();
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
		fullNameTypeMap.put("BLOB", "java.lang.Byte[]");
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

	private static String queryFullName;
	private static String queryName;
	private static String queryVarName;
	private static String responseResultFullName;
	private static String responseResultName;
	private static String responseResultVarName;
	private static String rrData;
	private static String rrGenericStr;
	private static boolean rrGeneric;
	private static String responseOk;
	private static String responseError;
	private static String whereStr;
	private static String pages;
	private static String total;
	private static String paginator;
	static {
		try (InputStream in = DBUtils.class.getClassLoader().getResourceAsStream("db.properties")) {
			Properties prop = new Properties();
			prop.load(in);

			queryFullName = prop.getProperty("query");
			responseResultFullName = prop.getProperty("response_result");
			rrData = prop.getProperty("rr_data");
			rrGenericStr = prop.getProperty("rr_generic");
			whereStr = prop.getProperty("where_str");
			responseOk = prop.getProperty("response_ok");
			responseError = prop.getProperty("response_error");
			pages = prop.getProperty("pages");
			total = prop.getProperty("total");
			paginator = prop.getProperty("paginator");
		} catch (Exception e) {
			queryFullName = "com.middle.last.domain.MyQuery";
			responseResultFullName = "com.middle.last.domain.ResponseResult";
			rrData = "data";
			rrGenericStr = "false";
			whereStr = "WHERE ${@com.middle.last.domain.MyQuery@getWhereStr(myQuery.filters)}";
			responseOk = "1";
			responseError = "-100";
			pages = "pages";
			total = "total";
			paginator = "pageHelper";
		}
		queryName = queryFullName.substring(queryFullName.lastIndexOf('.') + 1);
		queryVarName = toContentCase(queryName);
		responseResultName = responseResultFullName.substring(responseResultFullName.lastIndexOf('.') + 1);
		responseResultVarName = getVarNameByCamelCase(responseResultName);
		rrGeneric = "true".equals(rrGenericStr);
	}

	public static void main(String[] args) {
		try {
			Properties config = DBUtils.getConfigs();
			String path = config.getProperty("path");
			String packageName = config.getProperty("package");
			generate(path, packageName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void generate(String path, String packageName) throws IOException {
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
		
		generateEntity(finalParentPath, packageName);
		generateXmlMappers(xmlPath, packageName);
		generateMapper(finalParentPath, packageName);
		generateIService(finalParentPath, packageName);
		generateService(finalParentPath, packageName);
		generateController(finalParentPath, packageName);
	}
	
	private static List<Map<String, String>> resultMaps = new ArrayList<>();
	/**
	 * 生成entity实体类文件
	 */
	private static void generateEntity(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			Map<String, String> resultMap = new HashMap<>();
			resultMaps.add(resultMap);
			
			String entityName;
			entityName = getEntityName(tableName);

			List<Column> fields = table.getAllColumns();
			
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
				sbBeforeClass.append("import lombok.Data;\n");
				
				StringBuilder sbAfterClass = new StringBuilder();
				sbAfterClass.append("@Data\n");
				sbAfterClass.append("public class ").append(entityName).append(" {\n");
				for (Column column : fields) {
					String fieldName;
					//下划线字段名转驼峰命名
					String[] data = column.getColumnName().split("[_]+");
					if (data.length > 1) {
						StringBuilder sb = new StringBuilder();
						sb.append(data[0]);
						for (int i = 1; i < data.length; i++) {
							data[i] = toTitleCase(data[i]);
							sb.append(data[i]);
						}
						fieldName = sb.toString();
						
						resultMap.put(column.getColumnName(), fieldName);
					} else {
						fieldName = column.getColumnName();
					}
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

	private static String getEntityName(String tableName) {
		String entityName;
		String lastPart;
		if (tableName.indexOf('_') != -1) {
			lastPart = tableName.substring(tableName.lastIndexOf('_') + 1);
			if (Collections.frequency(entityNames, lastPart) > 1) {
				String[] data = tableName.split("_");
				if (data.length > 2) {
					entityName = toTitleCase(data[data.length - 2]) + toTitleCase(data[data.length - 1]);
				} else {
					entityName = toTitleCase(data[data.length - 1]);
				}
			} else {
				entityName = toTitleCase(lastPart);
			}
		} else {
			entityName = toTitleCase(tableName);
		}
		return entityName;
	}

	/**
	 * 生成mapper接口文件
	 */
	private static void generateMapper(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String mapperName = entityName + "Mapper";

			StringBuilder builder = new StringBuilder();
			builder.append("package ").append(packageName).append(".mapper;\n\n");

			builder.append("import java.util.List;\n");
			builder.append("import org.apache.ibatis.annotations.Param;\n");
			builder.append("import ").append(packageName).append(".entity.").append(entityName).append(";\n");
			builder.append("import ").append(queryFullName).append(";\n\n");
			if ("mybatis-paginator".equals(paginator)) {
				builder.append(PaginatorHandler.getImports(paginator));
			}

			builder.append("public interface ").append(mapperName).append(" {\n\n");

			builder.append("\tInteger add(").append(entityName).append(" entity);\n\n");

			builder.append(table.getAllPrimaryKeys().size() > 0 ? "\tInteger " + "update(" + entityName + " entity);\n\n" : "");

			switch (paginator) {
				case "pageHelper":
					builder.append("\tList<").append(entityName).append("> list(@Param(\"").append(queryVarName)
							.append("\") ").append(queryName).append(" ").append(queryVarName).append(");\n\n");
				case "mybatis-paginator":
					builder.append("\tList<").append(entityName).append("> list(@Param(\"").append(queryVarName)
							.append("\") ").append(queryName).append(" ").append(queryVarName)
							.append(", @Param(\"pageBounds\") PageBounds pageBounds);\n\n");
			}

			List<PrimaryKey> keys = table.getAllPrimaryKeys();
			if (keys.size() == 1) {
				PrimaryKey key = keys.get(0);
				String keyName = key.getPkName();
				if (keyName.endsWith("id")) {
					key.setPkName("id");
				}
			}
			for (PrimaryKey key : keys) {
				String keyName = key.getPkName();
				String keyType = typeMap.get(key.getPkType());
				String byWhat = toTitleCase(keyName);
				builder.append("\t").append(entityName).append(" getBy")
						.append(byWhat).append("(").append(keyType).append(" ").append(keyName).append(");\n\n");
				builder.append("\tInteger deleteBy")
						.append(byWhat).append("(").append(keyType).append(" ").append(keyName).append(");\n\n");
				builder.append("\tInteger batchDeleteBy")
						.append(byWhat).append("s(@Param(\"list\") List<").append(keyType)
						.append("> ").append(keyName).append("s);\n\n");
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
	private static void generateXmlMappers(String parentPath, String packageName) throws IOException {
		int index = 0;
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String mapperName = entityName + "Mapper";
			
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
									new FileOutputStream(fileName), StandardCharsets.UTF_8));
			) {
				StringBuilder builder = new StringBuilder();
				builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				builder.append("<!DOCTYPE mapper\n");
				builder.append("  PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n");
				builder.append("  \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n");
				
				builder.append("<mapper namespace=\"").append(packageName).append(".mapper.")
						.append(entityName).append("Mapper\">\n\n");
				
				//暂时不写add, 需要获取自增主键再继续
				
				StringBuilder builderGetByPk = new StringBuilder();
				String pkColumnName = null;
				boolean pkIsIncrement = false;
				//delete需要获取所有主键, 在这完成后到最后拼接
				StringBuilder builderDelete = new StringBuilder();
				StringBuilder builderBatchDel = new StringBuilder();
				List<PrimaryKey> keys = table.getAllPrimaryKeys();
				for (PrimaryKey key : keys) {
					//搞掂add的主键自增
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());

					pkColumnName = keyName;
					if (keyType.equals("Integer")) {
						pkIsIncrement = true;
					}
					
					//用builderGetByPk搞掂根据主键查询的各方法, 出了循环拼回原builder
					String byWhat = toTitleCase(keyName);
					builderGetByPk.append("\t<select id=\"getBy").append(byWhat)
							.append("\" resultType=\"").append(packageName).append(".entity.").append(entityName)
							.append("\" resultMap=\"mapping\">\n");
					builderGetByPk.append("\t\tSELECT *\n");
					builderGetByPk.append("\t\tFROM ").append(tableName).append("\n");
					builderGetByPk.append("\t\tWHERE ").append(keyName).append("=#{").append(keyName).append("}\n");
					builderGetByPk.append("\t</select>\n\n");
					
					//各款delete
					builderDelete.append("\t<delete id=\"deleteBy").append(byWhat).append("\">\n");
					builderDelete.append("\t\tDELETE FROM ").append(tableName).append("\n");
					builderDelete.append("\t\tWHERE ").append(keyName).append("=#{").append(keyName).append("}\n");
					builderDelete.append("\t</delete>\n\n");
					//各款批量delete
					builderBatchDel.append("\t<delete id=\"batchDeleteBy").append(byWhat).append("s\">\n");
					builderBatchDel.append("\t\tDELETE FROM ").append(tableName).append("\n");
					builderBatchDel.append("\t\tWHERE ").append(keyName).append(" in <foreach collection=\"list\" open=\"(\" separator=\",\" close=\")\" item=\"item\" index=\"index\">#{item}</foreach>\n");
					builderBatchDel.append("\t</delete>\n\n");
				}
				
				//resultMap
				builder.append("\t<resultMap id=\"mapping\" type=\"")
						.append(packageName).append(".entity.").append(entityName).append("\">\n");
				Map<String, String> resultMap = resultMaps.get(index);
				if (resultMap.size() > 0) {
					for (Map.Entry<String, String> entry : resultMaps.get(index).entrySet()) {
						String columnName = entry.getKey();
						String fieldName = entry.getValue();
						
						if (columnName.equals(pkColumnName) && pkIsIncrement) {
							builder.append("\t\t<id column=\"").append(columnName).append("\" property=\"").append(fieldName)
									.append("\" javaType=\"java.lang.Integer\" />\n");
						} else {
							builder.append("\t\t<result column=\"").append(columnName).append("\" property=\"")
									.append(fieldName).append("\" javaType=\"")
									.append(fullNameTypeMap.get(table.getTypeByColumnName(columnName))).append("\" />\n");
						}
					}
				}
				index++;
				builder.append("\t</resultMap>\n\n");
				
				//这里开始完成add
				builder.append("\t<insert id=\"add\"")
			.append((pkIsIncrement ? " useGeneratedKeys=\"true\" keyProperty=\"" + pkColumnName + "\"" : ""))
						.append(" parameterType=\"").append(packageName).append(".entity.").append(entityName).append("\">\n");
				builder.append("\t\tINSERT INTO ").append(tableName).append(" (\n");
				//需要遍历所有字段, 中断add
				
				//UPDATE
				StringBuilder builderUpdate = new StringBuilder();
				if (pkColumnName != null) {
					builderUpdate.append("\t<update id=\"update\" parameterType=\"")
							.append(packageName).append(".entity.").append(entityName).append("\">\n");
					builderUpdate.append("\t\tUPDATE ").append(tableName).append("\n");
					builderUpdate.append("\t\t<set>\n");
				}
				
				StringBuilder builderAfterValues = new StringBuilder();  //保存 ) VALUES ( 后面的插入字段
				int i = 1;
				for (Column c : table.getAllColumns()) {
					String columnName = c.getColumnName();
					String fieldName = resultMap.get(columnName) != null ? resultMap.get(columnName) : columnName;
					//搞掂add的插入字段
					if (!columnName.equals(pkColumnName)) {
						builder.append("\t\t\t").append(columnName).append(",\n");
						builderAfterValues.append("\t\t\t#{").append(fieldName).append("},\n");
					}
					
					//搞掂update
					if (pkColumnName != null && !columnName.equals(pkColumnName)) {
						builderUpdate.append("\t\t<if test=\"").append(fieldName).append(" != null\">\n");
						builderUpdate.append("\t\t\t").append(columnName).append(" = #{").append(fieldName).append("}");
						if (i == table.getAllColumns().size()) {
							builderUpdate.append("\n");
						} else {
							builderUpdate.append(",\n");
						}
						builderUpdate.append("\t\t</if>\n");
					}
					i++;
				}
				if (pkColumnName != null) {
					builderUpdate.append("\t\t</set>\n");
					builderUpdate.append("\t\tWHERE ").append(pkColumnName).append("=#{").append(pkColumnName).append("}\n");
					builderUpdate.append("\t</update>\n\n");
				}
				
				//删去最后一轮循环多出的",\n"
				builder.deleteCharAt(builder.length() - 1);
				builder.deleteCharAt(builder.length() - 1);
				builder.append("\n");
				builderAfterValues.deleteCharAt(builderAfterValues.length() - 1);
				builderAfterValues.deleteCharAt(builderAfterValues.length() - 1);
				builderAfterValues.append("\n");
				//继续add
				builder.append("\t\t) VALUES (\n");
				//把builderAfterValues拼回builder
				builder.append(builderAfterValues);
				builder.append("\t\t)\n");
				builder.append("\t</insert>\n\n");  //完成add!
				//把builderUpdate拼回builder
				builder.append(builderUpdate);
				//把builderGetByPk拼回builder
				builder.append(builderGetByPk);

				//list
				builder.append("\t<select id=\"list\" resultType=\"").append(packageName)
						.append(".entity.").append(entityName).append("\" resultMap=\"mapping\">\n");
				builder.append("\t\tSELECT *\n");
				builder.append("\t\tFROM ").append(tableName).append("\n");
				builder.append("\t\t").append(whereStr).append("\n");
				builder.append("\t\tORDER BY ${").append(queryVarName).append(".orderField} ${")
						.append(queryVarName).append(".orderType}\n");
				builder.append("\t</select>\n\n");
				
				//getAll
				builder.append("\t<select id=\"getAll\" resultType=\"").append(packageName)
						.append(".entity.").append(entityName).append("\" resultMap=\"mapping\">\n");
				builder.append("\t\tSELECT *\n");
				builder.append("\t\tFROM ").append(tableName).append("\n");
				builder.append("\t</select>\n\n");
				
				//delete
				builder.append(builderDelete);
				//batchDel
				builder.append(builderBatchDel);

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
	private static void generateIService(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String serviceName = entityName + "Service";
			
			File f = new File(parentPath + "/service/inter");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/service/inter/I" + serviceName + ".java";
			File service = new File(fileName);
			service.createNewFile();
			try (
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fileName), StandardCharsets.UTF_8))
			) {
				StringBuilder builder = new StringBuilder();
				builder.append("package ").append(packageName).append(".service;\n\n");
				builder.append("import java.util.List;\n");
				builder.append("import ").append(packageName).append(".entity.").append(entityName).append(";\n");
				builder.append("import ").append(queryFullName).append(";\n");
				builder.append("import ").append(responseResultFullName).append(";\n\n");
				builder.append("public interface I").append(serviceName).append(" {\n\n");

				String rrGenericVar = "";
				if (rrGeneric) {
					rrGenericVar = "<" + entityName + ">";
				}

				builder.append("\t").append(responseResultName).append(rrGenericVar).append(" add(").append(entityName).append(" entity);\n\n");
				List<PrimaryKey> keys = table.getAllPrimaryKeys();
				for (PrimaryKey key : keys) {
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					String byWhat = toTitleCase(keyName);
					builder.append("\t").append(responseResultName).append(rrGenericVar).append(" deleteBy").append(byWhat).append("(").append(keyType).append(" ").append(keyName).append(");\n\n");
					builder.append("\t").append(responseResultName).append(rrGenericVar).append(" batchDeleteBy").append(byWhat).append("s(List<").append(keyType).append("> ").append(keyName).append("s);\n\n");
				}
				for (PrimaryKey key : keys) {
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					String byWhat = toTitleCase(keyName);
					builder.append("\t").append(responseResultName).append(rrGenericVar).append(" getBy")
							.append(byWhat).append("(").append(keyType).append(" ").append(keyName).append(");\n\n");
				}
				builder.append("\t").append(responseResultName).append(rrGenericVar).append(" list(").append(queryName)
						.append(" ").append(queryVarName).append(");\n\n");
				builder.append(!table.getAllPrimaryKeys().isEmpty() ? "\t" + responseResultName + rrGenericVar + " update(" + entityName + " entity);\n\n" : "");
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
	private static void generateService(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String entityVarName = toContentCase(entityName);
			String serviceName = entityName + "ServiceImpl";
			String mapperName = entityName + "Mapper";
			String mapperVarName = toContentCase(mapperName);
			
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
				StringBuilder builder = new StringBuilder();
				builder.append("package ").append(packageName).append(".service;\n\n");
				
				builder.append("import java.util.List;\n");
				builder.append("import org.springframework.stereotype.Service;\n");
				builder.append("import org.springframework.beans.factory.annotation.Autowired;\n");
				builder.append("import org.springframework.transaction.annotation.Transactional;\n");
				builder.append(PaginatorHandler.getImports(paginator));
				builder.append("import ").append(packageName).append(".mapper.").append(entityName).append("Mapper;\n");
				builder.append("import ").append(packageName).append(".entity.").append(entityName).append(";\n");
				builder.append("import ").append(packageName).append(".util.StringUtils;\n");
				builder.append("import ").append(responseResultFullName).append(";\n");
				builder.append("import ").append(queryFullName).append(";\n\n");
				
				builder.append("@Service\n");
				builder.append("public class ").append(serviceName).append(" implements I").append(serviceName).append(" {\n\n");

				String rrGenericVar = "";
				if (rrGeneric) {
					rrGenericVar = "<" + entityName + ">";
				}

				builder.append("\t@Autowired\n");
				builder.append("\tprivate ").append(mapperName).append(" ").append(mapperVarName).append(";\n\n");
				
				builder.append("\t@Override\n");
				builder.append("\tpublic ").append(responseResultName).append(rrGenericVar).append(" add(").append(entityName).append(" entity) {\n");
				builder.append("\t\tif (").append(mapperVarName).append(".add(entity) < 1) {\n");
				builder.append("\t\t\treturn new ").append(responseResultName).append(rrGeneric?"<>":"").append("(")
						.append(responseError).append(", \"保存失败\");\n");
				builder.append("\t\t}\n");
				builder.append("\t\treturn new ").append(responseResultName).append(rrGeneric?"<>":"").append("(\"保存成功\");\n");
				builder.append("\t}\n\n");
				
				StringBuilder builderGetBy = new StringBuilder();
				String autoIncrementId = null;
				String otherTypePk = null;
				List<PrimaryKey> keys = table.getAllPrimaryKeys();
				for (PrimaryKey key : keys) {
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					if (keyType.equals("Integer")) {
						autoIncrementId = keyName;
					} else if (keyType.equals("String")) {
						otherTypePk = keyName;
					}

					String byWhat = toTitleCase(keyName);
					builder.append("\t@Override\n");
					builder.append("\tpublic ").append(responseResultName).append(rrGenericVar).append(" deleteBy")
							.append(byWhat).append("(").append(keyType).append(" ").append(keyName).append(") {\n");
					builder.append("\t\tif (").append(mapperVarName).append(".deleteBy").append(byWhat).append("(").append(keyName).append(") < 1) {\n");
					builder.append("\t\t\treturn new ").append(responseResultName).append(rrGeneric?"<>":"").append("(")
							.append(responseError).append(", \"删除失败，该记录不存在\");\n");
					builder.append("\t\t}\n");
					builder.append("\t\treturn new ").append(responseResultName).append(rrGeneric?"<>":"").append("(\"删除成功\");\n");
					builder.append("\t}\n\n");

					builder.append("\t@Transactional\n");
					builder.append("\t@Override\n");
					builder.append("\tpublic ").append(responseResultName).append(rrGenericVar).append(" batchDeleteBy").append(byWhat)
							.append("s(List<").append(keyType).append("> ").append(keyName).append("s) {\n");

					builder.append("\t\tif (").append(keyName).append("s == null || ").append(keyName).append("s.isEmpty()) {\n");
					builder.append("\t\t\treturn new ").append(responseResultName).append("(").append(responseError).append(", \"请选择待删除的记录\");\n");
					builder.append("\t\t}\n");
					builder.append("\t\tif (").append(mapperVarName).append(".batchDeleteBy").append(byWhat).append("s(").append(keyName).append("s) != ").append(keyName).append("s.size()) {\n");
					builder.append("\t\t\tthrow new IllegalStateException(\"批量删除失败\");\n");
					builder.append("\t\t}\n");
					builder.append("\t\treturn new ").append(responseResultName).append(rrGeneric?"<>":"").append("(\"批量删除成功\");\n");
					builder.append("\t}\n\n");

					builderGetBy.append("\t@Override\n");
					builderGetBy.append("\tpublic ").append(responseResultName).append(rrGenericVar).append(" getBy").append(byWhat).append("(").append(keyType).append(" ").append(keyName).append(") {\n");
					builderGetBy.append("\t\t").append(entityName).append(" ").append(entityVarName).append(" = ").append(mapperVarName).append(".getBy").append(byWhat).append("(").append(keyName).append(");\n");
					builderGetBy.append("\t\tif (").append(entityVarName).append(" == null) {\n");
					builderGetBy.append("\t\t\treturn new ").append(responseResultName).append(rrGeneric?"<>":"").append("(").append(responseError).append(", \"找不到该记录\");\n");
					builderGetBy.append("\t\t}\n");
					builderGetBy.append("\t\t").append(responseResultName).append(" ").append(responseResultVarName).append(" = new ").append(responseResultName).append(rrGeneric?"<>":"").append("();\n");
					builderGetBy.append("\t\t").append(responseResultVarName).append(".set").append(toTitleCase(rrData)).append("(").append(entityVarName).append(");\n");
					builderGetBy.append("\t\treturn ").append(responseResultVarName).append(";\n");
					builderGetBy.append("\t}\n\n");
				}
				
				builder.append(builderGetBy);
				
				builder.append("\t@Override\n");
				builder.append("\tpublic ").append(responseResultName).append(" list(").append(queryName)
						.append(" ").append(queryVarName).append(") {\n");
				if (autoIncrementId != null) {
					builder.append("\t\tif (").append(queryVarName)
							.append(" != null && StringUtils.isNullOrEmpty(").append(queryVarName)
							.append(".getOrderField())) {\n");
					builder.append("\t\t\t").append(queryVarName).append(".setOrderField(\"").append(autoIncrementId).append("\");\n");
					builder.append("\t\t\t").append(queryVarName).append(".setOrderType(\"ASC\");\n");
					builder.append("\t\t}\n");
				} else if (otherTypePk != null) {
					builder.append("\t\tif (").append(queryVarName).append(" != null && ")
							.append(queryVarName).append(".getOrderField() == null) {\n");
					builder.append("\t\t\t").append(queryVarName).append(".setOrderField(").append(otherTypePk).append(");\n");
					builder.append("\t\t\t").append(queryVarName).append(".setOrderType(\"ASC\");\n");
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
						builder.append("\t\t").append(responseResultVarName).append(".").append(toTitleCase(rrData)).append("(list);\n");
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
						builder.append("\t\t").append(responseResultVarName).append(".").append(toTitleCase(rrData)).append("(list);\n");
						builder.append("\t\t").append(responseResultVarName).append(".set").append(titleTotal)
								.append("(list.getPaginator().getTotalCount());\n");
						builder.append("\t\t").append(responseResultVarName).append(".set").append(titlePages)
								.append("(list.getPaginator().getTotalPages());\n");
				}

				builder.append("\t\treturn ").append(responseResultVarName).append(";\n");
				builder.append("\t}\n\n");
				
				if (autoIncrementId != null) {
					builder.append("\t@Override\n");
					builder.append("\tpublic ").append(responseResultName).append(" update(").append(entityName).append(" entity) {\n");
					builder.append("\t\tif (").append(mapperVarName).append(".update(entity) < 1) {\n");
					builder.append("\t\t\treturn new ").append(responseResultName)
							.append("(").append(responseError).append(", \"编辑失败\");\n");
					builder.append("\t\t}\n");
					builder.append("\t\treturn new ").append(responseResultName).append("(\"编辑成功\");\n");
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
	private static void generateController(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = getEntityName(tableName);
			String controllerName = entityName + "Controller";
			String serviceName = entityName + "Service";
			String serviceVarName = toContentCase(serviceName);
			
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
				builder.append("import org.springframework.beans.factory.annotation.Autowired;\n");
				builder.append("import org.slf4j.Logger;\n");
				builder.append("import org.slf4j.LoggerFactory;\n");
				builder.append("import ").append(packageName).append(".service.I").append(serviceName).append(";\n");
				builder.append("import ").append(packageName).append(".entity.").append(entityName).append(";\n");
				builder.append("import com.github.pagehelper.Page;\n");
				builder.append("import ").append(responseResultFullName).append(";\n");
				builder.append("import ").append(queryFullName).append(";\n\n");
				
				builder.append("@RestController\n");
				builder.append("@RequestMapping(\"/").append(entityName.toLowerCase()).append("\")\n");
				builder.append("public class ").append(controllerName).append(" {\n\n");
				
				builder.append("\t@Autowired\n");
				builder.append("\tprivate I").append(serviceName).append(" ").append(serviceVarName).append(";\n\n");

				builder.append("\tprivate Logger logger = LoggerFactory.getLogger(this.getClass());\n\n");

				builder.append("\t@RequestMapping(value = \"/add\", method = RequestMethod.POST)\n");
				builder.append("\tpublic ResponseResult add(@RequestBody ").append(entityName).append(" entity) {\n");
				builder.append("\t\ttry {\n");
				builder.append("\t\t\treturn ").append(serviceVarName).append(".add(entity);\n");
				builder.append("\t\t} catch (Exception e) {\n");
				builder.append("\t\t\tlogger.error(\"\", e);\n");
				builder.append("\t\t\treturn new ").append(responseResultName)
						.append("(").append(responseError).append(", ").append("\"服务器异常\");\n");
				builder.append("\t\t}\n");
				builder.append("\t}\n\n");
				
				builder.append("\t@RequestMapping(\"/list\")\n");
				builder.append("\tpublic ").append(responseResultName).append(" list(@RequestBody ").append(queryName)
						.append(" ").append(queryVarName).append(") {\n");
				builder.append("\t\ttry {\n");
				builder.append("\t\t\treturn ").append(serviceVarName).append(".list(").append(queryVarName).append(");\n");
				builder.append("\t\t} catch (Exception e) {\n");
				builder.append("\t\t\tlogger.error(\"\", e);\n");
				builder.append("\t\t\treturn new ").append(responseResultName)
						.append("(").append(responseError).append(", ").append("\"服务器异常\");\n");
				builder.append("\t\t}\n");
				builder.append("\t}\n\n");
				
				String pkName = null;
				String pkType = null;
				List<PrimaryKey> keys = table.getAllPrimaryKeys();
				for (PrimaryKey key : keys) {
					pkName = key.getPkName();
					pkType = typeMap.get(key.getPkType());
					break;
				}

				if (pkName != null) {
					builder.append("\t@RequestMapping(value = \"/update\", method = RequestMethod.POST)\n");
					builder.append("\tpublic ResponseResult update(@RequestBody ").append(entityName).append(" entity) {\n");
					builder.append("\t\ttry {\n");
					builder.append("\t\t\treturn ").append(serviceVarName).append(".update(entity);\n");
					builder.append("\t\t} catch (Exception e) {\n");
					builder.append("\t\t\tlogger.error(\"\", e);\n");
					builder.append("\t\t\treturn new ").append(responseResultName)
							.append("(").append(responseError).append(", ").append("\"服务器异常\");\n");
					builder.append("\t\t}\n");
					builder.append("\t}\n\n");

					builder.append("\t@RequestMapping(\"/getById\")\n");
					builder.append("\tpublic ResponseResult getById(").append(pkType).append(" ").append(pkName).append(") {\n");
					builder.append("\t\ttry {\n");
					builder.append("\t\t\treturn ").append(serviceVarName).append(".getById(").append(pkName).append(");\n");
					builder.append("\t\t} catch (Exception e) {\n");
					builder.append("\t\t\tlogger.error(\"\", e);\n");
					builder.append("\t\t\treturn new ").append(responseResultName)
							.append("(").append(responseError).append(", ").append("\"服务器异常\");\n");
					builder.append("\t\t}\n");
					builder.append("\t}\n\n");

					builder.append("\t@RequestMapping(value = \"/delete\", method = RequestMethod.POST)\n");
					builder.append("\tpublic ResponseResult delete(").append(pkType).append(" ").append(pkName).append(") {\n");
					builder.append("\t\ttry {\n");
					builder.append("\t\t\treturn ").append(serviceVarName).append(".deleteBy")
							.append(toTitleCase(pkName)).append("(").append(pkName).append(");\n");
					builder.append("\t\t} catch (Exception e) {\n");
					builder.append("\t\t\tlogger.error(\"\", e);\n");
					builder.append("\t\t\treturn new ").append(responseResultName)
							.append("(").append(responseError).append(", ").append("\"服务器异常\");\n");
					builder.append("\t\t}\n");
					builder.append("\t}\n\n");

					builder.append("\t@RequestMapping(value = \"/batchDelete\", method = RequestMethod.POST)\n");
					builder.append("\tpublic ResponseResult batchDelete(").append(pkType).append("[] ").append(pkName).append(") {\n");
					builder.append("\t\ttry {\n");
					builder.append("\t\t\treturn ").append(serviceVarName).append(".batchDeleteBy").append(toTitleCase(pkName)).append("s(Arrays.asList(").append(pkName).append("));\n");
					builder.append("\t\t} catch (IllegalStateException e) {\n");
					builder.append("\t\t\treturn new ").append(responseResultName)
							.append("(").append(responseError).append(", e.getMessage());\n");
					builder.append("\t\t} catch (Exception e) {\n");
					builder.append("\t\t\tlogger.error(\"\", e);\n");
					builder.append("\t\t\treturn new ").append(responseResultName)
							.append("(").append(responseError).append(", ").append("\"服务器异常\");\n");
					builder.append("\t\t}\n");
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
}
