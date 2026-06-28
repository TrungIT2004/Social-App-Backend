package com.minhtrung.social_app.dtos;

import com.minhtrung.social_app.enums.Visibility;

public class CreatePostRequest {
    private String text;
    private Visibility visibility = Visibility.PUBLIC;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Visibility getVisibility() {
        return this.visibility;
    }
    
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

}