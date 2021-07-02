package com.github.dgxwl.base.handler.paginator;

import com.github.dgxwl.base.handler.paginator.strategy.DefaultPaginatorStrategy;
import com.github.dgxwl.base.handler.paginator.strategy.IPaginatorStrategy;
import com.github.dgxwl.base.handler.paginator.strategy.MybatisPaginatorStrategy;
import com.github.dgxwl.base.handler.paginator.strategy.PageHelperStrategy;
import com.github.dgxwl.util.StringUtil;

public class PaginatorHandler {

    public static IPaginatorStrategy getStrategy(String paginator) {
        if (StringUtil.isEmpty(paginator)) {
            return new DefaultPaginatorStrategy();
        }
        if ("mybatis-paginator".equalsIgnoreCase(paginator)) {
            return new MybatisPaginatorStrategy();
        }
        if ("pageHelper".equalsIgnoreCase(paginator)) {
            return new PageHelperStrategy();
        }
        return new DefaultPaginatorStrategy();
    }
}
