package com.beckytech.physicsgrade9thteacherguide.model;

import java.io.Serializable;

public class Model implements Serializable {
    private final String title;
    private final int endPage;
    private final int startPage;
    private final String subTitle;

    public Model(String title, String subTitle, int startPage, int endPage) {
        this.title = title;
        this.startPage = startPage;
        this.endPage = endPage;
        this.subTitle = subTitle;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public int getStartPage() {
        return startPage;
    }
    public int getEndPage() {
        return endPage;
    }
}
