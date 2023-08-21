package com.example.calculater.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.calculater.SharedPreferencesHelper;
import com.example.calculater.adapters.CalculationHistoryAdapter;
import com.example.calculater.databinding.ActivityMainBinding;
import com.example.calculater.utils.CalculationHistory;

import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private static final char ADDITION = '+';
    private static final char SUBTRACTION = '-';
    private static final char MULTIPLICATION = '*';
    private static final char DIVISION = '/';
    private final List<CalculationHistory> calculationHistory = new ArrayList<>();
    String password = "";
    SharedPreferencesHelper sharedPreferencesHelper;
    Vibrator vb;
    private float value1 = Float.NaN;
    private float value2;
    private char CURRENT_ACTION;

    private CalculationHistoryAdapter historyAdapter;

    @Override
    ActivityMainBinding getLayout() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyAdapter = new CalculationHistoryAdapter();
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        clickButton();
        buttonImplement();

        binding.btnClear.setOnLongClickListener(v -> {
            binding.result.setText("");
            binding.solution.setText("");
            return true;
        });

        binding.rvHistory.setAdapter(historyAdapter);
    }


    // Implement clicks button
    @SuppressLint("ClickableViewAccessibility")
    public void clickButton() {
        binding.zeroBtn.setOnClickListener(v -> {
            type("0");
        });
        binding.btn1.setOnClickListener(v -> {
            type("1");
        });
        binding.btn2.setOnClickListener(v -> {
            type("2");
        });
        binding.btn3.setOnClickListener(v -> {
            type("3");
        });
        binding.btn4.setOnClickListener(v -> {
            type("4");
        });
        binding.btn5.setOnClickListener(v -> {
            type("5");
        });
        binding.btn6.setOnClickListener(v -> {
            type("6");
        });
        binding.btn7.setOnClickListener(v -> {
            type("7");
        });
        binding.btn8.setOnClickListener(v -> {
            type("8");
        });
        binding.btn9.setOnClickListener(v -> {
            type("9");
        });
        binding.btnDot.setOnClickListener(v -> {
            type(".");
        });
        binding.result.setOnTouchListener((v, event) -> {
            v.onTouchEvent(event);
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            return true;
        });

    }


    private void type(String value) {
        binding.solution.append(value);
        vb.vibrate(90);
        calculate();
    }

    public void buttonImplement() {

        binding.btnAdd.setOnClickListener(v -> {
            binding.solution.append("+");
            vb.vibrate(90);
            computeCalculation();
            CURRENT_ACTION = ADDITION;
            binding.result.setText(null);

        });

        binding.btnSubtract.setOnClickListener(v -> {
            binding.solution.append("-");
            vb.vibrate(90);
            computeCalculation();
            CURRENT_ACTION = SUBTRACTION;
            binding.result.setText(null);
        });

        binding.btnMultiply.setOnClickListener(v -> {
            binding.solution.append("*");
            vb.vibrate(90);
            computeCalculation();
            CURRENT_ACTION = MULTIPLICATION;
            binding.result.setText(null);
        });

        binding.btnDivide.setOnClickListener(v -> {
            binding.solution.append("/");
            vb.vibrate(90);
            computeCalculation();
            CURRENT_ACTION = DIVISION;
            binding.result.setText(null);
        });

        if (sharedPreferencesHelper.getString("password", "").isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter Your Password", Toast.LENGTH_SHORT).show();
        } else {
            binding.result.setText("");
        }

        binding.btnEqual.setOnClickListener(v -> {
            vb.vibrate(90);
            String expression = binding.solution.getText().toString();
            if (containsOperator(expression)) {
                equalPressed();
            } else {
                if (sharedPreferencesHelper.getString("password", "").isEmpty()) {

                    if (TextUtils.isEmpty(expression)) {
                        Toast.makeText(MainActivity.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
                    } else {
                        if (password.isEmpty()) {
                            password = expression;
                            binding.result.setText("");
                            Toast.makeText(MainActivity.this, "Re_Enter Your Password", Toast.LENGTH_SHORT).show();
                        } else if (password.equals(expression)) {
                            sharedPreferencesHelper.saveString("password", password);
                            //Toast.makeText(MainActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, UnlockActivity.class);
                            startActivity(intent);
                        } else
                            Toast.makeText(MainActivity.this, "Password Not Match", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    if (sharedPreferencesHelper.getString("password", "").equals(expression)) {
                        Intent intent = new Intent(MainActivity.this, UnlockActivity.class);
                        intent.putExtra("title", "Enter password");
                        startActivity(intent);
                        binding.result.setText("");
                    } else {
                        try {
                            computeCalculation();
                            binding.result.setText(String.valueOf(value1));
                            value1 = Float.NaN;
                            CURRENT_ACTION = '\0';
                        } catch (Exception e) {
                            e.getStackTrace();
                        }

                    }
                }
            }
        });

        binding.btnClear.setOnClickListener(v -> {
            vb.vibrate(90);

            if (binding.result.getText().length() > 0) {
                CharSequence currentText = binding.result.getText();
                binding.result.setText(currentText.subSequence(0, currentText.length() - 1));
            }
            if (binding.solution.getText().length() > 0) {
                CharSequence currentText = binding.solution.getText();
                binding.solution.setText(currentText.subSequence(0, currentText.length() - 1));
            } else {
                value1 = Float.NaN;
                value2 = Float.NaN;
                binding.result.setText("");
            }
        });


        binding.btnAc.setOnClickListener(v -> {
            vb.vibrate(90);
            binding.result.setText("");
            binding.solution.setText("");

        });

        binding.btnSin.setOnClickListener(v -> {
            vb.vibrate(90);
            if (TextUtils.isEmpty(binding.result.getText().toString())) {
                Toast.makeText(MainActivity.this, "Please enter the value", Toast.LENGTH_SHORT).show();
            } else {
                String input = binding.result.getText().toString();
                double inputValue = Double.parseDouble(input);
                double result = Math.sin(inputValue);
                binding.result.setText(String.valueOf(result));
            }
        });

        binding.btnCos.setOnClickListener(v -> {
            vb.vibrate(90);
            if (TextUtils.isEmpty(binding.result.getText().toString())) {
                Toast.makeText(MainActivity.this, "Please enter the value", Toast.LENGTH_SHORT).show();
            } else {

                String input = binding.result.getText().toString();
                double inputValue = Double.parseDouble(input);
                double result = Math.cos(inputValue);
                binding.result.setText(String.valueOf(result));
            }
        });

        binding.btnTan.setOnClickListener(v -> {
            vb.vibrate(90);
            if (TextUtils.isEmpty(binding.result.getText().toString())) {
                Toast.makeText(MainActivity.this, "Please enter the value", Toast.LENGTH_SHORT).show();
            } else {
                String input = binding.result.getText().toString();
                double inputValue = Double.parseDouble(input);
                double result = Math.tan(inputValue);
                binding.result.setText(String.valueOf(result));
            }
        });

        binding.btnE.setOnClickListener(v -> {
            vb.vibrate(90);
            double eValue = Math.E;
            binding.result.setText(String.valueOf(eValue));
        });

    }


    private void computeCalculation() {
        try {
            if (!Float.isNaN(value1)) {
                value2 = Float.parseFloat(binding.result.getText().toString());
                binding.result.setText("");

                Log.d("MainActivityType", String.valueOf(CURRENT_ACTION));
                Log.d("MainActivityType", " value1 " + value1);
                Log.d("MainActivityType", " value2 " + value2);

                if (CURRENT_ACTION == ADDITION) value1 += value2;
                else if (CURRENT_ACTION == SUBTRACTION) value1 -= value2;
                else if (CURRENT_ACTION == MULTIPLICATION) value1 *= value2;
                else if (CURRENT_ACTION == DIVISION) value1 /= value2;

            } else {
                try {
                    value1 = Float.parseFloat(binding.result.getText().toString());
                } catch (NumberFormatException ignored) {
                }

            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void calculate() {
        String expression = binding.solution.getText().toString();
        if (containsOperator(expression)) {
            ExpressionBuilder builder = new ExpressionBuilder(expression);
            double result = builder.build().evaluate();
            binding.result.setText(String.valueOf(result));
        } else binding.result.setText("");
    }

    private void equalPressed() {
        String expression = binding.solution.getText().toString();
        ExpressionBuilder builder = new ExpressionBuilder(expression);
        double result = builder.build().evaluate();
        binding.solution.setText(String.valueOf(result));
        binding.result.setText("");
    }

    private boolean containsOperator(String expression) {
        return expression.contains("*") || expression.contains("+") || expression.contains("/") || expression.contains("-");
    }


}