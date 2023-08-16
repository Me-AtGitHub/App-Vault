package com.example.calculater.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calculater.R;
import com.example.calculater.SharedPreferencesHelper;
import com.example.calculater.databinding.ActivityRecoveryQuestionBinding;

public class RecoveryQuestionActivity extends BaseActivity<ActivityRecoveryQuestionBinding> {
    TextView text1, text2, text3;
    ImageView awro;
    EditText edit1, edit2, edit3;

    String editText1, editText2, editText3;
    Button button;
    private SharedPreferencesHelper sharedPreferences;

    @Override
    ActivityRecoveryQuestionBinding getLayout() {
        return ActivityRecoveryQuestionBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);
        edit1 = findViewById(R.id.edit1);
        edit2 = findViewById(R.id.edit2);
        edit3 = findViewById(R.id.edit3);
        button = findViewById(R.id.submit_btn);
        awro = findViewById(R.id.arrow);

        awro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecoveryQuestionActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        sharedPreferences = new SharedPreferencesHelper(this);
        String text = sharedPreferences.getString("recovery_option1", "");
        String textview = sharedPreferences.getString("recovery_option2", "");
        String text03 = sharedPreferences.getString("recovery_option3", "");
        editText1 = sharedPreferences.getString("recovery_password4", "");

        if (editText1 != null) {
            // Toast.makeText(this, editText1, Toast.LENGTH_SHORT).show();
        } else {
            // Handle the case when the value is null
        }
        editText2 = sharedPreferences.getString("recovery_password5", "");
        if (editText2 != null) {
            // Use the retrieved value
        } else {
            // Handle the case when the value is null
        }

        editText3 = sharedPreferences.getString("recovery_password6", "");
        if (editText3 != null) {
            // Use the retrieved value
        } else {
            // Handle the case when the value is null
        }

        text1.setText(text);
        text2.setText(textview);
        text3.setText(text03);
        // button validation

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validation()) {
                    Intent intent = new Intent(RecoveryQuestionActivity.this, SetNewPatternActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private boolean validation() {
        String inputTex1 = edit1.getText().toString();
        String inputText2 = edit2.getText().toString();
        String inputText3 = edit3.getText().toString();


        if (inputTex1.isEmpty()) {
            edit1.setError("Please enter text");
            return false;
        }
        if (inputText2.isEmpty()) {
            edit2.setError("Please enter text");
            return false;
        }
        if (inputText3.isEmpty()) {
            edit3.setError("Please enter text");
            return false;
        }

        if (!editText1.equals(inputTex1)) {
            Toast.makeText(RecoveryQuestionActivity.this, "1 do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!editText2.equals(inputText2)) {
            Toast.makeText(RecoveryQuestionActivity.this, "2 do not match", Toast.LENGTH_SHORT).show();
            return false;

        }

        if (!editText3.equals(inputText3)) {
            Toast.makeText(RecoveryQuestionActivity.this, "3 do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}