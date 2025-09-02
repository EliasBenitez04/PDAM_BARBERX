package com.example.app_movil;

import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private EditText aux_login, aux_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //se enlaza con el xml
        aux_login=(EditText) findViewById(R.id.txt_login);
        aux_pass=(EditText) findViewById(R.id.txt_pass);

    }

    public void salir(View view) {
        finish();
    }

    public void limpiar(View view) {
        aux_login.setText("");
        aux_pass.setText("");
        aux_login.requestFocus();
    }

    // version 1.0
    // Version 1.1
    // Version 1.2
}