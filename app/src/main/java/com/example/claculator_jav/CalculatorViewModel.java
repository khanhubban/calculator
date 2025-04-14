package com.example.claculator_jav;

import androidx.lifecycle.LiveData; // Import LiveData
import androidx.lifecycle.MutableLiveData; // Import MutableLiveData
import androidx.lifecycle.ViewModel; // Import ViewModel

public class CalculatorViewModel extends ViewModel { // Extend ViewModel

    // --- LiveData for UI ---
    // MutableLiveData can be changed within the ViewModel
    private final MutableLiveData<String> _displayValue = new MutableLiveData<>("0");
    // LiveData is exposed publicly (read-only for the Activity)
    public final LiveData<String> displayValue = _displayValue;

    // Optional: LiveData for the expression display if you have one
    // private final MutableLiveData<String> _expressionValue = new MutableLiveData<>("");
    // public final LiveData<String> expressionValue = _expressionValue;

    // --- Instance of the Engine ---
    private final CalculatorEngine engine = new CalculatorEngine();

    // --- Public Methods for Activity to Call ---

    public void processDigit(String digit) {
        engine.inputDigit(digit);
        updateDisplay();
        // System.out.println("ViewModel: Processed Digit " + digit); // Debugging
    }

    public void processOperator(String operator) {
        engine.inputOperator(operator);
        updateDisplay(); // Update display to show the operand usually
        // System.out.println("ViewModel: Processed Operator " + operator); // Debugging
    }

    public void processDecimal() {
        engine.inputDecimal();
        updateDisplay();
        // System.out.println("ViewModel: Processed Decimal"); // Debugging
    }

    public void processEquals() {
        engine.calculateResult();
        updateDisplay();
        // System.out.println("ViewModel: Processed Equals"); // Debugging
    }

    public void processClear() {
        engine.clear();
        updateDisplay();
        // System.out.println("ViewModel: Processed Clear"); // Debugging
    }

    public void processBackspace() {
        engine.backspace();
        updateDisplay();
        // System.out.println("ViewModel: Processed Backspace"); // Debugging
    }

    public void processPercentage() {
        engine.calculatePercentage();
        updateDisplay();
        // System.out.println("ViewModel: Processed Percentage"); // Debugging
    }

    // --- Helper Method ---
    private void updateDisplay() {
        // Get the latest value from the engine and update LiveData
        String currentDisplay = engine.getDisplayValue();
        _displayValue.setValue(currentDisplay);

        // Optional: Update expression display LiveData if needed
        // _expressionValue.setValue(engine.getExpressionString()); // Assuming engine has such a method
        // System.out.println("ViewModel: Updating display to " + currentDisplay); // Debugging
    }

    // Constructor - called when the ViewModel is first created
    public CalculatorViewModel() {
        // Initialize LiveData
        updateDisplay(); // Set initial display value from engine's initial state
        // System.out.println("ViewModel: Initialized"); // Debugging
    }
}
