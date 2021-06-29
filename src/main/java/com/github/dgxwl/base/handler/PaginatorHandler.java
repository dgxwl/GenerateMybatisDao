package com.github.dgxwl.base.handler;

import java.util.HashMap;
import java.util.Map;

public class PaginatorHandler {

	private static Map<String, String> serviceImportMap = new HashMap<>(4);
    private static Map<String, String> mapperImportMap = new HashMap<>(4);
    static {
    	serviceImportMap.put("pageHelper", "import com.github.pagehelper.PageHelper;\n" +
                "import com.github.pagehelper.Page;\n");
    	serviceImportMap.put("mybatis-paginator", "import com.github.miemiedev.mybatis.paginator.domain.PageBounds;\n" +
		                       "import com.github.miemiedev.mybatis.paginator.domain.PageList;\n");

        mapperImportMap.put("pageHelper", "");
        mapperImportMap.put("mybatis-paginator", "import com.github.miemiedev.mybatis.paginator.domain.PageBounds;\n" +
                                           "import com.github.miemiedev.mybatis.paginator.domain.PageList;\n");
    }
    
    public static String getServiceImportStr(String paginator) {
        return serviceImportMap.get(paginator);
    }

    public static String getMapperImportStr(String paginator) {
        return mapperImportMap.get(paginator);
    }
}
