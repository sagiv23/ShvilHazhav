package com.example.sagivproject.models.enums;

/**
 * Represents the possible states when searching for a memory game opponent.
 */
public enum SearchState {
    /**
     * The initial state, not currently searching for a game.
     */
    IDLE,
    /**
     * Actively searching for an opponent.
     */
    SEARCHING,
    /**
     * An opponent has been found and the game is about to start.
     */
    GAME_FOUND
}
