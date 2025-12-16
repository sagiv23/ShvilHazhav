package com.example.sagivproject.models;

public class Card {
    public int imageResId;
    public boolean isRevealed = false;
    public boolean isMatched = false;

    public Card() {}

    public Card(int imageResId) {
        this.imageResId = imageResId;
    }

    public boolean isRevealed() { return isRevealed; }
    public void setRevealed(boolean revealed) { isRevealed = revealed; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public boolean isMatched() { return isMatched; }
    public void setMatched(boolean matched) { isMatched = matched; }
}
