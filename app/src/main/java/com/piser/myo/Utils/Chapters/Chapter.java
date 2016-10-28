package com.piser.myo.Utils.Chapters;

/**
 * Created by sergiopadilla on 22/10/16.
 */

public class Chapter {
    /**
     * Contain the information of a chapter
     */
    private String title;
    private String code;
    private String id;

    public Chapter(String code, String title, String id) {
        this.code = code;
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getCodePlusTitle() {
        return "Chapter "+code+" - "+title;
    }
}
