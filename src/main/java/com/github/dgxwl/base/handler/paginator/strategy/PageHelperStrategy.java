package com.github.dgxwl.base.handler.paginator.strategy;

import com.github.dgxwl.util.StringUtil;

public class PageHelperStrategy implements IPaginatorStrategy {
    @Override
    public String getServiceImport() {
        return "import com.github.pagehelper.PageHelper;\n" +
                "import com.github.pagehelper.Page;\n";
    }

    @Override
    public String getMapperImport() {
        return "";
    }

    @Override
    public void getMapperListDefines(StringBuilder builder, String entityName, String queryName, String queryVarName) {
        builder.append("\tList<").append(entityName).append("> list(@Param(\"").append(queryVarName)
                .append("\") ").append(queryName).append(" ").append(queryVarName).append(");\n\n");
    }

    @Override
    public void getServiceListBusinessLogic(StringBuilder builder, String queryVarName, String entityName,
                                            String mapperVarName, String lrrGenericName, String lrrDiamondName,
                                            String responseResultVarName, String listRrData,
                                            String titleTotal, String titlePages) {
        builder.append("\t\tPageHelper.startPage(").append(queryVarName).append(".getPage(), ")
                .append(queryVarName).append(".getLimit(), true);\n");
        builder.append("\t\tList<").append(entityName).append("> list = ").append(mapperVarName)
                .append(".list(").append(queryVarName).append(");\n");
        builder.append("\t\t").append(lrrGenericName).append(" ")
                .append(lrrGenericName).append(" = new ").append(lrrDiamondName).append("();\n");
        builder.append("\t\t").append(responseResultVarName).append(".set").append(StringUtil.toTitleCase(listRrData)).append("(list);\n");
        builder.append("\t\t").append(responseResultVarName)
                .append(".set").append(titleTotal)
                .append("((int)((Page<").append(entityName).append(">)list).getTotal());\n");
        builder.append("\t\t").append(responseResultVarName)
                .append(".set").append(titlePages)
                .append("((int)((Page<").append(entityName).append(">)list).getPages());\n");
    }
}
