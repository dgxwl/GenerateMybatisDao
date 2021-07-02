package com.github.dgxwl.base.handler.paginator.strategy;

import com.github.dgxwl.util.StringUtil;

public class MybatisPaginatorStrategy implements IPaginatorStrategy {

    @Override
    public String getServiceImport() {
        return "import com.github.miemiedev.mybatis.paginator.domain.PageBounds;\n" +
                "import com.github.miemiedev.mybatis.paginator.domain.PageList;\n";
    }

    @Override
    public String getMapperImport() {
        return "import com.github.miemiedev.mybatis.paginator.domain.PageBounds;\n" +
                "import com.github.miemiedev.mybatis.paginator.domain.PageList;\n";
    }

    @Override
    public void getMapperListDefines(StringBuilder builder, String entityName, String queryName, String queryVarName) {
        builder.append("\tPageList<").append(entityName).append("> list(@Param(\"").append(queryVarName)
                .append("\") ").append(queryName).append(" ").append(queryVarName)
                .append(", @Param(\"pageBounds\") PageBounds pageBounds);\n\n");
    }

    @Override
    public void getServiceListBusinessLogic(StringBuilder builder, String queryVarName, String entityName,
                                            String mapperVarName, String lrrGenericName, String lrrDiamondName,
                                            String responseResultVarName, String listRrData,
                                            String titleTotal, String titlePages) {
        builder.append("\t\tPageList<").append(entityName).append("> list = ").append(mapperVarName)
                .append(".list(").append(queryVarName).append(", new PageBounds(").append(queryVarName)
                .append(".getPage(), ").append(queryVarName).append(".getLimit()));\n");
        builder.append("\t\t").append(lrrGenericName).append(" ")
                .append(responseResultVarName).append(" = new ").append(lrrDiamondName).append("();\n");
        builder.append("\t\t").append(responseResultVarName).append(".set").append(StringUtil.toTitleCase(listRrData)).append("(list);\n");
        builder.append("\t\t").append(responseResultVarName).append(".set").append(titleTotal)
                .append("(list.getPaginator().getTotalCount());\n");
        builder.append("\t\t").append(responseResultVarName).append(".set").append(titlePages)
                .append("(list.getPaginator().getTotalPages());\n");
    }
}
