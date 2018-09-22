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
		
		generateXmlMappers(builder.toString());
		
		//拼接包名路径
		builder.append('/').append(packageName.replace('.', '/'));
		String finalParentPath = builder.toString();
		
		generateEntity(finalParentPath, packageName);
		generateMapper(finalParentPath, packageName);
		generateService(finalParentPath);
	}
	
	/**
	 * 生成entity实体类文件
	 * @throws IOException 
	 */
	private static void generateEntity(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String entityName = null;
			if (table.getTableName().indexOf('_') == -1) {
				entityName = table.getTableName();
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
				sbBeforeClass.append("package " + packageName + "\n\n");
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
	 * 生成mapper映射器文件
	 * @throws IOException 
	 */
	private static void generateXmlMappers(String parentPath) throws IOException {
		try {
//			File f = new File(parentPath + "/mappers");
//			if (!f.exists()) {
//				f.mkdirs();
//			}
//			
//			File mapper1 = new File(parentPath + "/mappers" + "/a.txt");
//			mapper1.createNewFile();
//			PrintWriter printWriter = new PrintWriter(
//					new BufferedOutputStream(
//							new FileOutputStream(parentPath + "/mappers" + "/a.txt")), true);
//			printWriter.println("hahahahahah");
//			printWriter.println("hahahahahah");
		} catch (Exception e) {
			throw new IOException(e);
		}
//		printWriter.close();
	}
	
	/**
	 * 生成mapper接口文件
	 * @throws IOException 
	 */
	private static void generateMapper(String parentPath, String packageName) throws IOException {
		for (Table table : tables) {
			String entityName = null;
			if (table.getTableName().indexOf('_') == -1) {
				entityName = table.getTableName();
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
				builder.append("package " + packageName + "\n\n");
				
				builder.append("import java.util.List;\n");
				builder.append("import org.apache.ibatis.annotations.Param;\n");
				builder.append("import com.github.miemiedev.mybatis.paginator.domain.PageBounds;\n");
				builder.append("import "+ packageName +".entity." + entityName + ";\n");
				builder.append("import "+ packageName +".domain.MyQuery;\n\n");
				
				builder.append("public interface " + mapperName + " {\n\n");
				
				for (PrimaryKey key : keys) {
					String keyName = key.getPkName();
					String keyType = typeMap.get(key.getPkType());
					builder.append("\tList<" + entityName + "> " + "findBy"
					+ toTitleCase(keyName) + "(" + keyType + " " + keyName + ");\n");
					builder.append("\tInteger " + "deleteBy"
					+ toTitleCase(keyName) + "(" + keyType + " " + keyName + ");\n");
				}
				builder.append("\tList<" + entityName + "> " + "findByField("+ entityName +" entity);\n");
				builder.append("\tList<" + entityName + "> " + "findAll();\n");
				builder.append("\tList<" + entityName + "> " + "findWithLimit(PageBounds pageBounds);\n");
				builder.append("\tInteger " + "save(" + entityName +" entity);\n");
				builder.append("\tInteger " + "update(" + entityName + " entity);\n");
				builder.append("\n}\n");
				
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
	private static void generateService(String parentPath) {
		
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
