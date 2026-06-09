package com.compuspulse.data.remote.model;

import java.util.ArrayList;
import java.util.List;

public class PageResponse<T> {
    private int curPage;
    private List<T> datas;
    private int offset;
    private boolean over;
    private int pageCount;
    private int size;
    private int total;

    public int getCurPage() {
        return curPage;
    }

    public List<T> getDatas() {
        return datas == null ? new ArrayList<>() : datas;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isOver() {
        return over;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getSize() {
        return size;
    }

    public int getTotal() {
        return total;
    }
}
