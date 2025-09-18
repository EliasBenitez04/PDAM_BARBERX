package com.example.app_movil;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FinServicioActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerAgendamiento;
    private AutoCompleteTextView spinnerUsuario;
    private TextInputEditText edtFechaFin;
    private TextInputEditText edtEstado;
    private TextInputEditText edtObservacion;
    private MaterialButton btnGuardar, btnLimpiar, btnSalir;

    private AdminSQLiteOpenHelper helper;

    // listas para relacionar la opción seleccionada con su id
    private final List<Integer> agendamientoIds = new ArrayList<>();
    private final List<Integer> usuarioIds = new ArrayList<>();

    // índices seleccionados (pueden establecerse por click en el dropdown o deducirse por texto)
    private int agendamientoSeleccionado = -1;
    private int usuarioSeleccionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_servicio);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_fin_servicio);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Fin de Servicio");

        helper = new AdminSQLiteOpenHelper(this);

        TextView tvNoAgendamientos = findViewById(R.id.tv_no_agendamientos);

        // referencias (asegúrate que los ids coincidan con tu layout)
        spinnerAgendamiento = findViewById(R.id.spinner_agendamiento);
        spinnerUsuario = findViewById(R.id.spinner_usuario_fin);
        edtFechaFin = findViewById(R.id.edt_fecha_fin);
        edtEstado = findViewById(R.id.edt_estado);
        edtObservacion = findViewById(R.id.edt_observacion_fin);
        btnGuardar = findViewById(R.id.btn_finalizar_servicio);
        btnLimpiar = findViewById(R.id.btn_limpiar_fin_servicio);
        btnSalir = findViewById(R.id.btn_salir_fin_servicio);

        // estado por defecto (según tu layout)
        edtEstado.setText("REALIZADO");

        // mostrar dropdown cuando se hace click en el campo (mejora UX)
        spinnerAgendamiento.setOnClickListener(v -> spinnerAgendamiento.showDropDown());
        spinnerUsuario.setOnClickListener(v -> spinnerUsuario.showDropDown());

        // detectar selección por click (devuelve posición dentro del adapter)
        spinnerAgendamiento.setOnItemClickListener((parent, view, position, id) -> agendamientoSeleccionado = position);
        spinnerUsuario.setOnItemClickListener((parent, view, position, id) -> usuarioSeleccionado = position);

        // cargar datos
        cargarAgendamientosPendientes();
        cargarUsuariosDropdown();

        // DatePicker
        configurarDatePicker();

        // botones
        btnGuardar.setOnClickListener(v -> guardarFinServicio());
        btnLimpiar.setOnClickListener(v -> limpiarCampos());
        btnSalir.setOnClickListener(v -> finish());
    }

    // carga agendamientos PENDIENTES y construye display + lista de ids
    private void cargarAgendamientosPendientes() {
        agendamientoIds.clear();

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT a.agen_cod, c.cli_nom || ' ' || c.cli_ape AS cliente, " +
                        "f.fun_nom || ' ' || f.fun_ape AS barbero, a.agen_date_reserva " +
                        "FROM agendamiento a " +
                        "INNER JOIN cliente c ON a.cli_cod = c.cli_cod " +
                        "INNER JOIN funcionario f ON a.fun_cod = f.fun_cod " +
                        "WHERE a.agen_estado = 'PENDIENTE' " +
                        "ORDER BY a.agen_date_reserva ASC", null);

        List<String> displays = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("agen_cod"));
            String cliente = cursor.getString(cursor.getColumnIndexOrThrow("cliente"));
            String barbero = cursor.getString(cursor.getColumnIndexOrThrow("barbero"));
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow("agen_date_reserva"));
            String display = id + " - " + cliente + " con " + barbero + " (" + fecha + ")";
            displays.add(display);
            agendamientoIds.add(id);
        }
        cursor.close();

        TextView tvNoAgendamientos = findViewById(R.id.tv_no_agendamientos);

        if (displays.isEmpty()) {
            tvNoAgendamientos.setVisibility(View.VISIBLE);
            spinnerAgendamiento.setVisibility(View.GONE);
        } else {
            tvNoAgendamientos.setVisibility(View.GONE);
            spinnerAgendamiento.setVisibility(View.VISIBLE);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, displays);
            spinnerAgendamiento.setAdapter(adapter);
        }

        Toast.makeText(this, "Agendamientos cargados: " + displays.size(), Toast.LENGTH_SHORT).show();

        // reset selección
        agendamientoSeleccionado = -1;
    }

    // carga usuarios mostrando el nombre del funcionario ligado al usuario
    private void cargarUsuariosDropdown() {
        usuarioIds.clear();

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT u.usu_cod, f.fun_nom || ' ' || f.fun_ape AS nombre " +
                        "FROM usuario u " +
                        "INNER JOIN funcionario f ON u.fun_cod = f.fun_cod " +
                        "ORDER BY f.fun_nom, f.fun_ape", null);

        List<String> displays = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("usu_cod"));
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            displays.add(nombre);
            usuarioIds.add(id);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, displays);
        spinnerUsuario.setAdapter(adapter);

        Toast.makeText(this, "Usuarios cargados: " + displays.size(), Toast.LENGTH_SHORT).show();

        // reset selección
        usuarioSeleccionado = -1;
    }

    // configura el date picker para edtFechaFin
    private void configurarDatePicker() {
        edtFechaFin.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(FinServicioActivity.this,
                    (view, y, m, d) -> {
                        // guardamos en formato YYYY-MM-DD
                        String fecha = String.format("%04d-%02d-%02d", y, m + 1, d);
                        edtFechaFin.setText(fecha);
                    }, year, month, day);
            dpd.show();
        });
    }

    // intenta obtener índice seleccionado — si el usuario escribió, busca por texto en adapter
    private int obtenerIndiceAgendamientoSeleccionado() {
        // si ya se seleccionó por click, usarlo
        if (agendamientoSeleccionado >= 0 && agendamientoSeleccionado < agendamientoIds.size()) return agendamientoSeleccionado;

        // si no, buscar por texto
        String texto = spinnerAgendamiento.getText().toString().trim();
        ArrayAdapter adapter = (ArrayAdapter) spinnerAgendamiento.getAdapter();
        if (adapter == null) return -1;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (texto.equals(adapter.getItem(i).toString())) return i;
            // permitir que el usuario escriba solo el id (ej: "12")
            try {
                int typedId = Integer.parseInt(texto);
                int adapterId = agendamientoIds.get(i);
                if (typedId == adapterId) return i;
            } catch (Exception ignore) {}
        }
        return -1;
    }

    private int obtenerIndiceUsuarioSeleccionado() {
        if (usuarioSeleccionado >= 0 && usuarioSeleccionado < usuarioIds.size()) return usuarioSeleccionado;

        String texto = spinnerUsuario.getText().toString().trim();
        ArrayAdapter adapter = (ArrayAdapter) spinnerUsuario.getAdapter();
        if (adapter == null) return -1;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (texto.equals(adapter.getItem(i).toString())) return i;
        }
        return -1;
    }

    // guarda fin_servicio y actualiza el agendamiento a REALIZADO
    private void guardarFinServicio() {
        int idxAgen = obtenerIndiceAgendamientoSeleccionado();
        int idxUsu = obtenerIndiceUsuarioSeleccionado();
        String fecha = edtFechaFin.getText().toString().trim();
        String estado = edtEstado.getText().toString().trim();
        String observacion = edtObservacion.getText().toString().trim();

        if (idxAgen < 0) {
            Toast.makeText(this, "Seleccione un agendamiento válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (idxUsu < 0) {
            Toast.makeText(this, "Seleccione un usuario válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fecha.isEmpty()) {
            Toast.makeText(this, "Seleccione la fecha de finalización", Toast.LENGTH_SHORT).show();
            return;
        }

        int agenCod = agendamientoIds.get(idxAgen);
        int usuCod = usuarioIds.get(idxUsu);

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put("agen_cod", agenCod);
            cv.put("usu_cod", usuCod);
            cv.put("fin_serv_date", fecha);
            cv.put("fin_serv_estado", estado);
            cv.put("fin_serv_obs", observacion.isEmpty() ? null : observacion);

            long id = db.insert("fin_servicio", null, cv);
            if (id == -1) throw new Exception("Error al insertar fin_servicio");

            db.execSQL("UPDATE agendamiento SET agen_estado = 'REALIZADO' WHERE agen_cod = ?", new Object[]{agenCod});

            db.setTransactionSuccessful();

            Toast.makeText(this, "Servicio finalizado correctamente", Toast.LENGTH_SHORT).show();

            // limpiar y recargar pendientes
            limpiarCampos();
            cargarAgendamientosPendientes();

        } catch (Exception e) {
            Toast.makeText(this, "Error guardando fin de servicio: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction();
        }
    }

    private void limpiarCampos() {
        spinnerAgendamiento.setText("");
        spinnerUsuario.setText("");
        edtFechaFin.setText("");
        edtObservacion.setText("");
        agendamientoSeleccionado = -1;
        usuarioSeleccionado = -1;
    }
}
