package com.gene.code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
	//映射SQL数据类型和Java数据类型
	private static Map<String, String> typeMap = new HashMap<>();
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
		
		generateXmlMappers(builder.toString(), packageName);
		
		//拼接包名路径
		builder.append('/').append(packageName.replace('.', '/'));
		String finalParentPath = builder.toString();
		
		generateEntity(finalParentPath, packageName);
		generateMapper(finalParentPath, packageName);
		generateService(finalParentPath, packageName);
		generateController(finalParentPath, packageName);
	}
	
	/**
	 * 生成entity实体类文件
	 * @throws IOException 
	 */
	private static void generateEntity(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String entityName = null;
			if (table.getTableName().indexOf('_') == -1) {
				entityName = toTitleCase(table.getTableName());
			} else {
				entityName = toTitleCase(table.getTableName().substring(table.getTableName().lastIndexOf('_') + 1));
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
					String fieldName = column.getColumnName();
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
			String entityName = null;
			if (table.getTableName().indexOf('_') == -1) {
				entityName = toTitleCase(table.getTableName());
			} else {
				entityName = toTitleCase(table.getTableName().substring(table.getTableName().lastIndexOf('_') + 1));
			}
			String mapperName = toTitleCase(entityName + "Mapper");
			List<PrimaryKey> keys = table.getAllPrimaryKeys();
			
			File f = new File(parentPath + "/mapper");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/mapper/" + mapperName + ".java";
			File entity = new File(fileName);
			entity.createNewFile();
			try (
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fileName), "utf-8"));
			) {
				StringBuilder builder = new StringBuilder();
				builder.append("package " + packageName + ".mapper;\n\n");
				
				builder.append("import java.util.List;\n");
				builder.append("import org.apache.ibatis.annotations.Param;\n");
				builder.append("import com.github.miemiedev.mybatis.paginator.domain.PageBounds;\n");
				builder.append("import "+ packageName +".entity." + entityName + ";\n");
				builder.append("import "+ packageName +".domain.MyQuery;\n\n");
				
				builder.append("public interface " + mapperName + " {\n\n");
				
				builder.append("\tInteger " + "save(" + entityName +" entity);\n");
				builder.append("\tInteger " + "update(" + entityName + " entity);\n");
				builder.append("\tList<" + entityName + "> " + "findByField("+ entityName +" entity);\n");
				builder.append("\tList<" + entityName + "> " + "findAll();\n");
				builder.append("\tList<" + entityName + "> " + "findWithLimit(@Param(\"myQuery\") MyQuery myQuery, PageBounds pageBounds);\n");
				for (PrimaryKey key : keys) {
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					builder.append("\t" + entityName + " findBy" + toTitleCase(keyName)
									+ "(" + keyType + " " + keyName + ");\n");
					builder.append("\tInteger " + "deleteBy" + toTitleCase(keyName)
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
		for (Table table : tables) {
			String entityName = null;
			if (table.getTableName().indexOf('_') == -1) {
				entityName = toTitleCase(table.getTableName());
			} else {
				entityName = toTitleCase(table.getTableName().substring(table.getTableName().lastIndexOf('_') + 1));
			}
			String mapperName = toTitleCase(entityName + "Mapper");
			List<PrimaryKey> keys = table.getAllPrimaryKeys();
			
			File f = new File(parentPath + "/mappers");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/mappers/" + mapperName + ".xml";
			File entity = new File(fileName);
			entity.createNewFile();
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
				
				//SAVE
				builder.append("\t<insert id=\"save\" ");
				//暂时中断save, 需要获取自增主键再继续
				StringBuilder builderFindByPk = new StringBuilder();
				String autoIncrementId = null;
				//delete需要获取所有主键, 在这完成后到最后拼接
				StringBuilder builderDelete = new StringBuilder();
				for (PrimaryKey key : keys) {
					//搞掂save的主键自增
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					if (keyType.equals("Integer")) {
						autoIncrementId = keyName;
						builder.append("useGeneratedKeys=\"true\" keyProperty=\"" + keyName + "\" ");
					}
					
					//用builderFindByPk搞掂根据主键查询的各方法, 出了循环拼回原builder
					builderFindByPk.append("\t<select id=\"findBy" + toTitleCase(keyName) + "\" "
							+ "resultType=\"" + packageName + ".entity." + entityName + "\">\n");
					builderFindByPk.append("\t\tSELECT *\n");
					builderFindByPk.append("\t\tFROM " + table.getTableName() + "\n");
					builderFindByPk.append("\t\tWHERE " + keyName + "=#{" + keyName + "}\n");
					builderFindByPk.append("\t</select>\n\n");
					
					//各款delete
					builderDelete.append("\t<delete id=\"deleteBy" + toTitleCase(keyName) + "\">\n");
					builderDelete.append("\t\tDELETE FROM " + table.getTableName() + "\n");
					builderDelete.append("\t\tWHERE " + keyName + "=#{" + keyName + "}\n");
					builderDelete.append("\t</delete>\n\n");
				}
				//这里继续完成save
				builder.append("parameterType=\"" + packageName + ".entity." + entityName + "\">\n");
				builder.append("\t\tINSERT INTO " + table.getTableName() + " (\n");
				//需要遍历所有字段, 再次中断save
				
				//UPDATE
				StringBuilder builderUpdate = new StringBuilder();
				builderUpdate.append("\t<update id=\"update\" parameterType=\"" + packageName + ".entity." + entityName + "\">\n");
				builderUpdate.append("\t\tUPDATE " + table.getTableName() + "\n");
				builderUpdate.append("\t\t<set>\n");
				
				StringBuilder builderAfterValues = new StringBuilder();  //保存 ) VALUES ( 后面的插入字段
				int i = 1;
				for (Column c : table.getAllColumns()) {
					String columnName = c.getColumnName();
					//搞掂save的插入字段
					if (!columnName.equals(autoIncrementId)) {
						builder.append("\t\t\t" + columnName + ",\n");
						builderAfterValues.append("\t\t\t#{" + columnName + "},\n");
					}
					
					//搞掂update
					if (!columnName.equals(autoIncrementId)) {
						builderUpdate.append("\t\t<if test=\"" + columnName + "!=null and " + columnName + "!=''\">\n");
						builderUpdate.append("\t\t" + columnName + "=#{" + columnName + "}");
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
								+ packageName + ".entity." + entityName + "\">\n");
				builder.append("\t\tSELECT *\n");
				builder.append("\t\tFROM " + table.getTableName() + "\n");
				builder.append("\t\t<where>\n");
				i = 1;
				for (Column c : table.getAllColumns()) {
					String columnName = c.getColumnName();
					if (!columnName.equals(autoIncrementId)) {
						builder.append("\t\t\t<if test=\"" + columnName + "!=null and " + columnName + "!=''\">\n");
						builder.append("\t\t\t" + columnName + "=#{" + columnName);
						if (i == table.getAllColumns().size()) {
							builder.append("}\n");
						} else {
							builder.append("},\n");
						}
						builder.append("\t\t\t</if>\n");
					}
					i++;
				}
				builder.append("\t\t</where>\n");
				builder.append("\t</select>\n\n");
				
				//findWithLimit
				builder.append("\t<select id=\"findWithLimit\" resultType=\""
								+ packageName + ".entity." + entityName + "\">\n");
				builder.append("\t\tSELECT *\n");
				builder.append("\t\tFROM " + table.getTableName() + "\n");
				builder.append("\t\tORDER BY ${myQuery.orderField} ${myQuery.orderType}\n");
				builder.append("\t</select>\n\n");
				
				//findAll
				builder.append("\t<select id=\"findAll\" resultType=\""
								+ packageName + ".entity." + entityName + "\">\n");
				builder.append("\t\tSELECT *\n");
				builder.append("\t\tFROM " + table.getTableName() + "\n");
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
	 * 生成service文件
	 * @throws IOException 
	 */
	private static void generateService(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String entityName = null;
			String mapperVarName = null;
			if (table.getTableName().indexOf('_') == -1) {
				entityName = toTitleCase(table.getTableName());
			} else {
				mapperVarName = table.getTableName().substring(table.getTableName().lastIndexOf('_') + 1) + "Mapper";
				entityName = toTitleCase(table.getTableName().substring(table.getTableName().lastIndexOf('_') + 1));
			}
			String serviceName = toTitleCase(entityName + "Service");
			String mapperName = entityName + "Mapper";
			List<PrimaryKey> keys = table.getAllPrimaryKeys();
			
			File f = new File(parentPath + "/service");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/service/" + serviceName + ".java";
			File entity = new File(fileName);
			entity.createNewFile();
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
				builder.append("import com.github.miemiedev.mybatis.paginator.domain.PageBounds;\n");
				builder.append("import "+ packageName +".mapper." + entityName + "Mapper;\n");
				builder.append("import "+ packageName +".entity." + entityName + ";\n");
				builder.append("import " + packageName + ".domain.ResponseResult;\n");
				builder.append("import " + packageName + ".domain.ResponseListResult;\n");
				builder.append("import "+ packageName +".domain.MyQuery;\n\n");
				
				builder.append("@Service\n");
				builder.append("public class " + serviceName + " {\n\n");
				
				builder.append("\t@Autowired\n");
				builder.append("\tprivate " + mapperName + " " + mapperVarName + ";\n\n");
				
				builder.append("\tpublic Integer save(" + entityName +" entity) {\n");
				builder.append("\t\treturn " + mapperVarName + ".save(entity);\n");
				builder.append("\t}\n\n");
				
				StringBuilder builderFindBy = new StringBuilder();
				String autoIncrementId = null;
				String otherTypePk = null;
				for (PrimaryKey key : keys) {
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					if (keyType.equals("Integer")) {
						autoIncrementId = keyName;
					} else if (keyType.equals("String")) {
						otherTypePk = keyName;
					}

					builder.append("\tpublic Integer " + "deleteBy" + toTitleCase(keyName)
									+ "(" + keyType + " " + keyName + "){ \n");
					builder.append("\t\treturn " + mapperVarName + ".deleteBy"
									+ toTitleCase(keyName) + "(" + keyName + ");\n");
					builder.append("\t}\n\n");
					
					builderFindBy.append("\tpublic " + entityName + " findBy"
								+ toTitleCase(keyName) + "(" + keyType + " " + keyName + ") {\n");
					builderFindBy.append("\t\treturn " + mapperVarName + ".findBy"
								+ toTitleCase(keyName) + "(" + keyName + ");\n");
					builderFindBy.append("\t}\n\n");
				}
				
				builder.append(builderFindBy);
				
				builder.append("\tpublic List<" + entityName + "> findByField(" + entityName + " entity) {\n");
				builder.append("\t\treturn " + mapperVarName + ".findByField(entity);\n");
				builder.append("\t}\n\n");

				builder.append("\tpublic List<" + entityName + "> findAll() {\n");
				builder.append("\t\treturn " + mapperVarName + ".findAll();\n");
				builder.append("\t}\n\n");
				
				builder.append("\tpublic List<" + entityName + "> findWithLimit(MyQuery myQuery, PageBounds pageBounds) {\n");
				if (autoIncrementId != null) {
					builder.append("\t\tif (myQuery != null && myQuery.getOrderField() == null) {\n");
					builder.append("\t\t\tmyQuery.setOrderField(\"" + autoIncrementId + "\");\n");
					builder.append("\t\t\tmyQuery.setOrderType(\"ASC\");\n");
					builder.append("\t\t}\n");
				} else if (otherTypePk != null) {
					builder.append("\t\tif (myQuery != null && myQuery.getOrderField() == null) {\n");
					builder.append("\t\t\tmyQuery.setOrderField(" + otherTypePk + ");\n");
					builder.append("\t\t\tmyQuery.setOrderType(\"ASC\");\n");
					builder.append("\t\t}\n");
				}
				builder.append("\t\tif (pageBounds == null) {\n");
				builder.append("\t\t\tpageBounds = new PageBounds();\n");
				builder.append("\t\t\tpageBounds.setLimit(10);\n");
				builder.append("\t\t\tpageBounds.setPage(1);\n");
				builder.append("\t\t}\n");
				builder.append("\t\treturn " + mapperVarName + ".findWithLimit(myQuery, pageBounds);\n");
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
			String entityName = null;
			String serviceVarName = null;
			if (table.getTableName().indexOf('_') == -1) {
				entityName = toTitleCase(table.getTableName());
			} else {
				serviceVarName = table.getTableName().substring(table.getTableName().lastIndexOf('_') + 1) + "Service";
				entityName = toTitleCase(table.getTableName().substring(table.getTableName().lastIndexOf('_') + 1));
			}
			String controllerName = toTitleCase(entityName + "Controller");
			String serviceName = entityName + "Service";
			List<PrimaryKey> keys = table.getAllPrimaryKeys();
			
			File f = new File(parentPath + "/controller");
			if (!f.exists()) {
				f.mkdirs();
			}
			String fileName = parentPath + "/controller/" + controllerName + ".java";
			File entity = new File(fileName);
			entity.createNewFile();
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
				builder.append("import org.springframework.beans.factory.annotation.Autowired;\n");
				builder.append("import com.github.miemiedev.mybatis.paginator.domain.PageBounds;\n");
				builder.append("import "+ packageName +".service." + serviceName + ";\n");
				builder.append("import "+ packageName +".entity." + entityName + ";\n");
				builder.append("import " + packageName + ".domain.ResponseResult;\n");
				builder.append("import " + packageName + ".domain.ResponseListResult;\n");
				builder.append("import "+ packageName +".domain.MyQuery;\n\n");
				
				builder.append("@RestController\n");
				builder.append("public class " + controllerName + " {\n\n");
				
				builder.append("\t@Autowired\n");
				builder.append("\tprivate " + serviceName + " " + serviceVarName + ";\n\n");
				
				builder.append("\t@RequestMapping(\"/list\")\n");
				builder.append("\tpublic ResponseListResult<" + entityName + "> list(MyQuery myQuery, PageBounds pageBounds) {\n");
				builder.append("\t\tResponseListResult<" + entityName + "> rr = new ResponseListResult<>();\n");
				builder.append("\t\ttry {\n");
				builder.append("\t\t\trr.setResult(1);\n");
				builder.append("\t\t\trr.setRows(" + serviceVarName + ".findWithLimit(myQuery, pageBounds));\n");
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
				builder.append("\t\t\t\trr.setMessage(\"服务器异常\");\n");
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
				builder.append("\t\t\t\trr.setMessage(\"服务器异常\");\n");
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
}
