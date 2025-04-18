# calculator
# Calculator App

This is a simple calculator application for Android. It supports basic arithmetic operations, parentheses, and calculation history.

## Features

* **Basic Arithmetic:** Addition, subtraction, multiplication, and division.
* **Parentheses:** Handles calculations with nested parentheses.(Needs fix)
* **Percentage Calculation:** Calculates percentages.
* **Clear and Backspace:** Clear the display or delete the last input.
* **Calculation History:** Maintains a history of calculations, which can be viewed and cleared.
* **Theme Switching:** Users can toggle between light and dark themes.
* **Haptic Feedback:** Provides haptic feedback on button presses.

## Architecture

The app follows the Model-View-ViewModel (MVVM) architecture to separate concerns:

* **View:** The `MainActivity` and layout XML files (`activity_main.xml`, `activity_splash.xml`) handle the user interface and user interactions.
* **ViewModel:** The `CalculatorViewModel` manages the app's data and logic, and it updates the View through `LiveData`.
* **Model:** The `CalculatorEngine` performs the core calculation logic.

## Key Components

* **`CalculatorEngine`:**
    * Performs the arithmetic calculations.
    * Manages operator precedence and parentheses.
    * Handles errors during calculation (e.g., division by zero).
    * Provides methods for inputting digits, operators, parentheses, and actions (clear, backspace, equals, percentage).
    * Keeps track of the expression and calculation state.
* **`CalculatorViewModel`:**
    * Provides `LiveData` to observe the display value, secondary display (expression preview), and calculation history.
    * Uses an `ExecutorService` to perform calculations on a background thread, ensuring the UI remains responsive.
    * Updates the `LiveData` on the main thread to reflect calculation results.
    * Manages the calculation history.
* **`MainActivity`:**
    * Sets up the user interface using `ActivityMainBinding` for view binding.
    * Observes the `LiveData` from the `CalculatorViewModel` to update the display.
    * Handles user input by calling methods on the `CalculatorViewModel`.
    * Implements the options menu for viewing history and changing the theme.
    * Displays the calculation history in an `AlertDialog`.
    * Toggles between light and dark themes.
* **`SplashActivity`:**
    * Displays a splash screen when the app is launched.
    * Uses a theme (`SplashTheme`) to hide the action bar.

##  Additional Notes

* The app uses AndroidX libraries.
* Kotlin is used for the Gradle build files (`build.gradle.kts`, `settings.gradle.kts`).
* The app supports vibration using the `VIBRATE` permission.
* The UI is designed to adapt to different screen sizes.

##  To-Do

* Add more advanced calculator functions (e.g., trigonometric functions, logarithms).
* Implement more robust error handling and user feedback.
* Improve the UI design and user experience.
* Add unit tests.
* Localize the app for different languages.
* Fix nested parentheses
