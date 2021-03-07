package com.github.dgxwl.payment;

import com.github.dgxwl.base.handler.dbhandler.DBHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        String sqlPattern = null;
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("sqlPattern.txt");
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                builder.append(str).append('\n');
            }
            sqlPattern = builder.toString();

            Properties prop = new Properties();
            final InputStream propIn = Main.class.getClassLoader().getResourceAsStream("payment.properties");
            prop.load(propIn);

            String billTableName = prop.getProperty("billTableName");
            String billPkName = prop.getProperty("billPkName");
            String pkType = prop.getProperty("pkType");
            String billTableComment = prop.getProperty("billTableComment");
            String columnBeforeBillAmount = prop.getProperty("columnBeforeBillAmount");
            String columnAfterBillAmount = prop.getProperty("columnAfterBillAmount");
            String billColumnComment = "";
            String columnBeforeLineAmount = prop.getProperty("columnBeforeLineAmount");
            String columnAfterLineAmount = prop.getProperty("columnAfterLineAmount");
            String lineColumnComment = "";
            String columnBeforePaymentAmount = prop.getProperty("columnBeforePaymentAmount");
            String columnAfterPaymentAmount = prop.getProperty("columnAfterPaymentAmount");
            String paymentColumnComment = "";
            String ownerId = prop.getProperty("ownerId");

            String[] parts = handleColSqlDefine(columnBeforeBillAmount, columnAfterBillAmount);
            columnBeforeBillAmount = parts[0];
            columnAfterBillAmount = parts[1];
            billColumnComment = parts[2];

            parts = handleColSqlDefine(columnBeforeLineAmount, columnAfterLineAmount);
            columnBeforeLineAmount = parts[0];
            columnAfterLineAmount = parts[1];
            lineColumnComment = parts[2];

            parts = handleColSqlDefine(columnBeforePaymentAmount, columnAfterPaymentAmount);
            columnBeforePaymentAmount = parts[0];
            columnAfterPaymentAmount = parts[1];
            paymentColumnComment = parts[2];

            String active = com.github.dgxwl.base.Main.active;
            String activeCode = com.github.dgxwl.base.Main.activeCode;
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
                    .replace("${active_code}", activeCode)
                    .replace("${billTableComment}", billTableComment)
                    .replace("${billColumnComment}", billColumnComment)
                    .replace("${columnBeforeLineAmount}", columnBeforeLineAmount)
                    .replace("${columnAfterLineAmount}", columnAfterLineAmount)
                    .replace("${lineColumnComment}", lineColumnComment)
                    .replace("${columnBeforePaymentAmount}", columnBeforePaymentAmount)
                    .replace("${columnAfterPaymentAmount}", columnAfterPaymentAmount)
                    .replace("${paymentColumnComment}", paymentColumnComment);
            System.out.println(sqlPattern);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static String[] handleColSqlDefine(String columnBeforeAmount, String columnAfterAmount) {
        StringBuilder colBuilder = new StringBuilder();
        StringBuilder commentBuilder = new StringBuilder();
        buildColSqlDefine(columnBeforeAmount, colBuilder, commentBuilder);
        columnBeforeAmount = colBuilder.toString();
        colBuilder = new StringBuilder();
        buildColSqlDefine(columnAfterAmount, colBuilder, commentBuilder);
        columnAfterAmount = colBuilder.toString();
        String billColumnComment = commentBuilder.toString();
        return new String[] {columnBeforeAmount, columnAfterAmount, billColumnComment};
    }

    private static void buildColSqlDefine(String columnProp, StringBuilder colBuilder, StringBuilder commentBuilder) {
        String[] cols = columnProp.split(";");
        for (String col : cols) {
            String[] colData = col.split("\\|");
            String colName = colData[0];
            String colType = colData[1];
            colBuilder.append('\n').append(colName).append(' ').append(colType).append(',');
            if (colData.length > 2) {
                commentBuilder.append('\n')
                        .append("COMMENT ON COLUMN ").append(colName).append(" IS '").append(colData[2]).append("';");
            }
        }
    }
}
