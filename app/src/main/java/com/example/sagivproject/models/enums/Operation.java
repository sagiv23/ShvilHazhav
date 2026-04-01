package com.example.sagivproject.models.enums;

/**
 * Defines the types of mathematical operations supported by the math problems generator.
 * <p>
 * This enum includes basic arithmetic operations, powers, and square roots.
 * It is used to randomize questions in the {@link com.example.sagivproject.screens.MathProblemsActivity}.
 * </p>
 */
public enum Operation {
    /**
     * Addition operation (+).
     */
    ADD,
    /**
     * Subtraction operation (-).
     */
    SUBTRACT,
    /**
     * Multiplication operation (×).
     */
    MULTIPLY,
    /**
     * Division operation (÷).
     */
    DIVIDE,
    /**
     * Power/exponentiation operation (^).
     */
    POWER,
    /**
     * Square root operation (√).
     */
    SQRT
}