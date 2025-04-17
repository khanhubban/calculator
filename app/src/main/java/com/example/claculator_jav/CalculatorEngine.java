package com.example.claculator_jav;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class CalculatorEngine {

    // --- Constants ---
    private static final int MAX_DIGITS = 16;
    private static final int DIVISION_SCALE = 8;
    private static final String OPEN_PAREN = "(";
    private static final String CLOSE_PAREN = ")";

    // --- State Variables ---
    private BigDecimal currentOperand;
    private boolean isEnteringDigits;
    private boolean isInErrorState = false;
    private boolean displayTrailingDecimal = false; // For display formatting (e.g., "5.")
    private int parenthesisBalance = 0; // Track open parentheses

    // --- Stacks for calculation precedence ---
    private Stack<BigDecimal> valueStack;
    private Stack<String> operatorStack;

    // --- Constructor ---
    public CalculatorEngine() {
        valueStack = new Stack<>();
        operatorStack = new Stack<>();
        clear(); // Initialize the state
    }

    // --- Public Methods ---

    /**
     * Resets the engine to its initial state.
     */
    public void clear() {
        currentOperand = BigDecimal.ZERO;
        isEnteringDigits = false;
        isInErrorState = false;
        displayTrailingDecimal = false;
        parenthesisBalance = 0;
        valueStack.clear();
        operatorStack.clear();
        System.out.println("Engine: Cleared");
    }

    /**
     * Processes a digit input.
     * @param digit The digit entered ("0"-"9").
     */
    public void inputDigit(String digit) {
        if (isInErrorState) {
            clear(); // Start fresh after error
        }

        // Handle potential implicit multiplication (e.g., after ')') if needed
        // if (!operatorStack.isEmpty() && operatorStack.peek().equals(CLOSE_PAREN) && !isEnteringDigits) {
        //    inputOperator("×"); // Requires careful state management
        // }

        if (!isEnteringDigits) {
            currentOperand = BigDecimal.ZERO;
            isEnteringDigits = true;
            displayTrailingDecimal = false;
        }

        String currentText = currentOperand.toPlainString();

        // Prevent adding digits beyond the limit
        String plainDigits = currentText.replace("-", "").replace(".", "");
        if (!displayTrailingDecimal && plainDigits.length() >= MAX_DIGITS) {
            System.out.println("Engine: Max digits reached");
            return;
        }

        // Build the new operand string
        if (currentText.equals("0") && !displayTrailingDecimal) {
            currentText = digit; // Replace initial 0
        } else if (displayTrailingDecimal) {
            currentText = currentText + "." + digit; // Append after the virtual decimal
            displayTrailingDecimal = false; // Decimal is now real
        } else {
            currentText += digit; // Append digit
        }

        try {
            currentOperand = new BigDecimal(currentText);
        } catch (NumberFormatException e) {
            System.err.println("Engine: Error parsing digit input: " + e.getMessage());
            setErrorState("Invalid Number");
        }
    }

    /**
     * Processes a decimal point input.
     */
    public void inputDecimal() {
        if (isInErrorState) {
            clear();
        }

        if (!isEnteringDigits) {
            // Start a new number with "0."
            currentOperand = BigDecimal.ZERO;
            isEnteringDigits = true;
            displayTrailingDecimal = true;
        } else {
            String currentText = currentOperand.toPlainString();
            // Only add decimal if one doesn't exist and within digit limits
            if (!currentText.contains(".")) {
                String plainDigits = currentText.replace("-", "").replace(".", "");
                if (plainDigits.length() >= MAX_DIGITS) {
                    System.out.println("Engine: Max digits reached (decimal)");
                    return;
                }
                displayTrailingDecimal = true; // Indicate pending decimal for display
            } else {
                System.out.println("Engine: Decimal already exists");
            }
        }
    }

    /**
     * Processes an operator input (+, -, ×, ÷).
     * Handles operator precedence and stack calculations.
     * @param operator The operator string.
     */
    public void inputOperator(String operator) {
        if (isInErrorState) {
            System.out.println("Engine: Operator ignored (in error state)");
            return;
        }

        // Handle Operator Replacement: If the last input was also an operator (and not after '('), replace it.
        if (!isEnteringDigits() && !operatorStack.isEmpty() && !operatorStack.peek().equals(OPEN_PAREN)) {
            operatorStack.pop();
            operatorStack.push(operator);
            System.out.println("Engine: Replaced top operator with " + operator);
            isEnteringDigits = false;
            displayTrailingDecimal = false;
            return;
        }

        // If digits were being entered, push the completed number onto the value stack
        if (isEnteringDigits()) {
            valueStack.push(currentOperand);
            isEnteringDigits = false;
            displayTrailingDecimal = false; // Number is now complete
        } else if (valueStack.isEmpty() && operatorStack.isEmpty()) {
            // Handle unary operator at the start (e.g., "-5"): push 0 as the left operand.
            valueStack.push(BigDecimal.ZERO);
            System.out.println("Engine: Pushed initial 0 for unary operator.");
        } else if (!operatorStack.isEmpty() && operatorStack.peek().equals(OPEN_PAREN)) {
            // Handle unary operator after an open parenthesis (e.g., "(-5"): push 0.
            valueStack.push(BigDecimal.ZERO);
            System.out.println("Engine: Pushed 0 for unary operator after '('.");
        }

        // Process operators from the stack with higher or equal precedence
        while (!operatorStack.isEmpty() &&
                !operatorStack.peek().equals(OPEN_PAREN) && // Stop at '('
                getPrecedence(operatorStack.peek()) >= getPrecedence(operator)) {

            if (valueStack.size() < 2) {
                System.err.println("Engine: Insufficient operands for operator " + operatorStack.peek());
                setErrorState("Syntax Error");
                return;
            }
            if (!processTopOperator()) { // Calculate using the top operator
                return; // Stop if calculation failed
            }
        }

        // Push the new operator onto the stack
        operatorStack.push(operator);
        System.out.println("Engine: Pushed Operator " + operator);

        // Reset state for the next operand input
        isEnteringDigits = false;
        displayTrailingDecimal = false;
    }

    /**
     * Processes parenthesis input ("(" or ")").
     * @param parenthesis The parenthesis character.
     */
    public void inputParenthesis(String parenthesis) {
        if (isInErrorState) {
            System.out.println("Engine: Parenthesis ignored (in error state)");
            return;
        }

        if (parenthesis.equals(OPEN_PAREN)) {
            handleOpenParenthesis();
        } else if (parenthesis.equals(CLOSE_PAREN)) {
            handleCloseParenthesis();
        }
    }

    /** Handles the logic for an opening parenthesis "(". */
    private void handleOpenParenthesis() {
        // Handle implicit multiplication before '(': e.g., "5(" becomes "5*("
        if (isEnteringDigits() || (!operatorStack.isEmpty() && operatorStack.peek().equals(CLOSE_PAREN))) {
            if (isEnteringDigits()) {
                valueStack.push(currentOperand); // Push the number first
                isEnteringDigits = false;
                displayTrailingDecimal = false;
            }
            // Treat implicit multiplication like a normal operator input
            inputOperator("×");
        }

        operatorStack.push(OPEN_PAREN);
        parenthesisBalance++;
        isEnteringDigits = false; // Expecting number or unary operator next
        System.out.println("Engine: Pushed Open Parenthesis. Balance: " + parenthesisBalance);
    }

    /** Handles the logic for a closing parenthesis ")". */
    private void handleCloseParenthesis() {
        if (parenthesisBalance <= 0) {
            System.err.println("Engine: Error - Closing parenthesis without matching open parenthesis.");
            setErrorState("Mismatched )");
            return;
        }

        // If digits were being entered, push the number first
        if (isEnteringDigits()) {
            valueStack.push(currentOperand);
            isEnteringDigits = false;
            displayTrailingDecimal = false;
        } else if (!operatorStack.isEmpty() && operatorStack.peek().equals(OPEN_PAREN)) {
            // Error on empty parentheses "()"
            System.err.println("Engine: Error - Empty parentheses '()'");
            setErrorState("Empty ()");
            return;
        }

        // Process operators within the parentheses until the matching '(' is found
        while (!operatorStack.isEmpty() && !operatorStack.peek().equals(OPEN_PAREN)) {
            if (valueStack.size() < 2) {
                System.err.println("Engine: Insufficient operands while processing for ')'");
                setErrorState("Syntax Error");
                return;
            }
            if (!processTopOperator()) {
                return; // Stop if calculation fails
            }
        }

        // Pop the matching '('
        if (!operatorStack.isEmpty() && operatorStack.peek().equals(OPEN_PAREN)) {
            operatorStack.pop();
            parenthesisBalance--;
            System.out.println("Engine: Processed Close Parenthesis. Balance: " + parenthesisBalance);
        } else {
            // Should not happen if balance check was correct
            System.err.println("Engine: Error - Open parenthesis expected but not found.");
            setErrorState("Mismatched (");
            return;
        }

        isEnteringDigits = false; // Next input likely operator, equals, or another '('
        // Implicit multiplication after ')' (e.g., ")5" or ")(") is handled by the next input method
    }

    /**
     * Calculates the final result of the expression.
     */
    public void calculateResult() {
        if (isInErrorState) return;

        // Push the last entered operand if any
        if (isEnteringDigits) {
            valueStack.push(currentOperand);
            isEnteringDigits = false;
            displayTrailingDecimal = false;
        }

        // Check for unclosed parentheses
        if (parenthesisBalance > 0) {
            System.err.println("Engine: Error - Unclosed parentheses.");
            setErrorState("Mismatched (");
            return;
        }

        // Process all remaining operators on the stack
        while (!operatorStack.isEmpty()) {
            if (operatorStack.peek().equals(OPEN_PAREN)) {
                // Should have been caught by balance check earlier
                System.err.println("Engine: Error - Mismatched parenthesis found during final calculation.");
                setErrorState("Mismatched (");
                operatorStack.pop(); // Attempt to recover?
                return;
            }
            if (valueStack.size() < 2) {
                System.err.println("Engine: Insufficient operands during final calculation for operator " + operatorStack.peek());
                setErrorState("Syntax Error");
                return;
            }
            if (!processTopOperator()) {
                return; // Stop if calculation failed
            }
        }

        // Final result should be the only item left on the value stack
        if (valueStack.size() == 1 && operatorStack.isEmpty()) {
            currentOperand = valueStack.pop(); // Get the final result
            System.out.println("Engine: Final Result = " + formatBigDecimal(currentOperand));
        } else if (valueStack.isEmpty() && operatorStack.isEmpty()) {
            // Case: User presses '=' without any input or after clear. Keep currentOperand (usually 0)
            System.out.println("Engine: Equals pressed on empty state.");
        } else {
            // If stacks are not in the expected state, signal an error
            System.err.println("Engine: Error in final calculation. Stacks inconsistent.");
            System.err.println("Value Stack: " + valueStack);
            System.err.println("Op Stack: " + operatorStack);
            setErrorState("Calculation Error");
        }

        // Reset state for next calculation
        isEnteringDigits = false;
        displayTrailingDecimal = false;
        parenthesisBalance = 0; // Should be 0, reset for safety
    }

    /**
     * Applies percentage (divides by 100) to the current operand or last result.
     * Note: Needs refinement for complex precedence like 5 + 10%.
     */
    public void calculatePercentage() {
        if (isInErrorState) return;

        // Apply percentage to the number currently being entered
        if (isEnteringDigits) {
            try {
                BigDecimal hundred = BigDecimal.valueOf(100);
                currentOperand = currentOperand.divide(hundred, DIVISION_SCALE, RoundingMode.HALF_UP);
                displayTrailingDecimal = false; // Result is unlikely to need trailing decimal display
                System.out.println("Engine: Applied % to current operand: " + currentOperand.toPlainString());
            } catch (ArithmeticException e) {
                System.err.println("Engine: Arithmetic error during percentage: " + e.getMessage());
                setErrorState("Math Error");
            }
        } else if (!valueStack.isEmpty()) {
            // Apply percentage to the last calculated value on the stack
            try {
                BigDecimal topValue = valueStack.pop();
                BigDecimal hundred = BigDecimal.valueOf(100);
                BigDecimal result = topValue.divide(hundred, DIVISION_SCALE, RoundingMode.HALF_UP);
                valueStack.push(result);
                currentOperand = result; // Update display value to show the result
                System.out.println("Engine: Applied % to top of stack: " + result.toPlainString());
            } catch (ArithmeticException e) {
                System.err.println("Engine: Arithmetic error during percentage (stack): " + e.getMessage());
                setErrorState("Math Error");
            }
        } else {
            System.out.println("Engine: Percentage ignored (no operand available)");
        }
        // State after % should probably be !isEnteringDigits, ready for operator or '='
        isEnteringDigits = false;
        displayTrailingDecimal = false;
    }

    /**
     * Handles the backspace action. Deletes the last digit or pending decimal.
     * Note: Backspacing operators/parentheses might require more complex state reversal.
     */
    public void backspace() {
        if (isInErrorState) {
            // Require AC/Clear to recover from errors
            System.out.println("Engine: Backspace ignored (in error state)");
            return;
        }

        // Handle backspacing while entering digits
        if (isEnteringDigits) {
            if (displayTrailingDecimal) {
                displayTrailingDecimal = false; // Remove pending decimal point display
                System.out.println("Engine: Backspace removed trailing decimal display.");
                return; // Done
            }

            String currentText = currentOperand.toPlainString();
            if (currentText.length() > 0 && !currentText.equals("0")) {
                // Handle backspacing to zero or empty string
                if (currentText.length() == 1 || (currentText.startsWith("-") && currentText.length() == 2)) {
                    currentText = "0";
                } else {
                    // Remove last character
                    currentText = currentText.substring(0, currentText.length() - 1);
                    // Handle cases like removing "-" or the digit after "."
                    if (currentText.equals("-")) {
                        currentText = "0";
                    } else if (currentText.endsWith(".")) {
                        // If result is like "1.", remove the "." and set flag for display
                        currentText = currentText.substring(0, currentText.length() - 1);
                        displayTrailingDecimal = true;
                    }
                }

                try {
                    // Update operand from the modified text
                    currentOperand = new BigDecimal(currentText);
                    // isEnteringDigits remains true unless backspaced to 0? (Can stay true)
                } catch (NumberFormatException e) {
                    // Should not happen if logic is correct, but handle defensively
                    System.err.println("Engine: Error parsing after backspace: " + e.getMessage());
                    currentOperand = BigDecimal.ZERO;
                    isEnteringDigits = true;
                    displayTrailingDecimal = false;
                }
            } else {
                System.out.println("Engine: Backspace ignored (already 0)");
            }
            System.out.println("Engine: Backspace processed (digit). Current Operand = " + getDisplayValue());

        }
        // --- Optional: Handle backspacing operators/parentheses ---
        /*
        else if (!operatorStack.isEmpty()) {
            String popped = operatorStack.pop();
            System.out.println("Engine: Backspace popped operator/paren: " + popped);
            if (popped.equals(OPEN_PAREN)) {
                parenthesisBalance--;
            }
            // Reversing state fully after popping operator is complex
            // For simplicity, we might just pop and update currentOperand display
             if (!valueStack.isEmpty()) {
                currentOperand = valueStack.peek(); // Show last value on stack
            } else {
                currentOperand = BigDecimal.ZERO;
            }
            isEnteringDigits = false;
            displayTrailingDecimal = false;
        } */
        else {
            System.out.println("Engine: Backspace ignored (nothing to delete)");
        }
    }

    /**
     * Gets the value to be shown on the main display.
     * @return Formatted string representation of the current number or result, or "Error".
     */
    public String getDisplayValue() {
        if (isInErrorState) {
            return "Error"; // Consider specific error messages
        }

        String displayText;
        if (isEnteringDigits) {
            // Show the number currently being typed
            displayText = formatBigDecimal(currentOperand);
            // Append "." if a decimal was just pressed but no digits followed yet
            if (displayTrailingDecimal && !displayText.contains(".")) {
                displayText += ".";
            }
        } else if (!valueStack.isEmpty() || !operatorStack.isEmpty()) {
            // Show the last intermediate or final result stored in currentOperand
            displayText = formatBigDecimal(currentOperand);
        } else {
            // Default state (cleared or initial)
            displayText = formatBigDecimal(currentOperand); // Usually "0"
        }
        return displayText;
    }

    // --- Getters for state ---
    public boolean isEnteringDigits() { return isEnteringDigits; }
    public boolean isInErrorState() { return isInErrorState; }
    public int getParenthesisBalance() { return parenthesisBalance; }

    // --- Private Helper Methods ---

    /** Formats BigDecimal for display, removing trailing zeros. */
    private String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        // TODO: Add scientific notation for very large/small numbers if needed
        return value.stripTrailingZeros().toPlainString();
    }

    /** Sets the error state with a specific message. */
    private void setErrorState(String message) {
        System.err.println("Engine Error: " + message);
        isInErrorState = true;
        currentOperand = BigDecimal.ZERO; // Or store/display the message?
        valueStack.clear();
        operatorStack.clear();
        isEnteringDigits = false;
        displayTrailingDecimal = false;
        parenthesisBalance = 0;
        System.out.println("Engine: Entered Error State.");
    }

    /** Sets the error state with a default message. */
    private void setErrorState() {
        setErrorState("Calculation Error"); // Default message
    }

    /**
     * Gets the precedence level of an operator. Higher number means higher precedence.
     */
    private int getPrecedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "×": // Multiplication
            case "÷": // Division
                return 2;
            case OPEN_PAREN: // '(' on stack has lowest precedence during evaluation
                return 0;
            default:
                return 0; // Should not happen for valid operators
        }
    }

    /**
     * Pops the top operator and required operands from the stacks,
     * performs the calculation, and pushes the result back onto the value stack.
     * Updates currentOperand to the result.
     * @return true if successful, false if an error occurred (error state is set internally).
     */
    private boolean processTopOperator() {
        if (valueStack.size() < 2 || operatorStack.isEmpty()) {
            System.err.println("Engine: Cannot process operator, insufficient operands or empty operator stack.");
            setErrorState("Syntax Error");
            return false;
        }

        try {
            String op = operatorStack.pop();
            // Pop operands in reverse order (right then left)
            BigDecimal rightOperand = valueStack.pop();
            BigDecimal leftOperand = valueStack.pop();
            BigDecimal result = BigDecimal.ZERO;

            System.out.println("Engine: Processing: " + formatBigDecimal(leftOperand) + " " + op + " " + formatBigDecimal(rightOperand));

            switch (op) {
                case "+":
                    result = leftOperand.add(rightOperand);
                    break;
                case "-":
                    result = leftOperand.subtract(rightOperand);
                    break;
                case "×":
                    result = leftOperand.multiply(rightOperand);
                    break;
                case "÷":
                    if (rightOperand.compareTo(BigDecimal.ZERO) == 0) {
                        System.err.println("Engine: Division by zero!");
                        setErrorState("Division by Zero");
                        return false;
                    }
                    result = leftOperand.divide(rightOperand, DIVISION_SCALE, RoundingMode.HALF_UP);
                    break;
                default:
                    System.err.println("Engine: Unknown operator popped: " + op);
                    setErrorState("Internal Error");
                    return false;
            }

            // TODO: Check for potential overflow if needed based on MAX_DIGITS or value limits

            valueStack.push(result); // Push result back onto value stack
            currentOperand = result; // Update currentOperand to reflect intermediate/final result
            System.out.println("Engine: Pushed Result " + formatBigDecimal(result));
            return true; // Success

        } catch (ArithmeticException e) {
            System.err.println("Engine: Arithmetic error during stack calculation: " + e.getMessage());
            setErrorState("Math Error");
            return false;
        } catch (Exception e) { // Catch other potential errors like EmptyStackException
            System.err.println("Engine: Unexpected error during stack calculation: " + e.getMessage());
            setErrorState("Internal Error");
            return false;
        }
    }

    /**
     * Generates a string representation of the expression currently being built,
     * based on the state of the value and operator stacks.
     * Used for the secondary display.
     * @return A string showing the expression preview.
     */
    public String getExpressionPreview() {
        // Note: This implementation reconstructs from stacks and might need refinement
        // for complex cases or perfect visual consistency with all input sequences.
        System.out.println("--- getExpressionPreview START ---"); // Keep START/END for debugging hangs
        StringBuilder preview = new StringBuilder();
        List<Object> elements = new ArrayList<>(); // Combine numbers and operators in order

        // Make copies to not modify the actual state stacks
        Stack<String> tempOperatorStack = new Stack<>();
        tempOperatorStack.addAll(operatorStack);
        Stack<BigDecimal> tempValueStack = new Stack<>();
        tempValueStack.addAll(valueStack);

        // Reverse the temp stacks to reconstruct in approximate input order
        Collections.reverse(tempOperatorStack);
        Collections.reverse(tempValueStack);

        int valIndex = 0;
        int opIndex = 0;

        // Interleave values and operators (simplified logic, may need adjustment)
        // This loop needs careful testing for edge cases
        while (opIndex < tempOperatorStack.size() || valIndex < tempValueStack.size()) {
            // Prioritize adding value if available and seems appropriate
            if (valIndex < tempValueStack.size()) {
                // Simple interleaving: Add value, then operator if available
                elements.add(tempValueStack.get(valIndex++));
                if(opIndex < tempOperatorStack.size()) {
                    // Avoid adding operator immediately after '('? Check operator type?
                    elements.add(tempOperatorStack.get(opIndex++));
                }
            } else if (opIndex < tempOperatorStack.size()) {
                // Add remaining operators if no more values
                elements.add(tempOperatorStack.get(opIndex++));
            } else {
                // Should not happen if loop condition is correct
                break;
            }
        }

        // Format the interleaved elements into a string
        for (int i = 0; i < elements.size(); i++) {
            Object element = elements.get(i);
            if (element instanceof BigDecimal) {
                preview.append(formatBigDecimal((BigDecimal) element));
            } else {
                // Append operator/parenthesis, potentially adding space before/after?
                // Add space after operators/parens unless it's the last element
                preview.append(element);
                if (i < elements.size() - 1) {
                    preview.append(" "); // Add space for readability
                }
            }
            // Add space between number and operator? Improves readability but needs care.
            if (i < elements.size() - 1 && element instanceof BigDecimal && elements.get(i+1) instanceof String) {
                // Only add space if the next element is an operator/paren string
                if (!elements.get(i+1).equals(")")) { // Avoid space before closing paren? "5 )" vs "5)"
                    preview.append(" ");
                }
            }
        }


        // If currently entering digits, append the live input
        if (isEnteringDigits()) {
            String currentDisplayValue = getDisplayValue(); // Get formatted current input
            // Append with space if preview isn't empty and doesn't end with non-numeric requiring space
            if (preview.length() > 0 && !preview.toString().endsWith("( ") && !preview.toString().endsWith(" ")) {
                preview.append(" ");
            }
            preview.append(currentDisplayValue);
        }

        System.out.println("--- getExpressionPreview END ---"); // Keep START/END for debugging hangs
        return preview.toString().trim(); // Trim leading/trailing whitespace
    }
}