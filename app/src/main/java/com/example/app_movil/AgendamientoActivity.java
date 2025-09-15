package com.example.app_movil;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class AgendamientoActivity extends AppCompatActivity {

    private EditText edtFechaReserva;
    private EditText edtFechaAtencion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamiento);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_agendamiento);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getTitle());

        // Habilitar botón de retroceso
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Referencias a los EditText
        edtFechaReserva = findViewById(R.id.edt_fecha_reserva);
        edtFechaAtencion = findViewById(R.id.edt_fecha_atencion);

        // Click listener para abrir DatePicker + TimePicker en edtFechaReserva
        edtFechaReserva.setOnClickListener(v -> abrirDateTimePicker(edtFechaReserva));

        // Click listener para abrir solo DatePicker en edtFechaAtencion
        edtFechaAtencion.setOnClickListener(v -> abrirDatePicker(edtFechaAtencion));
    }

    // Función para abrir DatePicker y setear solo la fecha
    private void abrirDatePicker(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AgendamientoActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    String fecha = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    editText.setText(fecha);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    // Función para abrir DatePicker + TimePicker y setear fecha + hora
    private void abrirDateTimePicker(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AgendamientoActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            AgendamientoActivity.this,
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
