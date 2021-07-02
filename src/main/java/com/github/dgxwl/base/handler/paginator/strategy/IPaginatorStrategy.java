package com.github.dgxwl.base.handler.paginator.strategy;

public interface IPaginatorStrategy {

    String getServiceImport();

    String getMapperImport();

    void getMapperListDefines(StringBuilder builder, String entityName, String queryName, String queryVarName);

    void getServiceListBusinessLogic(StringBuilder builder, String queryVarName, String entityName,
                                     String mapperVarName, String lrrGenericName, String lrrDiamondName,
                                     String responseResultVarName, String listRrData,
                                     String titleTotal, String titlePages);
}
