package com.example.sagivproject.models;

public class Card {
    public int imageResId;
    public boolean isRevealed = false;
    public boolean isMatched = false;
    private boolean wasRevealed;

    public Card() {}

    public Card(int imageResId) {
        this.imageResId = imageResId;
    }

    public boolean getIsRevealed() { return isRevealed; }
    public void setRevealed(boolean revealed) { isRevealed = revealed; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public boolean getIsMatched() { return isMatched; }
    public void setMatched(boolean matched) { isMatched = matched; }

    public boolean getWasRevealed() { return wasRevealed; }
    public void setWasRevealed(boolean wasRevealed) { this.wasRevealed = wasRevealed; }
}
