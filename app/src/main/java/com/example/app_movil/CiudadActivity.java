package com.example.app_movil;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CiudadActivity extends AppCompatActivity {

    private TextInputEditText edtCiudad;
    private MaterialButton btnGuardar, btnLimpiar, btnSalir;
    private RecyclerView rvCiudades;

    private AdminSQLiteOpenHelper dbHelper;
    private CiudadAdapter adapter;
    private ArrayList<Ciudad> listaCiudades;
    private Ciudad ciudadSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ciudad);

        Toolbar toolbar = findViewById(R.id.toolbar_ciudad);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtCiudad = findViewById(R.id.edt_ciudad);
        btnGuardar = findViewById(R.id.btn_guardar_ciudad);
        btnLimpiar = findViewById(R.id.btn_limpiar_ciudad);
        btnSalir = findViewById(R.id.btn_salir_ciudad);
        rvCiudades = findViewById(R.id.rv_ciudades);

        // Usamos el AdminSQLiteOpenHelper global (versión 2)
        dbHelper = new AdminSQLiteOpenHelper(this);

        listaCiudades = new ArrayList<>();

        adapter = new CiudadAdapter(listaCiudades, new CiudadAdapter.OnItemClickListener() {
            @Override
            public void onEditar(Ciudad ciudad) {
                ciudadSeleccionada = ciudad;
                editarCiudad();
            }

            @Override
            public void onBorrar(Ciudad ciudad) {
                ciudadSeleccionada = ciudad;
                borrarCiudad();
            }

            @Override
            public void onSeleccion(Ciudad ciudad) {
                ciudadSeleccionada = ciudad;
                edtCiudad.setText(ciudad.getNombre());
            }
        });

        rvCiudades.setLayoutManager(new LinearLayoutManager(this));
        rvCiudades.setAdapter(adapter);

        cargarCiudades();

        btnGuardar.setOnClickListener(v -> guardarCiudad());
        btnLimpiar.setOnClickListener(v -> {
            edtCiudad.setText("");
            ciudadSeleccionada = null;
        });
        btnSalir.setOnClickListener(v -> finish());
    }

    private void cargarCiudades() {
        listaCiudades.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ciu_cod, ciu_desc FROM ciudad ORDER BY ciu_desc ASC", null);

        if (cursor.moveToFirst()) {
            do {
                listaCiudades.add(new Ciudad(cursor.getInt(0), cursor.getString(1)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void guardarCiudad() {
        String nombre = edtCiudad.getText() != null ? edtCiudad.getText().toString().trim().toUpperCase() : "";
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre de ciudad", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de duplicado (case-insensitive)
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor check = db.rawQuery("SELECT ciu_cod FROM ciudad WHERE UPPER(ciu_desc)=?", new String[]{nombre});
        if (check.moveToFirst()) {
            check.close();
            db.close();
            Toast.makeText(this, "Ya existe una ciudad con esa descripción", Toast.LENGTH_SHORT).show();
            return;
        }
        check.close();
        db.close();

        // Insert
        db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO ciudad (ciu_desc) VALUES (?)", new Object[]{nombre});
        db.close();

        Toast.makeText(this, "Ciudad guardada", Toast.LENGTH_SHORT).show();
        edtCiudad.setText("");
        cargarCiudades();
    }

    private void editarCiudad() {
        if (ciudadSeleccionada == null) {
            Toast.makeText(this, "Seleccione una ciudad primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevoNombre = edtCiudad.getText() != null ? edtCiudad.getText().toString().trim().toUpperCase() : "";
        if (nuevoNombre.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de duplicado excluyendo el registro actual
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor check = db.rawQuery(
                "SELECT ciu_cod FROM ciudad WHERE UPPER(ciu_desc)=? AND ciu_cod<>?",
                new String[]{nuevoNombre, String.valueOf(ciudadSeleccionada.getId())}
        );
        if (check.moveToFirst()) {
            check.close();
            db.close();
            Toast.makeText(this, "Ya existe otra ciudad con esa descripción", Toast.LENGTH_SHORT).show();
            return;
        }
        check.close();
        db.close();

        // Update
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE ciudad SET ciu_desc=? WHERE ciu_cod=?", new Object[]{nuevoNombre, ciudadSeleccionada.getId()});
        db.close();

        Toast.makeText(this, "Ciudad actualizada", Toast.LENGTH_SHORT).show();
        edtCiudad.setText("");
        ciudadSeleccionada = null;
        cargarCiudades();
    }

    private void borrarCiudad() {
        if (ciudadSeleccionada == null) {
            Toast.makeText(this, "Seleccione una ciudad primero", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM ciudad WHERE ciu_cod=?", new Object[]{ciudadSeleccionada.getId()});
        db.close();

        Toast.makeText(this, "Ciudad eliminada", Toast.LENGTH_SHORT).show();
        edtCiudad.setText("");
        ciudadSeleccionada = null;
        cargarCiudades();
    }

    /* ===================== CLASES INTERNAS ===================== */

    private static class Ciudad {
        private int id;
        private String nombre;

        public Ciudad(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }

    private static class CiudadAdapter extends RecyclerView.Adapter<CiudadAdapter.ViewHolder> {

        interface OnItemClickListener {
            void onEditar(Ciudad ciudad);
            void onBorrar(Ciudad ciudad);
            void onSeleccion(Ciudad ciudad);
        }

        private final List<Ciudad> listaCiudades;
        private final OnItemClickListener listener;

        public CiudadAdapter(List<Ciudad> listaCiudades, OnItemClickListener listener) {
            this.listaCiudades = listaCiudades;
            this.listener = listener;
        }

        @NonNull
        @Override
        public CiudadAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_texto, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CiudadAdapter.ViewHolder holder, int position) {
            Ciudad ciudad = listaCiudades.get(position);
            holder.txtDescripcion.setText(ciudad.getNombre());

            holder.itemView.setOnClickListener(v -> listener.onSeleccion(ciudad));
            holder.btnEditar.setOnClickListener(v -> listener.onEditar(ciudad));
            holder.btnBorrar.setOnClickListener(v -> listener.onBorrar(ciudad));
        }

        @Override
        public int getItemCount() {
            return listaCiudades.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtDescripcion;
            Button btnEditar, btnBorrar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtDescripcion = itemView.findViewById(R.id.txt_item_descripcion);
                btnEditar = itemView.findViewById(R.id.btn_item_editar);
                btnBorrar = itemView.findViewById(R.id.btn_item_borrar);
            }
        }
    }
}
