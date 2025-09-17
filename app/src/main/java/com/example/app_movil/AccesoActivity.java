package com.example.app_movil;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AccesoActivity extends AppCompatActivity {

    private EditText txt_login, txt_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_login = findViewById(R.id.txt_login);
        txt_pass = findViewById(R.id.txt_pass);

        // Crear o abrir la base de datos
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase bd = admin.getWritableDatabase();
        bd.close();
    }

    public void iniciarSesion(View view) {
        String usuario = txt_login.getText().toString().trim();
        String password = txt_pass.getText().toString().trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase bd = admin.getReadableDatabase();

        Cursor fila = bd.rawQuery(
                "SELECT usu_cod FROM usuario WHERE usu_name=? AND usu_pass=?",
                new String[]{usuario, password});

        if (fila.moveToFirst()) {
            Toast.makeText(this, "Acceso correcto", Toast.LENGTH_SHORT).show();

            // Abrir la siguiente Activity
            Intent i = new Intent(this, MenuPrincipal.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }

        fila.close();
        bd.close();
    }

    public void limpiar(View view) {
        txt_login.setText("");
        txt_pass.setText("");
    }

    public void salir(View view) {
        finishAffinity(); // Cierra la app
    }
}
