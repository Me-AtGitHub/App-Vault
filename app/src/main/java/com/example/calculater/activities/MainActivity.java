package com.example.calculater.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calculater.R;
import com.example.calculater.SharedPreferencesHelper;
import com.example.calculater.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private final char ADDITION = '+';
    private final char SUBTRACTION = '-';
    ;
    private final char MULTIPLICATION = '*';
    private final char DIVISION = '/';
    TextView editText;
    TextView solution;
    Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btnDot, btnAdd, btnSubtract, btnMultiply, btnDivide, btnEqual, btnClear, btnAc;
    Button btnSin, btnCos, btnTan, btnE;
    String password = "";
    SharedPreferencesHelper sharedPreferencesHelper;
    private float value1 = Float.NaN;
    private float value2;
    private char CURRENT_ACTION;

    @Override
    ActivityMainBinding getLayout() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        findId();
        clickButton();
        buttonImplement();

        btnClear.setOnLongClickListener(v -> {
            editText.setText("");
            solution.setText("");
            return true;
        });
    }

    /// Implement Id
    public void findId() {
        editText = findViewById(R.id.result);
        btn0 = findViewById(R.id.zeroBtn);
        btn1 = findViewById(R.id.Btn1);
        btn2 = findViewById(R.id.Btn2);
        btn3 = findViewById(R.id.Btn3);
        btn4 = findViewById(R.id.Btn4);
        btn5 = findViewById(R.id.Btn5);
        btn6 = findViewById(R.id.Btn6);
        btn7 = findViewById(R.id.Btn7);
        btn8 = findViewById(R.id.Btn8);
        btn9 = findViewById(R.id.Btn9);
        btnDot = findViewById(R.id.dotBtn);
        btnAdd = findViewById(R.id.plusBtn);
        btnSubtract = findViewById(R.id.minBtn);
        btnMultiply = findViewById(R.id.multiplyBtn);
        btnDivide = findViewById(R.id.divideBtn);
        btnEqual = findViewById(R.id.equalBtn);
        btnClear = findViewById(R.id.backBtn);
        btnAc = findViewById(R.id.acBtn);
        btnSin = findViewById(R.id.sinBtn);
        btnCos = findViewById(R.id.cosBtn);
        btnTan = findViewById(R.id.tanBtn);
        btnE = findViewById(R.id.eBtn);
        solution = findViewById(R.id.solution);

    }

    // Implement clicks button
    public void clickButton() {

        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solution.setText(solution.getText() + "0");
                editText.setText(editText.getText() + "0");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
            }
        });
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(editText.getText() + "1");
                solution.setText(solution.getText() + "1");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(editText.getText() + "2");
                solution.setText(solution.getText() + "2");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(editText.getText() + "3");
                solution.setText(solution.getText() + "3");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                editText.setText(editText.getText() + "4");
                solution.setText(solution.getText() + "4");
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(editText.getText() + "5");
                solution.setText(solution.getText() + "5");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
            }
        });
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(editText.getText() + "6");
                solution.setText(solution.getText() + "6");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
            }
        });
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(editText.getText() + "7");
                solution.setText(solution.getText() + "7");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
            }
        });
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(editText.getText() + "8");
                solution.setText(solution.getText() + "8");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
            }
        });
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(editText.getText() + "9");
                solution.setText(solution.getText() + "9");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
            }
        });
        btnDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(editText.getText() + ".");
                solution.setText(solution.getText() + ".");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
            }
        });
        editText.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
        });

    }

    // Implement addition subtract and more buttons

    public void buttonImplement() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solution.append("+");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                computeCalculation();
                CURRENT_ACTION = ADDITION;
                editText.setText(null);

            }
        });
        btnSubtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solution.append("-");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                computeCalculation();
                CURRENT_ACTION = SUBTRACTION;
                editText.setText(null);
            }
        });
        btnMultiply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solution.append("*");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                computeCalculation();
                CURRENT_ACTION = MULTIPLICATION;
                editText.setText(null);
            }
        });
        btnDivide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solution.append("/");
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                computeCalculation();
                CURRENT_ACTION = DIVISION;
                editText.setText(null);
            }
        });

        if (sharedPreferencesHelper.getString("password", "").isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter Your Password", Toast.LENGTH_SHORT).show();
        } else {
            editText.setText("");
        }

        btnEqual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                if (sharedPreferencesHelper.getString("password", "").isEmpty()) {
                    if (TextUtils.isEmpty(editText.getText().toString())) {
                        Toast.makeText(MainActivity.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
                    } else {
                        if (password.isEmpty()) {
                            password = editText.getText().toString();
                            editText.setText("");
                            //editText.setText("ReEnter your password");
                            Toast.makeText(MainActivity.this, "Re_Enter Your Password", Toast.LENGTH_SHORT).show();
                        } else if (password.equals(editText.getText().toString())) {
                            sharedPreferencesHelper.saveString("password", password);
                            //Toast.makeText(MainActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, UnlockActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "Password Not Match", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(MainActivity.this, "Re_Enter Your Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (sharedPreferencesHelper.getString("password", "").equals(editText.getText().toString())) {
                        //Toast.makeText(MainActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, UnlockActivity.class);
                        intent.putExtra("title", "Enter password");
                        startActivity(intent);
                        editText.setText("");
                    } else {
                        //editText.setText("");
                        try {
                            computeCalculation();
                            editText.setText(String.valueOf(value1));
                            value1 = Float.NaN;
                            CURRENT_ACTION = '\0';
                        } catch (Exception e) {
                            e.getStackTrace();
                        }

                    }
                }

            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);

                if (editText.getText().length() > 0) {
                    CharSequence currentText = editText.getText();
                    editText.setText(currentText.subSequence(0, currentText.length() - 1));

                }
                if (solution.getText().length() > 0) {
                    CharSequence currentText = solution.getText();
                    solution.setText(currentText.subSequence(0, currentText.length() - 1));
                } else {

                    value1 = Float.NaN;
                    value2 = Float.NaN;
                    editText.setText("");
                }
            }
        });


        btnAc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                editText.setText("");
                solution.setText("");

            }
        });
        btnSin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                if (TextUtils.isEmpty(editText.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter the value", Toast.LENGTH_SHORT).show();
                } else {
                    String input = editText.getText().toString();
                    double inputValue = Double.parseDouble(input);
                    double result = Math.sin(inputValue);
                    editText.setText(String.valueOf(result));
                }
            }
        });
        btnCos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                if (TextUtils.isEmpty(editText.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter the value", Toast.LENGTH_SHORT).show();
                } else {

                    String input = editText.getText().toString();
                    double inputValue = Double.parseDouble(input);
                    double result = Math.cos(inputValue);
                    editText.setText(String.valueOf(result));
                }
            }
        });
        btnTan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                if (TextUtils.isEmpty(editText.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter the value", Toast.LENGTH_SHORT).show();
                } else {
                    String input = editText.getText().toString();
                    double inputValue = Double.parseDouble(input);
                    double result = Math.tan(inputValue);
                    editText.setText(String.valueOf(result));
                }
            }
        });
        btnE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(90);
                double eValue = Math.E;
                editText.setText(String.valueOf(eValue));
            }
        });

    }

    // Implement calculation code here
    // Implement calculation code here
    // Implement calculation code hereßðð¯ß¯
    private void computeCalculation() {
        try {
            if (!Float.isNaN(value1)) {
                value2 = Float.parseFloat(editText.getText().toString());
                editText.setText("");

                if (CURRENT_ACTION == ADDITION)
                    value1 += value2;
                else if (CURRENT_ACTION == SUBTRACTION)
                    value1 -= value2;
                else if (CURRENT_ACTION == MULTIPLICATION)
                    value1 *= value2;
                else if (CURRENT_ACTION == DIVISION)
                    value1 /= value2;

            } else {
                try {
                    value1 = Float.parseFloat(editText.getText().toString());
                } catch (NumberFormatException ignored) {
                }

            }
        } catch (Exception e) {
            //(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
            e.getStackTrace();
        }
    }


}