package com.example.briantomasco.profile_fill;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

/**
 * Created by briantomasco on 9/28/17.
 */

public class ConfirmPasswordActivity extends Activity{

    final int CONFIRM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_password);

        //access the reconfirm field
        final EditText cp = findViewById(R.id.reconfirm);

        //listen to what text is entered
        cp.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {

                //check if there's a match

                final Button ok = findViewById(R.id.confirm_ok);

                String passwd1 = getIntent().getStringExtra("passwd1");
                String passwd2 = cp.getText().toString();
                TextView match = findViewById(R.id.matches);

                if (passwd1.equals(passwd2)){
                    match.setText("Passwords match :)");
                    match.setTextColor(Color.GREEN);
                    ok.setClickable(true);
                    ok.setAlpha(1);
                }
                else{
                    match.setText("Passwords do not match :(");
                    match.setTextColor(Color.RED);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

    }
    //when the user is satisfied and has successfully matched
    protected void onOkClick(View v){
        Intent result = new Intent();
        result.putExtra("match", true);
        setResult(CONFIRM, result);
        finish();
    }
    //when the user wants to change the original password
    protected void onCancelClick(View v){
        //document that a match did not occur
        Intent passMatch = new Intent();
        passMatch.putExtra("match", false);
        setResult(CONFIRM, passMatch);
        finish();
    }

}
