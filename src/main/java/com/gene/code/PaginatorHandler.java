package com.gene.code;

import java.util.HashMap;
import java.util.Map;

public class PaginatorHandler {

    private static Map<String, String> importMap = new HashMap<>();
    static {
        importMap.put("pageHelper", "import com.github.pagehelper.PageHelper;\n");
        importMap.put("mybatis-paginator", "import com.github.miemiedev.mybatis.paginator.domain.PageBounds;\n" +
                                            "import com.github.miemiedev.mybatis.paginator.domain.PageList;\n");
    }

    public static String getImports(String paginator) {
        return importMap.get(paginator);
    }
}
