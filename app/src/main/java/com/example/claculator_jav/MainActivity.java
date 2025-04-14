package com.example.claculator_jav;

import android.os.Bundle;
import android.view.View; // Import View for OnClickListener if needed, though lambdas are cleaner

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer; // Import Observer
import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider

// Import the generated ViewBinding class
import com.example.claculator_jav.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Declare the binding variable
    private ActivityMainBinding binding;
    // Declare the ViewModel variable
    private CalculatorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- View Binding Setup ---
        // Inflate the layout using View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // Set the content view to the root of the binding
        setContentView(binding.getRoot());

        // --- ViewModel Setup ---
        // Get the ViewModel instance
        viewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);

        // --- Observe LiveData ---
        // Observe the displayValue LiveData from the ViewModel
        viewModel.displayValue.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String display) {
                // Update the display TextView whenever the LiveData changes
                binding.display.setText(display);
                // Optional: Update expression display if you have LiveData for it
                // binding.expressionDisplay.setText(expression);
            }
        });

        // Optional: Observe the expressionValue LiveData if you created it
         /*
         viewModel.expressionValue.observe(this, new Observer<String>() {
             @Override
             public void onChanged(String expression) {
                 binding.expressionDisplay.setText(expression);
             }
         });
         */


        // --- Set Click Listeners ---
        // Use View Binding to access buttons and set listeners
        // Using Lambda expressions (->) for conciseness

        // Digits
        binding.button0.setOnClickListener(v -> viewModel.processDigit("0"));
        binding.button1.setOnClickListener(v -> viewModel.processDigit("1"));
        binding.button2.setOnClickListener(v -> viewModel.processDigit("2"));
        binding.button3.setOnClickListener(v -> viewModel.processDigit("3"));
        binding.button4.setOnClickListener(v -> viewModel.processDigit("4"));
        binding.button5.setOnClickListener(v -> viewModel.processDigit("5"));
        binding.button6.setOnClickListener(v -> viewModel.processDigit("6"));
        binding.button7.setOnClickListener(v -> viewModel.processDigit("7"));
        binding.button8.setOnClickListener(v -> viewModel.processDigit("8"));
        binding.button9.setOnClickListener(v -> viewModel.processDigit("9"));

        // Operators
        binding.buttonAdd.setOnClickListener(v -> viewModel.processOperator("+"));
        binding.buttonSubtract.setOnClickListener(v -> viewModel.processOperator("-"));
        binding.buttonMultiply.setOnClickListener(v -> viewModel.processOperator("×")); // Or "*" if your engine uses that
        binding.buttonDivide.setOnClickListener(v -> viewModel.processOperator("÷")); // Or "/" if your engine uses that

        // Actions
        binding.buttonEquals.setOnClickListener(v -> viewModel.processEquals());
        binding.buttonAc.setOnClickListener(v -> viewModel.processClear());
        binding.buttonDecimal.setOnClickListener(v -> viewModel.processDecimal());
        binding.buttonBackspace.setOnClickListener(v -> viewModel.processBackspace());
        binding.buttonPercent.setOnClickListener(v -> viewModel.processPercentage());

        // Parentheses (if you implement the logic in ViewModel/Engine)
        // binding.buttonOpenParentheses.setOnClickListener(v -> viewModel.processParenthesis("("));
        // binding.buttonCloseParentheses.setOnClickListener(v -> viewModel.processParenthesis(")"));

    }

    // --- NO MORE CALCULATION LOGIC OR STATE VARIABLES HERE! ---
    // The Activity is now much simpler.
}