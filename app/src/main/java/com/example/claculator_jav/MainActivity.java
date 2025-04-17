package com.example.claculator_jav;

import android.content.Context; // Keep if Vibrator used
import android.os.Build; // Keep if Vibrator used
import android.os.Bundle;
import android.os.VibrationEffect; // Keep if Vibrator used
import android.os.Vibrator; // Keep if Vibrator used
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.claculator_jav.databinding.ActivityMainBinding;

import java.math.BigDecimal; // Keep if formatting used
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CalculatorViewModel viewModel;
    private DecimalFormat displayFormatter; // Keep if used

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- SET UP THE TOOLBAR ---
        Toolbar toolbar = binding.toolbar; // Use view binding to get the toolbar
        setSupportActionBar(toolbar);      // Set the toolbar as the action bar
        // --- END TOOLBAR SETUP ---


        // --- Formatter Setup (Keep if needed) ---
        displayFormatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US); // Or Locale.getDefault()
        displayFormatter.setMinimumFractionDigits(0);
        displayFormatter.setMaximumFractionDigits(8); // Max digits after decimal
        displayFormatter.setGroupingUsed(false); // Optional: disable thousands separators
        // --- End Formatter Setup ---


        viewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);

        // --- Observe LiveData ---
        viewModel.displayValue.observe(this, display -> {
            binding.display.setText(display);
        });
        viewModel.secondaryDisplayValue.observe(this, expression -> {
            binding.expressionDisplay.setText(expression);
        });
        viewModel.history.observe(this, historyList -> {
            System.out.println("MainActivity: History updated, size = " + (historyList != null ? historyList.size() : 0)); // Debugging
        });

        // --- Set Click Listeners (Button listeners remain unchanged) ---
        // Digits
        binding.button0.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDigit("0"); });
        binding.button1.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDigit("1"); });
        binding.button2.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDigit("2"); });
        binding.button3.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDigit("3"); });
        binding.button4.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDigit("4"); });
        binding.button5.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDigit("5"); });
        binding.button6.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDigit("6"); });
        binding.button7.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDigit("7"); });
        binding.button8.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDigit("8"); });
        binding.button9.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDigit("9"); });

        // Operators
        binding.buttonAdd.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processOperator("+"); });
        binding.buttonSubtract.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processOperator("-"); });
        binding.buttonMultiply.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processOperator("×"); });
        binding.buttonDivide.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processOperator("÷"); });

        // Actions
        binding.buttonEquals.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processEquals(); });
        binding.buttonAc.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS); viewModel.processClear(); });
        binding.buttonDecimal.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processDecimal(); });
        binding.buttonBackspace.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processBackspace(); });
        binding.buttonPercent.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.processPercentage(); });
        binding.buttonParentheses.setOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            viewModel.processParenthesis("(");
        });


        // --- REMOVE Listener for History from expressionDisplay ---
        binding.expressionDisplay.setOnClickListener(null); // Remove the old listener

    } // onCreate ends here

    // --- Inflate Options Menu ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu); // Inflate the menu resource
        return true; // Return true to display the menu
    }

    // --- Handle Options Menu Item Clicks ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId(); // Get the item ID once

        if (itemId == R.id.action_history) {
            showHistoryDialog(); // Handle History click
            return true; // Indicate the click was handled
        } else if (itemId == R.id.action_change_theme) {
            // Handle Change Theme click
            toggleTheme(); // Call method to handle theme change
            return true; // Indicate the click was handled
        }
        // Handle other menu items here if you add more

        return super.onOptionsItemSelected(item); // Default handling for unhandled items
    }


    // --- Method to Show History Dialog (Keep this as is) ---
    private void showHistoryDialog() {
        List<String> historyList = viewModel.history.getValue();

        if (historyList == null || historyList.isEmpty()) {
            Toast.makeText(this, "History is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final CharSequence[] items = historyList.toArray(new CharSequence[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Calculation History");

        // Create an ArrayAdapter to customize text color
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.select_dialog_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(R.color.calc_white_text)); // Or a color that suits your theme
                return view;
            }
        };

        builder.setAdapter(adapter, null); // Use the custom adapter

        builder.setNegativeButton("Clear History", (dialog, which) -> {
            viewModel.clearHistory();
            Toast.makeText(this, "History Cleared", Toast.LENGTH_SHORT).show();
        });
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // --- Method to Toggle Theme ---
    private void toggleTheme() {
        // Get the current night mode setting
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        // Toggle between Light and Dark mode
        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            // Currently dark, switch to light
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            // TODO: Save preference (e.g., SharedPreferences) -> MODE_NIGHT_NO
        } else {
            // Currently light (or unspecified), switch to dark
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            // TODO: Save preference (e.g., SharedPreferences) -> MODE_NIGHT_YES
        }

        // *** UNCOMMENT this line to make the theme change apply immediately ***
        recreate();

        // Remove or comment out the Toast message, as the change is now instant
        // Toast.makeText(this, "Theme will update on next app restart", Toast.LENGTH_SHORT).show();
    }
}