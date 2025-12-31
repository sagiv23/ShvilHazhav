package com.example.sagivproject.models;

public class Card {
    private String imageId;
    private String base64Content;
    private boolean isRevealed = false;
    private boolean isMatched = false;
    private boolean wasRevealed;

    public Card() {}

    public Card(String imageId, String base64Content) {
        this.imageId = imageId;
        this.base64Content = base64Content;
    }

    public String getBase64Content() { return base64Content; }
    public void setBase64Content(String base64Content) { this.base64Content = base64Content; }

    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }

    public boolean getIsRevealed() { return isRevealed; }
    public void setRevealed(boolean revealed) { isRevealed = revealed; }

    public boolean getIsMatched() { return isMatched; }
    public void setMatched(boolean matched) { isMatched = matched; }

    public boolean getWasRevealed() { return wasRevealed; }
    public void setWasRevealed(boolean wasRevealed) { this.wasRevealed = wasRevealed; }
}
