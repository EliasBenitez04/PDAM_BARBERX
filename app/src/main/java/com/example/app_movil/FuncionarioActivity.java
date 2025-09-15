package com.example.app_movil;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class FuncionarioActivity extends AppCompatActivity {

    private EditText edtFechaContratacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funcionario);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_funcionario);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getTitle());

        // Habilitar botón de retroceso
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Referencia al EditText de fecha
        edtFechaContratacion = findViewById(R.id.edt_fecha_contratacion);

        // Configurar click para abrir DatePicker
        edtFechaContratacion.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    FuncionarioActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        // Ajuste del mes (0-11)
                        String fecha = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        edtFechaContratacion.setText(fecha);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });
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
