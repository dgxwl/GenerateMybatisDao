package com.github.dgxwl.util;

import java.util.Arrays;

/**
 * 字符串必需工具类
 * @author dgxwl
 */
public class StringUtil {

	private StringUtil() {}
	
	/**
	 * 首字母变大写
	 * @param str 单词
	 * @return 首字母大写的字符串
	 */
	public static String toTitleCase(String str) {
		if (isEmpty(str)) {
			return str;
		}
		char[] chs = str.toCharArray();
		if (chs[0] >= 'a' && chs[0] <= 'z') {
			chs[0] -= 32;
		}
		return String.valueOf(chs);
	}

	/**
	 * 驼峰类名转变量名, AbcDef -> abcDef
	 * @param str 驼峰类名
	 * @return 变量名
	 */
	public static String classNameToVarName(String str) {
		if (isEmpty(str)) {
			return str;
		}
		char[] chs = str.toCharArray();
		if (chs[0] >= 'A' && chs[0] <= 'Z') {
			chs[0] += 32;
		}
		return String.valueOf(chs);
	}

	/**
	 * 驼峰类名转简短变量名, AbcDef -> ad
	 * @param str 驼峰类名
	 * @return 简短变量名
	 */
	public static String classNameToSimpleVarName(String str) {
		if (isEmpty(str)) {
			return str;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0, length = str.length(); i < length; i++) {
			char ch = str.charAt(i);
			if (ch >= 'A' && ch <= 'Z') {
				ch += 32;
				builder.append(ch);
			}
		}
		return builder.toString();
	}
	
	/**
	 * 从完整类名获取简短类名
	 * @param fullClassName 含包名路径的完整类名
	 * @return 简短类名
	 */
	public static String getSimpleClassName(String fullClassName) {
		if (isEmpty(fullClassName)) {
			return fullClassName;
		}
		int lastIndexOfDot = fullClassName.lastIndexOf('.');
		if (lastIndexOfDot == -1) {
			return fullClassName;
		}
		return fullClassName.substring(lastIndexOfDot + 1);
	}
	
	public static boolean isEmpty(String str) {
		return str == null || str.trim().equals("");
	}

	/**
	 * 下划线命名转驼峰命名
	 * @param str 下划线命名
	 * @return 驼峰命名
	 */
	public static String underscoreCaseToCamelCase(String str) {
		if (isEmpty(str)) {
			return str;
		}
		String[] data = str.split("[_]+");
		if (data.length == 1) {
			return str;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(data[0]);
		for (int i = 1; i < data.length; i++) {
			data[i] = toTitleCase(data[i]);
			sb.append(data[i]);
		}
		return sb.toString();
	}

	/**
	 * 驼峰命名转下划线命名
	 * @param str 驼峰命名
	 * @return 下划线命名
	 */
	public static String camelCaseToUnderscoreCase(String str) {
		if (isEmpty(str)) {
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
	
	/**
	 * 单词单数转复数. 没搞太复杂, 不规则变化的手动改吧>.<
	 * @param str
	 * @return 复数形式
	 */
	public static String toPlural(String str) {
		if (isEmpty(str)) {
			return str;
		}
		int length = str.length();
		if (length < 2) {
			return str;
		}
		if ("photo".equals(str) || "piano".equals(str)) {
			return str;
		}
		if (str.endsWith("s") || str.endsWith("o") || str.endsWith("x") || str.endsWith("sh") || str.endsWith("ch")) {
			return str + "es";
		}
		if (!Arrays.asList('a', 'e', 'i', 'o', 'u').contains(str.charAt(length - 1)) && str.endsWith("y")) {
			return str.substring(0, length - 1) + "ies";
		}
		if (str.endsWith("f")) {
			return str.substring(0, length - 1) + "ves";
		}
		if (str.endsWith("fe")) {
			return str.substring(0, length - 2) + "ves";
		}
		return str + "s";
	}
	
	public static void main(String[] args) {
		System.out.println(toPlural("cafe"));
	}
}
