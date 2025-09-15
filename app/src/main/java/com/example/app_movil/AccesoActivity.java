package com.example.app_movil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AccesoActivity extends AppCompatActivity {

    private EditText aux_login, aux_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilitar EdgeToEdge para que el contenido se muestre bajo la barra de estado
        EdgeToEdge.enable(this);

        // Cargar layout principal
        setContentView(R.layout.activity_main);

        // Ajustar padding automáticamente según las barras del sistema (EdgeToEdge)
        // Asegúrate que el layout raíz tenga android:id="@+id/main"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enlaza con el XML
        aux_login = findViewById(R.id.txt_login);
        aux_pass = findViewById(R.id.txt_pass);
    }

    // Limpiar campos
    public void limpiar(View view) {
        aux_login.setText("");
        aux_pass.setText("");
        aux_login.requestFocus();
    }

    // Salir de la Activity
    public void salir(View view) {
        finish();
    }

    // Validar usuario y contraseña
    /*public void verificar(View view) {
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase BaseDeDatos = miconexion.getWritableDatabase();

        String usuario = aux_login.getText().toString();
        String clave = aux_pass.getText().toString();

        if (!usuario.isEmpty() && !clave.isEmpty()) {
            // Usar parámetros para evitar SQL Injection
            Cursor fila = BaseDeDatos.rawQuery(
                    "SELECT cod_usu, usu_nombre, usu_rol FROM usuario WHERE usu_login=? AND usu_clave=?",
                    new String[]{usuario, clave}
            );

            // Verificar si el cursor tiene resultados
            if (fila != null && fila.moveToFirst()) {
                // Guardar preferencias en SharedPreferences
                guardar_preferencias();

                // Pasar parámetros a la siguiente Activity
                Intent siguiente = new Intent(this, MenuPrincipal.class);
                siguiente.putExtra("parametro_usu", fila.getString(1)); // Nombre del usuario
                siguiente.putExtra("parametro_rol", fila.getString(2)); // Rol o nivel

                startActivity(siguiente);
                finish(); // Cerrar acceso

            } else {
                // Usuario o contraseña incorrecta
                Toast.makeText(this, "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show();
            }

            // Cerrar Cursor y Base de Datos para evitar fugas de memoria
            if (fila != null) fila.close();
            BaseDeDatos.close();

        } else {
            // Si hay campos vacíos
            Toast.makeText(this, "Hay campos vacíos, por favor verifique", Toast.LENGTH_SHORT).show();
        }
    }

    // Guardar SharedPreferences
    private void guardar_preferencias() {
        SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("user", aux_login.getText().toString());
        editor.putString("pass", aux_pass.getText().toString());

        // Aplicar cambios de forma asíncrona (más eficiente que commit())
        editor.apply();
    }

    // Leer SharedPreferences
    public void leer_preferencias(View view) {
        SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);

        String user = preferences.getString("user", "");
        String pass = preferences.getString("pass", "");

        aux_login.setText(user);
        aux_pass.setText(pass);
    }*/
}
