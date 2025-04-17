package com.example.claculator_jav;

import android.content.Context;
import android.os.Bundle;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.claculator_jav.databinding.ActivityMainBinding;

import java.util.List;
import java.util.Locale;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CalculatorViewModel viewModel;
    private DecimalFormat displayFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- SET UP THE TOOLBAR ---
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        // --- END TOOLBAR SETUP ---

        // --- Formatter Setup ---
        displayFormatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        displayFormatter.setMinimumFractionDigits(0);
        displayFormatter.setMaximumFractionDigits(8);
        displayFormatter.setGroupingUsed(false);
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
            System.out.println("MainActivity: History updated, size = " + (historyList != null ? historyList.size() : 0));
        });

        // --- Set Click Listeners ---
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
        binding.expressionDisplay.setOnClickListener(null);

    }

    // --- Inflate Options Menu ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // --- Handle Options Menu Item Clicks ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_history) {
            showHistoryDialog();
            return true;
        } else if (itemId == R.id.action_change_theme) {
            toggleTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        // Create a custom ArrayAdapter to set text color
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.select_dialog_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);

                // Set the text color based on the current theme
                int textColor = ContextCompat.getColor(getContext(), R.color.calc_black_text); // Default to black
                textView.setTextColor(textColor);

                return view;
            }
        };

        builder.setAdapter(adapter, null);
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
        } else {
            // Currently light (or unspecified), switch to dark
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        recreate();
    }
}