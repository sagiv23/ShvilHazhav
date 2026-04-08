package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a single card in the online memory game.
 * <p>
 * This class holds the state of a card, including its unique identifier (shared
 * with its matching pair), its image content as a Base64 string, and its current state
 * in the game (revealed, matched).
 * </p>
 */
public class Card implements Serializable, Idable {
    private String id;
    private String base64Content;
    private boolean isRevealed = false;
    private boolean isMatched = false;
    private boolean wasRevealed;

    /**
     * Default constructor required for Firebase deserialization (DataSnapshot.getValue).
     */
    public Card() {
    }

    /**
     * Constructs a new Card with a specific ID and image content.
     *
     * @param id            The identifier for the card, shared with its match.
     * @param base64Content The Base64 encoded string of the card's image.
     */
    public Card(String id, String base64Content) {
        this.id = id;
        this.base64Content = base64Content;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the Base64 image content of the card.
     *
     * @return Base64 string.
     */
    public String getBase64Content() {
        return base64Content;
    }

    /**
     * Sets the Base64 image content of the card.
     *
     * @param base64Content New image content.
     */
    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
    }

    /**
     * Checks if the card is currently revealed (flipped up).
     *
     * @return true if revealed.
     */
    public boolean getIsRevealed() {
        return isRevealed;
    }

    /**
     * Sets the revealed state of the card.
     *
     * @param revealed true to flip up, false to flip down.
     */
    public void setRevealed(boolean revealed) {
        isRevealed = revealed;
    }

    /**
     * Checks if the card has been successfully matched.
     *
     * @return true if matched.
     */
    public boolean getIsMatched() {
        return isMatched;
    }

    /**
     * Sets the matched state of the card.
     *
     * @param matched true if the card pair was found.
     */
    public void setMatched(boolean matched) {
        isMatched = matched;
    }

    /**
     * Internal flag to track if the card was previously revealed, used for animation triggers.
     * Annotated with {@code @Exclude} to prevent saving to Firebase.
     *
     * @return true if the card was revealed in the previous frame.
     */
    @Exclude
    public boolean wasRevealed() {
        return wasRevealed;
    }

    /**
     * Sets the previous revealed state for animation tracking.
     *
     * @param wasRevealed previous state.
     */
    @Exclude
    public void setWasRevealed(boolean wasRevealed) {
        this.wasRevealed = wasRevealed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return isRevealed == card.isRevealed &&
                isMatched == card.isMatched &&
                Objects.equals(id, card.id) &&
                Objects.equals(base64Content, card.base64Content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, base64Content, isRevealed, isMatched);
    }

    @NonNull
    @Override
    public String toString() {
        return "Card{" +
                "id='" + id + '\'' +
                ", base64Content='" + base64Content + '\'' +
                ", isRevealed=" + isRevealed +
                ", isMatched=" + isMatched +
                ", wasRevealed=" + wasRevealed +
                '}';
    }
}