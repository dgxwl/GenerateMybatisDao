package com.github.dgxwl.base.handler.paginator.strategy;

public class DefaultPaginatorStrategy implements IPaginatorStrategy {
    @Override
    public String getServiceImport() {
        return "";
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
        //todo 自己实现分页
    }
}
