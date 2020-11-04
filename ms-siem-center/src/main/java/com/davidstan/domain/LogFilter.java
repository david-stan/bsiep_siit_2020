package com.davidstan.domain;

import org.springframework.stereotype.Component;

@Component
public class LogFilter {
    private String filterByType = "";
    private String filterByContent = ""; // regex

    public LogFilter() {
    }


    public String getFilterByType() {
        return filterByType;
    }

    public void setFilterByType(String filterByType) {
        this.filterByType = filterByType;
    }

    public String getFilterByContent() {
        return filterByContent;
    }

    public void setFilterByContent(String filterByContent) {
        this.filterByContent = filterByContent;
    }

}
