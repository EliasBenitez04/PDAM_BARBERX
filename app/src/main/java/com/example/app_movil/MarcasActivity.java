package com.example.app_movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MarcasActivity extends AppCompatActivity {

    EditText txtMarca;
    Button btnGuardar, btnSalir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcas);

        txtMarca = findViewById(R.id.txt_marcas);
        btnGuardar = findViewById(R.id.btn_guardar_marcas);
        btnSalir = findViewById(R.id.btn_salir_marcas);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String marca = txtMarca.getText().toString().trim();
                if (!marca.isEmpty()) {
                    Toast.makeText(MarcasActivity.this, "Marca guardada: " + marca, Toast.LENGTH_SHORT).show();
                    txtMarca.setText("");
                } else {
                    Toast.makeText(MarcasActivity.this, "Ingrese una marca", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}