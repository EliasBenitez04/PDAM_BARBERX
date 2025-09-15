package com.example.app_movil;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class UsuarioActivity extends AppCompatActivity {

    private EditText edtFechaContratacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_usuario);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getTitle());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Referencia al campo de fecha
        edtFechaContratacion = findViewById(R.id.edt_fecha_contratacion);

        // Setear fecha actual al abrir la pantalla
        Calendar calendar = Calendar.getInstance();
        String fechaActual = calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.YEAR);
        edtFechaContratacion.setText(fechaActual);

        // Click listener para abrir DatePicker
        edtFechaContratacion.setOnClickListener(v -> abrirDatePicker(edtFechaContratacion));
    }

    // Función para abrir DatePicker
    private void abrirDatePicker(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                UsuarioActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    String fecha = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    editText.setText(fecha);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    // Manejar click del botón atrás
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
