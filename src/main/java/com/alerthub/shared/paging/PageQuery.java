package com.alerthub.shared.paging;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public class PageQuery {

    @QueryParam("page")
    @DefaultValue("0")
    private int page;

    @QueryParam("size")
    @DefaultValue("20")
    private int size;

    public int page() {
        return Math.max(page, 0);
    }

    public int size() {
        return Math.min(Math.max(size, 1), 100);
    }
}
