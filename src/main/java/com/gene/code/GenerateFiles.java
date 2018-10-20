package com.gene.code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
		typeMap.put("TEXT", "String");
		typeMap.put("BIT", "Boolean");
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
		
		fullNameTypeMap.put("CHAR", "java.lang.String");
		fullNameTypeMap.put("VARCHAR", "java.lang.String");
		fullNameTypeMap.put("VARCHAR2", "java.lang.String");
		fullNameTypeMap.put("NVARCHAR", "java.lang.String");
		fullNameTypeMap.put("BLOB", "java.lang.Byte[]");
		fullNameTypeMap.put("TEXT", "java.lang.String");
		fullNameTypeMap.put("BIT", "java.lang.Boolean");
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
		
		typeImportMap.put("BigDecimal", "import java.math.BigDecimal;");
		typeImportMap.put("Date", "import java.util.Date;");
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
	
	public static void generate(String path, String packageName) throws IOException {
		File pathFile = new File(path.replaceAll("/{2,}", "/"));
		
		StringBuilder builder = new StringBuilder();
		builder.append(path);
		if (pathFile.exists()) {
			int i = 1;
			do {
				i++;
				if (builder.charAt(builder.length() -2) == '_' && Character.isDigit(builder.charAt(builder.length()-1))) {
					builder.delete(builder.length() -2, builder.length());
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
	 * @throws IOException 
	 */
	private static void generateEntity(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			Map<String, String> resultMap = new HashMap<>();
			resultMaps.add(resultMap);
			
			String entityName = null;
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
				sbBeforeClass.append("package " + packageName + ".entity;\n\n");
				sbBeforeClass.append("import lombok.Data;\n");
				
				StringBuilder sbAfterClass = new StringBuilder();
				sbAfterClass.append("@Data\n");
				sbAfterClass.append("public class " + entityName + " {\n");
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
						if (sbBeforeClass.indexOf(typeImportMap.get(fieldType)) == -1) {
							sbBeforeClass.append(typeImportMap.get(fieldType) + "\n");
						}
						break;
					case "BigDecimal":
						if (sbBeforeClass.indexOf(typeImportMap.get(fieldType)) == -1) {
							sbBeforeClass.append(typeImportMap.get(fieldType) + "\n");
						}
						break;
					}
					boolean hasRemark = column.getRemarks() != null && !column.getRemarks().equals("");
					
					sbAfterClass.append("\tprivate " + fieldType + " " + fieldName + ";"
										+ (hasRemark ? "  //" + column.getRemarks() : "") + "\n");
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
	
	/**
	 * 生成mapper接口文件
	 * @throws IOException 
	 */
	private static void generateMapper(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = null;
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
			String mapperName = entityName + "Mapper";
			
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
									new FileOutputStream(fileName), "utf-8"));
			) {
				StringBuilder builder = new StringBuilder();
				builder.append("package " + packageName + ".mapper;\n\n");
				
				builder.append("import java.util.List;\n");
				builder.append("import org.apache.ibatis.annotations.Param;\n");
				builder.append("import "+ packageName +".entity." + entityName + ";\n");
				builder.append("import "+ packageName +".domain.MyQuery;\n\n");
				
				builder.append("public interface " + mapperName + " {\n\n");
				
				builder.append("\tInteger " + "save(" + entityName +" entity);\n");
				builder.append("\tInteger " + "update(" + entityName + " entity);\n");
				builder.append("\tList<" + entityName + "> " + "findByField("+ entityName +" entity);\n");
				builder.append("\tList<" + entityName + "> " + "findAll();\n");
				builder.append("\tList<" + entityName + "> " + "findByCondition(@Param(\"myQuery\") MyQuery myQuery);\n");
				List<PrimaryKey> keys = table.getAllPrimaryKeys();
				for (PrimaryKey key : keys) {
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					String byWhat = toTitleCase(keyName);
					builder.append("\t" + entityName + " findBy" + byWhat
									+ "(" + keyType + " " + keyName + ");\n");
					builder.append("\tInteger " + "deleteBy" + byWhat
									+ "(" + keyType + " " + keyName + ");\n");
				}
				
				builder.append("\n}\n");
				
				bw.write(builder.toString());
				bw.flush();
			} catch (IOException e) {
				throw new IOException(e);
			}
		}
	}
	
	/**
	 * 生成mapper映射器文件
	 * @throws IOException 
	 */
	private static void generateXmlMappers(String parentPath, String packageName) throws IOException {
		int index = 0;
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = null;
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
									new FileOutputStream(fileName), "utf-8"));
			) {
				StringBuilder builder = new StringBuilder();
				builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				builder.append("<!DOCTYPE mapper\n");
				builder.append("  PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n");
				builder.append("  \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n");
				
				builder.append("<mapper namespace=\"" + packageName + ".mapper." + entityName + "Mapper\">\n\n");
				
				//暂时不写save, 需要获取自增主键再继续
				
				StringBuilder builderFindByPk = new StringBuilder();
				String autoIncrementId = null;
				//delete需要获取所有主键, 在这完成后到最后拼接
				StringBuilder builderDelete = new StringBuilder();
				List<PrimaryKey> keys = table.getAllPrimaryKeys();
				for (PrimaryKey key : keys) {
					//搞掂save的主键自增
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					if (keyType.equals("Integer")) {
						autoIncrementId = keyName;
					}
					
					//用builderFindByPk搞掂根据主键查询的各方法, 出了循环拼回原builder
					String byWhat = toTitleCase(keyName);
					builderFindByPk.append("\t<select id=\"findBy" + byWhat + "\" "
							+ "resultType=\"" + packageName + ".entity." + entityName + "\" resultMap=\"mapping\">\n");
					builderFindByPk.append("\t\tSELECT *\n");
					builderFindByPk.append("\t\tFROM " + tableName + "\n");
					builderFindByPk.append("\t\tWHERE " + keyName + "=#{" + keyName + "}\n");
					builderFindByPk.append("\t</select>\n\n");
					
					//各款delete
					builderDelete.append("\t<delete id=\"deleteBy" + byWhat + "\">\n");
					builderDelete.append("\t\tDELETE FROM " + tableName + "\n");
					builderDelete.append("\t\tWHERE " + keyName + "=#{" + keyName + "}\n");
					builderDelete.append("\t</delete>\n\n");
				}
				
				//resultMap
				builder.append("\t<resultMap id=\"mapping\" type=\""+ packageName + ".entity." + entityName +"\">\n");
				Map<String, String> resultMap = resultMaps.get(index);
				if (resultMap.size() > 0) {
					for (Map.Entry<String, String> entry : resultMaps.get(index).entrySet()) {
						String columnName = entry.getKey();
						String fieldName = entry.getValue();
						
						if (columnName.equals(autoIncrementId)) {
							builder.append("\t\t<id column=\""+ columnName +"\" property=\""
									+ fieldName + "\" javaType=\"java.lang.Integer\" />\n");
						} else {
							builder.append("\t\t<result column=\""+ columnName +"\" property=\""
									+ fieldName + "\" javaType=\"" + fullNameTypeMap.get(table.getTypeByColumnName(columnName)) + "\" />\n");
						}
					}
				}
				index++;
				builder.append("\t</resultMap>\n\n");
				
				//这里开始完成save
				builder.append("\t<insert id=\"save\" useGeneratedKeys=\"true\" keyProperty=\"" + autoIncrementId + "\""
						+ " parameterType=\"" + packageName + ".entity." + entityName + "\">\n");
				builder.append("\t\tINSERT INTO " + tableName + " (\n");
				//需要遍历所有字段, 中断save
				
				//UPDATE
				StringBuilder builderUpdate = new StringBuilder();
				builderUpdate.append("\t<update id=\"update\" parameterType=\"" + packageName + ".entity." + entityName + "\">\n");
				builderUpdate.append("\t\tUPDATE " + tableName + "\n");
				builderUpdate.append("\t\t<set>\n");
				
				StringBuilder builderAfterValues = new StringBuilder();  //保存 ) VALUES ( 后面的插入字段
				int i = 1;
				for (Column c : table.getAllColumns()) {
					String columnName = c.getColumnName();
					String fieldName = resultMap.get(columnName) != null ? resultMap.get(columnName) : columnName;
					//搞掂save的插入字段
					if (!columnName.equals(autoIncrementId)) {
						builder.append("\t\t\t" + columnName + ",\n");
						builderAfterValues.append("\t\t\t#{" + fieldName + "},\n");
					}
					
					//搞掂update
					if (!columnName.equals(autoIncrementId)) {
						builderUpdate.append("\t\t<if test=\"" + fieldName + "!=null and " + fieldName + "!=''\">\n");
						builderUpdate.append("\t\t" + columnName + "=#{" + fieldName + "}");
						if (i == table.getAllColumns().size()) {
							builderUpdate.append("\n");
						} else {
							builderUpdate.append(",\n");
						}
						builderUpdate.append("\t\t</if>\n");
					}
					i++;
				}
				builderUpdate.append("\t\t</set>\n");
				builderUpdate.append("\t\tWHERE " + autoIncrementId + "=#{" + autoIncrementId + "}\n");
				builderUpdate.append("\t</update>\n\n");
				//删去最后一轮循环多出的",\n"
				builder.deleteCharAt(builder.length() - 1);
				builder.deleteCharAt(builder.length() - 1);
				builder.append("\n");
				builderAfterValues.deleteCharAt(builderAfterValues.length() - 1);
				builderAfterValues.deleteCharAt(builderAfterValues.length() - 1);
				builderAfterValues.append("\n");
				//继续save
				builder.append("\t\t) VALUES (\n");
				//把builderAfterValues拼回builder
				builder.append(builderAfterValues);
				builder.append("\t\t)\n");
				builder.append("\t</insert>\n\n");  //完成save!
				//把builderUpdate拼回builder
				builder.append(builderUpdate);
				//把builderFindByPk拼回builder
				builder.append(builderFindByPk);
				
				//findByField
				builder.append("\t<select id=\"" + "findByField" + "\" resultType=\""
								+ packageName + ".entity." + entityName + "\" resultMap=\"mapping\">\n");
				builder.append("\t\tSELECT *\n");
				builder.append("\t\tFROM " + tableName + "\n");
				builder.append("\t\t<where>\n");
				i = 1;
				for (Column c : table.getAllColumns()) {
					String columnName = c.getColumnName();
					String fieldName = resultMap.get(columnName) != null ? resultMap.get(columnName) : columnName;
					if (!columnName.equals(autoIncrementId)) {
						builder.append("\t\t\t<if test=\"" + fieldName + "!=null and " + fieldName + "!=''\">\n");
						builder.append("\t\t\tAND " + columnName + "=#{" + fieldName + "}\n");
						builder.append("\t\t\t</if>\n");
					}
					i++;
				}
				builder.append("\t\t</where>\n");
				builder.append("\t</select>\n\n");
				
				//findByCondition
				builder.append("\t<select id=\"findByCondition\" resultType=\""
								+ packageName + ".entity." + entityName + "\" resultMap=\"mapping\">\n");
				builder.append("\t\tSELECT *\n");
				builder.append("\t\tFROM " + tableName + "\n");
				builder.append("\t\t<where>\n");
				builder.append("\t\t\t<foreach collection=\"myQuery.filters\" separator=\"AND\" item=\"filter\">\n");
				builder.append("\t\t\t\t${filter.column} ${filter.operator} ${filter.value}\n");
				builder.append("\t\t\t</foreach>\n");
				builder.append("\t\t</where>\n");
				builder.append("\t\tORDER BY ${myQuery.orderField} ${myQuery.orderType}\n");
				builder.append("\t</select>\n\n");
				
				//findAll
				builder.append("\t<select id=\"findAll\" resultType=\""
								+ packageName + ".entity." + entityName + "\" resultMap=\"mapping\">\n");
				builder.append("\t\tSELECT *\n");
				builder.append("\t\tFROM " + tableName + "\n");
				builder.append("\t</select>\n\n");
				
				//delete
				builder.append(builderDelete);
				
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
	 * @param parentPath
	 * @param packageName
	 * @throws IOException
	 */
	private static void generateIService(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = null;
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
			String serviceName = entityName + "Service";
			
			File f = new File(parentPath + "/service");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/service/I" + serviceName + ".java";
			File service = new File(fileName);
			service.createNewFile();
			try (
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fileName), "utf-8"));
			) {
				StringBuilder builder = new StringBuilder();
				builder.append("package " + packageName + ".service;\n\n");
				builder.append("import java.util.List;\n");
				builder.append("import " + packageName +".entity." + entityName + ";\n");
				builder.append("import " + packageName +".domain.MyQuery;\n\n");
				builder.append("public interface I" + serviceName + " {\n");
				builder.append("\tInteger save(" + entityName + " entity);\n");
				List<PrimaryKey> keys = table.getAllPrimaryKeys();
				for (PrimaryKey key : keys) {
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					String byWhat = toTitleCase(keyName);
					builder.append("\tInteger deleteBy" + byWhat + "(" + keyType + " " + keyName + ");\n");
				}
				for (PrimaryKey key : keys) {
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					String byWhat = toTitleCase(keyName);
					builder.append("\t" + entityName + " findBy" + byWhat + "(" + keyType + " " + keyName + ");\n");
				}
				builder.append("\tList<" + entityName + "> findByField(" + entityName + " entity, MyQuery myQuery);\n");
				builder.append("\tList<" + entityName + "> findAll();\n");
				builder.append("\tList<" + entityName + "> findByCondition(MyQuery myQuery);\n");
				builder.append("\tInteger update(" + entityName + " entity);\n");
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
	 * @throws IOException 
	 */
	private static void generateService(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = null;
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
			String serviceName = entityName + "Service";
			String mapperName = entityName + "Mapper";
			String mapperVarName = toContentCase(mapperName);
			
			File f = new File(parentPath + "/service");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/service/" + serviceName + ".java";
			File service = new File(fileName);
			service.createNewFile();
			try (
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fileName), "utf-8"));
			) {
				StringBuilder builder = new StringBuilder();
				builder.append("package " + packageName + ".service;\n\n");
				
				builder.append("import java.util.List;\n");
				builder.append("import org.springframework.stereotype.Service;\n");
				builder.append("import org.springframework.beans.factory.annotation.Autowired;\n");
				builder.append("import com.github.pagehelper.PageHelper;\n");
				builder.append("import " + packageName +".mapper." + entityName + "Mapper;\n");
				builder.append("import " + packageName +".entity." + entityName + ";\n");
				builder.append("import " + packageName +".util.StringUtils;\n");
				builder.append("import " + packageName +".domain.MyQuery;\n\n");
				
				builder.append("@Service\n");
				builder.append("public class " + serviceName + " implements " + "I" + serviceName + " {\n\n");
				
				builder.append("\t@Autowired\n");
				builder.append("\tprivate " + mapperName + " " + mapperVarName + ";\n\n");
				
				builder.append("\tpublic Integer save(" + entityName +" entity) {\n");
				builder.append("\t\treturn " + mapperVarName + ".save(entity);\n");
				builder.append("\t}\n\n");
				
				StringBuilder builderFindBy = new StringBuilder();
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
					builder.append("\tpublic Integer " + "deleteBy" + byWhat + "(" + keyType + " " + keyName + ") { \n");
					builder.append("\t\treturn " + mapperVarName + ".deleteBy" + byWhat + "(" + keyName + ");\n");
					builder.append("\t}\n\n");
					
					builderFindBy.append("\tpublic " + entityName + " findBy" + byWhat + "(" + keyType + " " + keyName + ") {\n");
					builderFindBy.append("\t\treturn " + mapperVarName + ".findBy" + byWhat + "(" + keyName + ");\n");
					builderFindBy.append("\t}\n\n");
				}
				
				builder.append(builderFindBy);
				
				builder.append("\tpublic List<" + entityName + "> findByField(" + entityName + " entity, MyQuery myQuery) {\n");
				if (autoIncrementId != null) {
					builder.append("\t\tif (myQuery != null && StringUtils.isNullOrEmpty(myQuery.getOrderField())) {\n");
					builder.append("\t\t\tmyQuery.setOrderField(\"" + autoIncrementId + "\");\n");
					builder.append("\t\t\tmyQuery.setOrderType(\"ASC\");\n");
					builder.append("\t\t}\n");
				} else if (otherTypePk != null) {
					builder.append("\t\tif (myQuery != null && myQuery.getOrderField() == null) {\n");
					builder.append("\t\t\tmyQuery.setOrderField(" + otherTypePk + ");\n");
					builder.append("\t\t\tmyQuery.setOrderType(\"ASC\");\n");
					builder.append("\t\t}\n");
				}
				builder.append("\t\tPageHelper.startPage(myQuery.getPage(), myQuery.getLimit(), true);\n");
				builder.append("\t\treturn " + mapperVarName + ".findByField(entity);\n");
				builder.append("\t}\n\n");

				builder.append("\tpublic List<" + entityName + "> findAll() {\n");
				builder.append("\t\treturn " + mapperVarName + ".findAll();\n");
				builder.append("\t}\n\n");
				
				builder.append("\tpublic List<" + entityName + "> findByCondition(MyQuery myQuery) {\n");
				if (autoIncrementId != null) {
					builder.append("\t\tif (myQuery != null && StringUtils.isNullOrEmpty(myQuery.getOrderField())) {\n");
					builder.append("\t\t\tmyQuery.setOrderField(\"" + autoIncrementId + "\");\n");
					builder.append("\t\t\tmyQuery.setOrderType(\"ASC\");\n");
					builder.append("\t\t}\n");
				} else if (otherTypePk != null) {
					builder.append("\t\tif (myQuery != null && myQuery.getOrderField() == null) {\n");
					builder.append("\t\t\tmyQuery.setOrderField(" + otherTypePk + ");\n");
					builder.append("\t\t\tmyQuery.setOrderType(\"ASC\");\n");
					builder.append("\t\t}\n");
				}
				builder.append("\t\tPageHelper.startPage(myQuery.getPage(), myQuery.getLimit(), true);\n");
				builder.append("\t\treturn " + mapperVarName + ".findByCondition(myQuery);\n");
				builder.append("\t}\n\n");
				
				builder.append("\tpublic Integer update(" + entityName + " entity) {\n");
				builder.append("\t\treturn " + mapperVarName + ".update(entity);\n");
				builder.append("\t}\n\n");
				
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
	 * @param finalParentPath
	 * @param packageName
	 * @throws IOException 
	 */
	private static void generateController(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String tableName = table.getTableName();
			String entityName = null;
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
									new FileOutputStream(fileName), "utf-8"));
			) {
				StringBuilder builder = new StringBuilder();
				
				builder.append("package " + packageName + ".controller;\n\n");

				builder.append("import java.util.List;\n");
				builder.append("import org.springframework.web.bind.annotation.RestController;\n");
				builder.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
				builder.append("import org.springframework.web.bind.annotation.RequestMethod;\n");
				builder.append("import org.springframework.web.bind.annotation.RequestBody;\n");
				builder.append("import org.springframework.beans.factory.annotation.Autowired;\n");
				builder.append("import "+ packageName +".service." + serviceName + ";\n");
				builder.append("import "+ packageName +".entity." + entityName + ";\n");
				builder.append("import com.github.pagehelper.Page;\n");
				builder.append("import " + packageName + ".domain.ResponseResult;\n");
				builder.append("import "+ packageName +".domain.MyQuery;\n\n");
				
				builder.append("@RestController\n");
				builder.append("@RequestMapping(\"/" + entityName.toLowerCase() + "\")\n");
				builder.append("public class " + controllerName + " {\n\n");
				
				builder.append("\t@Autowired\n");
				builder.append("\tprivate " + serviceName + " " + serviceVarName + ";\n\n");
				
				builder.append("\t@RequestMapping(\"/list\")\n");
				builder.append("\tpublic ResponseResult list(MyQuery myQuery) {\n");
				builder.append("\t\tResponseResult rr = new ResponseResult();\n");
				builder.append("\t\ttry {\n");
				builder.append("\t\tList<"+ entityName +"> list = " + serviceVarName + ".findByCondition(myQuery);\n");
				builder.append("\t\t\trr.setResult(1);\n");
				builder.append("\t\t\trr.setData(list);\n");
				builder.append("\t\t\trr.setTotal((int)((Page<" + entityName + ">)list).getTotal());\n");
				builder.append("\t\t} catch (Exception e) {\n");
				builder.append("\t\t\te.printStackTrace();\n");
				builder.append("\t\t\trr.setResult(-100);\n");
				builder.append("\t\t\trr.setMessage(\"服务器异常\");\n");
				builder.append("\t\t}\n");
				builder.append("\t\treturn rr;\n");
				builder.append("\t}\n\n");
				
				builder.append("\t@RequestMapping(\"/searchlist\")\n");
				builder.append("\tpublic ResponseResult searchList(@RequestBody MyQuery myQuery) {\n");
				builder.append("\t\tResponseResult rr = new ResponseResult();\n");
				builder.append("\t\ttry {\n");
				builder.append("\t\tList<"+ entityName +"> list = " + serviceVarName + ".findByCondition(myQuery);\n");
				builder.append("\t\t\trr.setResult(1);\n");
				builder.append("\t\t\trr.setData(list);\n");
				builder.append("\t\t\trr.setTotal((int)((Page<" + entityName + ">)list).getTotal());\n");
				builder.append("\t\t} catch (Exception e) {\n");
				builder.append("\t\t\te.printStackTrace();\n");
				builder.append("\t\t\trr.setResult(-100);\n");
				builder.append("\t\t\trr.setMessage(\"服务器异常\");\n");
				builder.append("\t\t}\n");
				builder.append("\t\treturn rr;\n");
				builder.append("\t}\n\n");
				
				builder.append("\t@RequestMapping(value = \"/save\", method = RequestMethod.POST)\n");
				builder.append("\tpublic ResponseResult save(" + entityName + " entity) {\n");
				builder.append("\t\tResponseResult rr = new ResponseResult();\n");
				builder.append("\t\ttry {\n");
				builder.append("\t\t\tInteger result = " + serviceVarName + ".save(entity);\n");
				builder.append("\t\t\tif (result > 0) {\n");
				builder.append("\t\t\t\trr.setResult(1);\n");
				builder.append("\t\t\t\trr.setMessage(\"保存成功\");\n");
				builder.append("\t\t\t} else {\n");
				builder.append("\t\t\t\trr.setResult(-100);\n");
				builder.append("\t\t\t\trr.setMessage(\"服务器异常\");\n");
				builder.append("\t\t\t}\n");
				builder.append("\t\t} catch (Exception e) {\n");
				builder.append("\t\t\te.printStackTrace();\n");
				builder.append("\t\t\trr.setResult(-100);\n");
				builder.append("\t\t\trr.setMessage(\"服务器异常\");\n");
				builder.append("\t\t}\n");
				builder.append("\t\treturn rr;\n");
				builder.append("\t}\n\n");
				
				builder.append("\t@RequestMapping(value = \"/update\", method = RequestMethod.POST)\n");
				builder.append("\tpublic ResponseResult update(" + entityName + " entity) {\n");
				builder.append("\t\tResponseResult rr = new ResponseResult();\n");
				builder.append("\t\ttry {\n");
				builder.append("\t\t\tInteger result = " + serviceVarName + ".update(entity);\n");
				builder.append("\t\t\tif (result > 0) {\n");
				builder.append("\t\t\t\trr.setResult(1);\n");
				builder.append("\t\t\t\trr.setMessage(\"修改成功\");\n");
				builder.append("\t\t\t} else {\n");
				builder.append("\t\t\t\trr.setResult(-100);\n");
				builder.append("\t\t\t\trr.setMessage(\"找不到该记录\");\n");
				builder.append("\t\t\t}\n");
				builder.append("\t\t} catch (Exception e) {\n");
				builder.append("\t\t\te.printStackTrace();\n");
				builder.append("\t\t\trr.setResult(-100);\n");
				builder.append("\t\t\trr.setMessage(\"服务器异常\");\n");
				builder.append("\t\t}\n");
				builder.append("\t\treturn rr;\n");
				builder.append("\t}\n\n");
				
				builder.append("\t@RequestMapping(value = \"/delete\", method = RequestMethod.POST)\n");
				
				String autoIncrementId = null;
				List<PrimaryKey> keys = table.getAllPrimaryKeys();
				for (PrimaryKey key : keys) {
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					if (keyType.equals("Integer")) {
						autoIncrementId = keyName;
					}
				}
				builder.append("\tpublic ResponseResult delete(Integer " + autoIncrementId + ") {\n");
				builder.append("\t\tResponseResult rr = new ResponseResult();\n");
				builder.append("\t\ttry {\n");
				builder.append("\t\t\tInteger result = " + serviceVarName + ".deleteBy"
								+ toTitleCase(autoIncrementId) + "(" + autoIncrementId + ");\n");
				builder.append("\t\t\tif (result > 0) {\n");
				builder.append("\t\t\t\trr.setResult(1);\n");
				builder.append("\t\t\t\trr.setMessage(\"删除成功\");\n");
				builder.append("\t\t\t} else {\n");
				builder.append("\t\t\t\trr.setResult(-100);\n");
				builder.append("\t\t\t\trr.setMessage(\"找不到该记录\");\n");
				builder.append("\t\t\t}\n");
				builder.append("\t\t} catch (Exception e) {\n");
				builder.append("\t\t\te.printStackTrace();\n");
				builder.append("\t\t\trr.setResult(-100);\n");
				builder.append("\t\t\trr.setMessage(\"服务器异常\");\n");
				builder.append("\t\t}\n");
				builder.append("\t\treturn rr;\n");
				builder.append("\t}\n\n");
				
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
}
