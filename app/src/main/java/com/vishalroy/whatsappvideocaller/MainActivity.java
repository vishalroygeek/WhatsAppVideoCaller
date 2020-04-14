package com.vishalroy.whatsappvideocaller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;
import com.vishalroy.whatsappvideocaller.Helpers.Typefaces;

public class MainActivity extends AppCompatActivity {

    private Button video_call_button;
    private TextView message;
    private EditText number;
    private Typefaces typefaces;
    private CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing some objects
        typefaces = new Typefaces(this);

        //Finding view by ID
        video_call_button = findViewById(R.id.video_call_button);
        message = findViewById(R.id.message);
        number = findViewById(R.id.number);
        ccp = findViewById(R.id.ccp);

        //Changing typefaces
        video_call_button.setTypeface(typefaces.ralewayMedium());
        message.setTypeface(typefaces.ralewayMedium());
        number.setTypeface(typefaces.latoBold());
        ccp.setTypeFace(typefaces.ralewaySemiBold());

        //Binding number editText to ccp
        ccp.registerCarrierNumberEditText(number);
    }
}
