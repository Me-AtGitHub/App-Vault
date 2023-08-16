package com.example.calculater.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calculater.R;
import com.example.calculater.SharedPreferencesHelper;
import com.example.calculater.databinding.ActivityRecoveryPasswordBinding;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class RecoveryPasswordActivity extends BaseActivity<ActivityRecoveryPasswordBinding> {
    ImageView arow;
    private TextView spinnerRecoveryQuestions, spinner2, spinner3;
    private TextView firstName, lastname, nickname;
    private Button validateButton;
    private EditText recoveryPasswordEditText, editText1, editText2;
    private SharedPreferencesHelper sharedPreferences;

    public RecoveryPasswordActivity() {
    }

    @Override
    ActivityRecoveryPasswordBinding getLayout() {
        return ActivityRecoveryPasswordBinding.inflate(getLayoutInflater());
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arow = findViewById(R.id.arrow);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // id findView
        spinnerRecoveryQuestions = findViewById(R.id.spinner_recovery_questions);
        spinner2 = findViewById(R.id.spinner2);
        spinner3 = findViewById(R.id.spinner3);
        recoveryPasswordEditText = findViewById(R.id.edit);
        editText1 = findViewById(R.id.edit1);
        editText2 = findViewById(R.id.edit2);
        validateButton = findViewById(R.id.submit);

        arow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecoveryPasswordActivity.this, SettingActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // sharedPreferences
        sharedPreferences = new SharedPreferencesHelper(this);

        // Create an array adapter for the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.question, // String array resource containing recovery questions
                android.R.layout.simple_spinner_item

        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the adapter on the spinner
        //   spinnerRecoveryQuestions.setAdapter(adapter);
        // second adapter

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this,
                R.array.question1, // String array resource containing items for spinner 2
                android.R.layout.simple_spinner_item
        );
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner2.setAdapter(adapter2);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
                this,
                R.array.question2, // String array resource containing items for spinner 2
                android.R.layout.simple_spinner_item
        );
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // spinner3.setAdapter(adapter2);

     /*   spinnerRecoveryQuestions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedQuestion = adapterView.getItemAtPosition(position).toString();
                // Do something with the selected question
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case where nothing is selected
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedSpinner2Item = adapterView.getItemAtPosition(position).toString();
                // Do something with the selected item from spinner 2
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case where nothing is selected in spinner 2
            }
        });
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedSpinner2Item = adapterView.getItemAtPosition(position).toString();
                // Do something with the selected item from spinner 2
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case where nothing is selected in spinner 2
            }
        });*/

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = recoveryPasswordEditText.getText().toString();
                String inputText1 = editText2.getText().toString();
                String inputText2 = editText1.getText().toString();
                if (inputText.isEmpty()) {
                    recoveryPasswordEditText.setError("Please enter text");
                }
                if (inputText1.isEmpty()) {
                    editText1.setError("Please enter text");
                }
                if (inputText2.isEmpty()) {
                    editText2.setError("Please enter text");
                } else {
                    setRecoveryPassword();
                    Intent intent = new Intent(RecoveryPasswordActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
              /*  if(validateSpinners()){
                    setRecoveryPassword();
                    Intent intent = new Intent(RecoveryPasswordActivity.this,HomeActivity.class);
                    startActivity(intent);
                }else {
                }*/
            }
        });
    }

    private void setRecoveryPassword() {
        String spinnerRecover = spinnerRecoveryQuestions.getText().toString();
        String spinnerRecover2 = spinner2.getText().toString();
        String spinnerRecover3 = spinner3.getText().toString();
        String recoveryPassword = recoveryPasswordEditText.getText().toString();
        String recoveryPassword2 = editText1.getText().toString();
        String recoveryPassword3 = editText2.getText().toString();

        // Store the recovery option and password in SharedPreferences
        sharedPreferences.saveString("recovery_option1", spinnerRecover);
        sharedPreferences.saveString("recovery_option2", spinnerRecover2);
        sharedPreferences.saveString("recovery_option3", spinnerRecover3);
        sharedPreferences.saveString("recovery_password4", recoveryPassword);
        sharedPreferences.saveString("recovery_password5", recoveryPassword2);
        sharedPreferences.saveString("recovery_password6", recoveryPassword3);
        String dataToSave = "Selected Option: " + spinnerRecover + "" + spinnerRecover2 + "" + spinnerRecover3 +
                "" + recoveryPassword + "" + recoveryPassword2 + "" + recoveryPassword3;
        Toast.makeText(this, "Recovery password set successfully", Toast.LENGTH_SHORT).show();
        saveDataToFile(dataToSave);
        //createFolder();
    }

    private void createFolder() {
        String folderName = "MyFolder";
        String folderPath = Environment.getExternalStorageDirectory().toString() + "/Pictures" + File.separator + folderName;

        File folder = new File(folderPath);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Toast.makeText(this, "Folder created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create the folder", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Folder already exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDataToFile(String data) {
        String folderName = "MyFolder";
        String fileName = "data.txt";

        // Create the folder in the Downloads directory if it doesn't exist
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Toast.makeText(this, "Folder created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create the folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Get the Downloads directory URI
        Uri downloadsUri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        }

        // Create a new file entry in MediaStore
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + folderName);
        Uri fileUri = getContentResolver().insert(downloadsUri, values);
        if (fileUri == null) {
            Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
            return;
        }
        // Write data to the file
        try (OutputStream outputStream = getContentResolver().openOutputStream(fileUri)) {
            if (outputStream != null) {
                outputStream.write(data.getBytes());
                Toast.makeText(this, "Data saved to file", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
        }
    }

  /*  private boolean validateSpinners() {
        int position1 = spinnerRecoveryQuestions.getSelectedItemPosition();
        int position2 = spinner2.getSelectedItemPosition();
        int position3 = spinner3.getSelectedItemPosition();

        if (position1 == position2 || position1 == position3 || position2 == position3) {
            String errorMessage = getString(R.string.spinner_position_error);
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            // Perform further actions or validations
            return true;
        }
    }*/

}