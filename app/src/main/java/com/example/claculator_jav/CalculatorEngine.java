package com.example.claculator_jav;

import java.math.BigDecimal; // Using BigDecimal for better precision
import java.math.RoundingMode; // For controlling division precision

public class CalculatorEngine {

    // --- Constants ---
    private static final int MAX_DIGITS = 16; // Max digits for display/input
    private static final int DIVISION_SCALE = 8; // Precision for division

    // --- State Variables ---
    private BigDecimal currentOperand;      // The number currently being entered or the result
    private BigDecimal pendingOperand;      // The first operand waiting for an operator
    private String pendingOperator;       // The operator (+, -, *, /) waiting to be applied
    private boolean isEnteringDigits;     // Flag to know if the user is currently typing a number
    private boolean isInErrorState = false; // Flag for error state (e.g., division by zero)

    // --- Constructor ---
    public CalculatorEngine() {
        clear(); // Initialize the state
    }

    // --- Public Methods (Interface for ViewModel) ---

    /**
     * Clears the calculator state (AC button).
     */
    public void clear() {
        currentOperand = BigDecimal.ZERO;
        pendingOperand = null;
        pendingOperator = null;
        isEnteringDigits = false;
        isInErrorState = false; // Clear error on AC
        // System.out.println("Engine: Cleared"); // Debugging
    }

    /**
     * Processes a digit input (0-9).
     * @param digit The digit pressed ("0" through "9").
     */
    public void inputDigit(String digit) {
        /**
         * Processes a digit input (0-9).
         * @param digit The digit pressed ("0" through "9").
         */

            if (isInErrorState) {
                clear(); // Start fresh after an error by clearing everything
            }

            String currentText; // Will hold the string representation to be parsed

            if (!isEnteringDigits) {
                // --- Start: Entering a new number ---
                currentText = digit; // Start the new number string directly with the digit
                isEnteringDigits = true; // Now we are entering digits
                // If the first digit entered is "0", currentText is now "0". That's correct.
                // --- End: Entering a new number ---
            } else {
                // --- Start: Appending to existing number ---
                currentText = currentOperand.toPlainString(); // Get the current value as a string

                // Prevent adding digits beyond the limit (Check before appending)
                // Count actual digits, ignoring '-' and '.'
                String plainDigits = currentText.replace("-", "").replace(".", "");
                if (plainDigits.length() >= MAX_DIGITS) {
                    System.out.println("Engine: Max digits reached"); // Debugging
                    return; // Do not append if max digits are already reached
                }

                // Handle appending to "0" (e.g., "0" -> "5", but "0." -> "0.5")
                if (currentText.equals("0")) {
                    if (digit.equals("0")) {
                        // If current value is "0" and user presses "0", do nothing
                        return;
                    } else {
                        // If current value is "0" and user presses "1"-"9", replace "0"
                        currentText = digit;
                    }
                } else {
                    // Append the digit to the non-zero number
                    currentText += digit;
                }
                // --- End: Appending to existing number ---
            }

            // --- Update BigDecimal from the constructed string ---
            try {
                // BigDecimal can parse strings like "0", "5", "123", "0.5" etc.
                currentOperand = new BigDecimal(currentText);
            } catch (NumberFormatException e) {
                System.err.println("Engine: Error parsing digit input: " + e.getMessage());
                isInErrorState = true; // Set error state
                // Optionally reset currentOperand here, e.g., currentOperand = BigDecimal.ZERO;
            }
            // System.out.println("Engine: Input Digit " + digit + ", Current Operand = " + currentOperand.toPlainString()); // Debugging
        }
    public void inputDecimal() {
        if (isInErrorState) {
            clear(); // Start fresh after an error by clearing everything
        }

        String currentText = currentOperand.toPlainString();

        if (!isEnteringDigits) {
            if (pendingOperator != null) {
                currentText = "0";
            }
            currentText = "0.";
            isEnteringDigits = true;
        } else {
            if (!currentText.contains(".")) {
                if (currentText.length() >= MAX_DIGITS) {
                    System.out.println("Engine: Max digits reached (decimal)"); // Debugging
                    return;
                }
                currentText += ".";
            } else {
                System.out.println("Engine: Decimal already exists"); // Debugging
                return;
            }
        }

        try {
            // Update currentOperand to reflect the presence of the decimal
            // Even if it's just "0." or "5.", BigDecimal can handle this intermediate state
            currentOperand = new BigDecimal(currentText);
        } catch (NumberFormatException e) {
            // This might happen if currentText somehow becomes invalid like "-." though unlikely here
            System.err.println("Engine: Error parsing decimal input: " + e.getMessage());
            isInErrorState = true;
        }
        // System.out.println("Engine: Current Operand = " + currentOperand.toPlainString()); // Debugging
    }

    /**
     * Processes an operator input (+, -, ×, ÷).
     * @param operator The operator pressed ("+", "-", "×", "÷").
     */
    public void inputOperator(String operator) {
        if (isInErrorState) {
            clear(); // Start fresh after an error by clearing everything
        }

        if (isEnteringDigits) {
            if (pendingOperator != null && pendingOperand != null) {
                if (!isInErrorState) { // Don't calculate if previous step resulted in error
                    performCalculation();
                }
                // If performCalculation caused an error, isInErrorState is now true
                if (isInErrorState) return; // Stop processing if calculation failed
            } else {
                pendingOperand = currentOperand;
            }
            isEnteringDigits = false;
        }

        if (pendingOperand == null) {
            if (!isInErrorState) { // If no error, use current value
                pendingOperand = currentOperand;
            } else { // If error, pending should remain null or be zero after clear
                pendingOperand = BigDecimal.ZERO; // Assume starting from 0 after error+operator
            }
        }

        pendingOperator = operator;
        // System.out.println("Engine: Operator=" + operator + ", PendingOperand=" + (pendingOperand != null ? pendingOperand.toPlainString() : "null")); // Debugging
    }

    /**
     * Processes the equals input (=). Performs the final calculation.
     */
    public void calculateResult() {
        if (isInErrorState) return; // Don't do anything if error state is already set

        if (pendingOperator != null && pendingOperand != null) {
            performCalculation(); // This might set isInErrorState

            // Only clear pendingOperand if no error occurred
            if (!isInErrorState) {
                pendingOperand = null; // Reset for new calculation start after '='
            }
        } else {
            // System.out.println("Engine: Equals pressed with no pending operation."); // Debugging
        }
        // System.out.println("Engine: Equals processed. Display = " + getDisplayValue()); // Debugging
    }

    /**
     * Processes the backspace input. Deletes the last entered digit or decimal point.
     */
    public void backspace() {
        if (isInErrorState) {
            System.out.println("Engine: Operation ignored (in error state)"); // Debugging
            return; // Do nothing if in error state
        }

        if (!isEnteringDigits) {
            System.out.println("Engine: Backspace ignored (not entering digits)"); // Debugging
            return;
        }

        String currentText = currentOperand.toPlainString();
        if (currentText.length() > 0 && !currentText.equals("0")) {
            currentText = currentText.substring(0, currentText.length() - 1);

            if (currentText.isEmpty() || currentText.equals("-")) {
                currentText = "0";
            }

            try {
                currentOperand = new BigDecimal(currentText);
            } catch (NumberFormatException e) {
                System.err.println("Engine: Error parsing after backspace: " + e.getMessage());
                currentOperand = BigDecimal.ZERO; // Reset to 0 if backspace created invalid number like "-"
            }
        } else {
            System.out.println("Engine: Backspace ignored (already 0 or empty)"); // Debugging
        }
        // System.out.println("Engine: Backspace processed. Current Operand = " + currentOperand.toPlainString()); // Debugging
    }

    /**
     * Processes the percentage input (%).
     * Behavior depends on context.
     */
    public void calculatePercentage() {
        if (isInErrorState) {
            System.out.println("Engine: Operation ignored (in error state)"); // Debugging
            return; // Do nothing if in error state
        }

        BigDecimal hundred = BigDecimal.valueOf(100);

        try {
            if (pendingOperator != null && pendingOperand != null) {
                // Scenario: 50 + 10% -> Calculate 10% of 50
                BigDecimal percentageOfPending = pendingOperand.multiply(currentOperand).divide(hundred, DIVISION_SCALE, RoundingMode.HALF_UP);
                currentOperand = percentageOfPending; // This becomes the new currentOperand for the pending '+'
                System.out.println("Engine: Calculated % of pending: " + currentOperand.toPlainString()); // Debugging
            } else {
                // Scenario: 50% -> Calculate 50 / 100
                currentOperand = currentOperand.divide(hundred, DIVISION_SCALE, RoundingMode.HALF_UP);
                System.out.println("Engine: Calculated value / 100: " + currentOperand.toPlainString()); // Debugging
            }

            isEnteringDigits = false; // Result of % is shown, not entering digits

        } catch (ArithmeticException e) {
            System.err.println("Engine: Arithmetic error during percentage: " + e.getMessage());
            isInErrorState = true; // Set error state
        } catch (Exception e) {
            System.err.println("Engine: Unexpected error during percentage: " + e.getMessage());
            isInErrorState = true; // Set error state
        }
    }

    /**
     * Gets the value to be displayed on the calculator screen.
     * Formats the number nicely (e.g., removes trailing decimal zeros).
     * Returns "Error" if the engine is in an error state.
     * @return A string representation of the current operand, result, or "Error".
     */
    public String getDisplayValue() {
        if (isInErrorState) {
            return "Error";
        }

        if (currentOperand == null) {
            return "0";
        }

        BigDecimal displayVal = currentOperand.stripTrailingZeros();
        String displayText = displayVal.toPlainString();

        // Handle cases where user is actively typing a decimal
        if (isEnteringDigits) {
            String plainString = currentOperand.toPlainString();
            if (plainString.endsWith(".")) {
                // If user typed "5.", show "5." not "5"
                displayText = plainString;
            } else if (plainString.matches("^-?0\\.$")) {
                // If user typed "0." or "-0.", show that
                displayText = plainString;
            }
        }

        // Optional: Add locale-specific formatting or scientific notation handling here if needed.
        // Optional: Limit display length if needed

        // System.out.println("Engine: getDisplayValue returns: " + displayText); // Debugging
        return displayText;
    }


    // --- Private Helper Methods ---

    /**
     * Performs the pending calculation if an operator and operands are ready.
     * Updates currentOperand with the result and resets pending state, or sets error state.
     */
    private void performCalculation() {
        if (pendingOperator == null || pendingOperand == null) {
            return;
        }
        if (isInErrorState) return; // Don't calculate if already in error

        BigDecimal result = BigDecimal.ZERO; // Keep default

        try {
            switch (pendingOperator) {
                case "+":
                    result = pendingOperand.add(currentOperand);
                    break;
                case "-":
                    result = pendingOperand.subtract(currentOperand);
                    break;
                case "×":
                    result = pendingOperand.multiply(currentOperand);
                    break;
                case "÷":
                    if (currentOperand.compareTo(BigDecimal.ZERO) == 0) {
                        System.err.println("Engine: Division by zero!");
                        isInErrorState = true;
                        currentOperand = BigDecimal.ZERO; // Reset the currentOperand when there is an error.
                        return; // Exit calculation attempt
                    }
                    result = pendingOperand.divide(currentOperand, DIVISION_SCALE, RoundingMode.HALF_UP);
                    break;
                default:
                    System.err.println("Engine: Unknown operator: " + pendingOperator);
                    return; // Don't change anything
            }

            // Calculation successful, update current operand
            currentOperand = result;

        } catch (ArithmeticException e) {
            System.err.println("Engine: Arithmetic error during calculation: " + e.getMessage());
            isInErrorState = true;
            currentOperand = BigDecimal.ZERO; // Reset the currentOperand when there is an error.
        } catch (Exception e) {
            System.err.println("Engine: Unexpected error during calculation: " + e.getMessage());
            isInErrorState = true;
            currentOperand = BigDecimal.ZERO; // Reset the currentOperand when there is an error.
        }

        // --- Reset state based on whether an error occurred ---
        if (!isInErrorState) {
            // Normal path
            pendingOperand = currentOperand; // Result is ready for next operation
            pendingOperator = null;
            isEnteringDigits = false;
            // System.out.println("Engine: Calculation performed. Result = " + currentOperand.toPlainString()); // Debugging
        } else {
            // Error path
            pendingOperator = null; // Clear pending state
            pendingOperand = null;
            isEnteringDigits = false; // Not entering digits after error
        }
    }
}