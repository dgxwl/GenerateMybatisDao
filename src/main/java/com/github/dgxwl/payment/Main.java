package com.github.dgxwl.payment;

import com.github.dgxwl.base.ApiGenerator;
import com.github.dgxwl.base.handler.dbhandler.DBHandler;
import com.github.dgxwl.util.DBUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        String sqlPattern;
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("sqlPattern.txt");
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr);
             InputStream propIn = Main.class.getClassLoader().getResourceAsStream("payment.properties")) {
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                builder.append(str).append('\n');
            }
            sqlPattern = builder.toString();

            Properties prop = new Properties();
            prop.load(propIn);

            String billTableName = prop.getProperty("billTableName");
            String billLineTableName = billTableName + "_line";
            String billPaymentTableName = billTableName + "_payment";
            String billPkName = prop.getProperty("billPkName");
            String pkType = prop.getProperty("pkType");
            String billTableComment = new String(prop.getProperty("billTableComment", "").getBytes("ISO8859-1"), "GBK");
            String columnBeforeBillAmount = new String(prop.getProperty("columnBeforeBillAmount", "").getBytes("ISO8859-1"), "GBK");
            String columnAfterBillAmount = new String(prop.getProperty("columnAfterBillAmount", "").getBytes("ISO8859-1"), "GBK");
            String billColumnComment = "";
            String columnBeforeLineAmount = new String(prop.getProperty("columnBeforeLineAmount", "").getBytes("ISO8859-1"), "GBK");
            String columnAfterLineAmount = new String(prop.getProperty("columnAfterLineAmount", "").getBytes("ISO8859-1"), "GBK");
            String lineColumnComment = "";
            String columnBeforePaymentAmount = new String(prop.getProperty("columnBeforePaymentAmount", "").getBytes("ISO8859-1"), "GBK");
            String columnAfterPaymentAmount = new String(prop.getProperty("columnAfterPaymentAmount", "").getBytes("ISO8859-1"), "GBK");
            String paymentColumnComment = "";
            String ownerId = prop.getProperty("ownerId");

            String[] parts = handleColSqlDefine(columnBeforeBillAmount, columnAfterBillAmount, billTableName);
            columnBeforeBillAmount = parts[0];
            columnAfterBillAmount = parts[1];
            billColumnComment = parts[2];

            parts = handleColSqlDefine(columnBeforeLineAmount, columnAfterLineAmount, billLineTableName);
            columnBeforeLineAmount = parts[0];
            columnAfterLineAmount = parts[1];
            lineColumnComment = parts[2];

            parts = handleColSqlDefine(columnBeforePaymentAmount, columnAfterPaymentAmount, billPaymentTableName);
            columnBeforePaymentAmount = parts[0];
            columnAfterPaymentAmount = parts[1];
            paymentColumnComment = parts[2];

            String active = ApiGenerator.active;
            String activeCode = ApiGenerator.activeCode;
            String shortIntType = DBHandler.getShortIntType();
            String dateTimeType = DBHandler.getDateTimeType();

            sqlPattern = sqlPattern.replace("${billTableName}", billTableName)
                    .replace("${billPkName}", billPkName)
                    .replaceAll("\\$\\{pkType}", pkType)
                    .replace("${ownerId}", ownerId)
                    .replace("${columnBeforeBillAmount}", columnBeforeBillAmount)
                    .replace("${columnAfterBillAmount}", columnAfterBillAmount)
                    .replace("${shortIntType}", shortIntType)
                    .replace("${dateTimeType}", dateTimeType)
                    .replace("${active}", active)
                    .replace("${active_code}", activeCode)
                    .replace("${billTableComment}", billTableComment)
                    .replace("${billColumnComment}", billColumnComment)
                    .replace("${columnBeforeLineAmount}", columnBeforeLineAmount)
                    .replace("${columnAfterLineAmount}", columnAfterLineAmount)
                    .replace("${lineColumnComment}", lineColumnComment)
                    .replace("${columnBeforePaymentAmount}", columnBeforePaymentAmount)
                    .replace("${columnAfterPaymentAmount}", columnAfterPaymentAmount)
                    .replace("${paymentColumnComment}", paymentColumnComment);

            Connection conn = DBUtils.getConnection();
            PreparedStatement ps = conn.prepareStatement(sqlPattern);
            ps.execute();
            DBUtils.closeConnection(conn);

            String tablesStr = billTableName + "," + billPaymentTableName;
            String oneToMany = billTableName + ":" + billLineTableName;
            new ApiGenerator(tablesStr, oneToMany).generate();


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static String[] handleColSqlDefine(String columnBeforeAmount, String columnAfterAmount, String billTableName) {
        StringBuilder colBuilder = new StringBuilder();
        StringBuilder commentBuilder = new StringBuilder();
        buildColSqlDefine(columnBeforeAmount, billTableName, colBuilder, commentBuilder);
        columnBeforeAmount = colBuilder.toString();
        colBuilder = new StringBuilder();
        buildColSqlDefine(columnAfterAmount, billTableName, colBuilder, commentBuilder);
        columnAfterAmount = colBuilder.toString();
        String billColumnComment = commentBuilder.toString();
        return new String[] {columnBeforeAmount, columnAfterAmount, billColumnComment};
    }

    private static void buildColSqlDefine(String columnProp, String billTableName, StringBuilder colBuilder, StringBuilder commentBuilder) {
        String[] cols = columnProp.split(";");
        for (String col : cols) {
            String[] colData = col.split("\\|");
            String colName = colData[0];
            String colType = colData[1];
            colBuilder.append('\n').append(colName).append(' ').append(colType).append(',');
            if (colData.length > 2) {
                commentBuilder.append('\n')
                        .append("COMMENT ON COLUMN ").append(billTableName).append('.').append(colName).append(" IS '").append(colData[2]).append("';");
            }
        }
    }
}
