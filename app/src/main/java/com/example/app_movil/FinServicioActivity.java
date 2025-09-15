package com.example.app_movil;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class FinServicioActivity extends AppCompatActivity {

    private EditText edtFechaFin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_servicio);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_fin_servicio);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getTitle());

        // Habilitar botón de retroceso
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Referencia al EditText
        edtFechaFin = findViewById(R.id.edt_fecha_fin);

        // Click listener para abrir DatePicker + TimePicker
        edtFechaFin.setOnClickListener(v -> abrirDateTimePicker(edtFechaFin));
    }

    // Función para abrir DatePicker + TimePicker
    private void abrirDateTimePicker(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                FinServicioActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            FinServicioActivity.this,
                            (timeView, hourOfDay, minute1) -> {
                                String fechaHora = dayOfMonth + "/" + (month1 + 1) + "/" + year1 +
                                        " " + String.format("%02d:%02d", hourOfDay, minute1);
                                editText.setText(fechaHora);
                            },
                            hour, minute, true
                    );
                    timePickerDialog.show();
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
