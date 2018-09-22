package com.gene.code;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
		generateMapper(finalParentPath);
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
				entityName = table.getTableName().substring(table.getTableName().lastIndexOf('_') + 1);
				char[] chs = entityName.toCharArray();
				chs[0] -= 32;
				entityName = String.valueOf(chs);
			}
			
			List<Column> fields = table.getAllColumns();
			
			File f = new File(parentPath + "/entity");
			if (!f.exists()) {
				f.mkdirs();
			}
			File entity = new File(parentPath + "/entity/" + entityName + ".java");
			entity.createNewFile();
			try (
					PrintWriter pw = new PrintWriter(
							new OutputStreamWriter(
									new FileOutputStream(parentPath + "/entity/" + entityName + ".java"), "utf-8"), true);
			) {
				pw.println("package " + packageName);
				pw.println();
				pw.println("import lombok.Data;");
				pw.println();
				pw.println("@Data");
				pw.println("public class " + entityName + "{");
				for (Column column : fields) {
					String fieldName = column.getColumnName();
					String fieldType = typeMap.get(column.getType());
					boolean hasRemark = column.getRemarks() != null && !column.getRemarks().equals("");
					
					pw.println("\tprivate " + fieldType + " " + fieldName + ";" + (hasRemark ? "  //" + column.getRemarks() : ""));
				}
				pw.println("}");
			} catch (IOException e) {
				e.printStackTrace();
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
	 */
	private static void generateMapper(String parentPath) {
		
	}
	
	/**
	 * 生成service文件
	 */
	private static void generateService(String parentPath) {
		
	}
}
