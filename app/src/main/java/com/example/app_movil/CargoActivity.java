package com.example.app_movil;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

public class CargoActivity extends AppCompatActivity {

    private TextInputEditText edtCargo;
    private MaterialButton btnGuardar, btnLimpiar, btnSalir;
    private RecyclerView rvCargos;

    private AdminSQLiteOpenHelper dbHelper;
    private CargoAdapter adapter;
    private ArrayList<Cargo> listaCargos;
    private Cargo cargoSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargo);

        Toolbar toolbar = findViewById(R.id.toolbar_cargo);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtCargo = findViewById(R.id.edt_cargo);
        btnGuardar = findViewById(R.id.btn_guardar_cargo);
        btnLimpiar = findViewById(R.id.btn_limpiar_cargo);
        btnSalir = findViewById(R.id.btn_salir_cargo);
        rvCargos = findViewById(R.id.rv_cargos);

        dbHelper = new AdminSQLiteOpenHelper(this);

        listaCargos = new ArrayList<>();

        adapter = new CargoAdapter(listaCargos, new CargoAdapter.OnItemClickListener() {
            @Override
            public void onEditar(Cargo cargo) {
                cargoSeleccionado = cargo;
                editarCargo();
            }

            @Override
            public void onBorrar(Cargo cargo) {
                cargoSeleccionado = cargo;
                borrarCargo();
            }

            @Override
            public void onSeleccion(Cargo cargo) {
                cargoSeleccionado = cargo;
                edtCargo.setText(cargo.getDescripcion());
            }
        });

        rvCargos.setLayoutManager(new LinearLayoutManager(this));
        rvCargos.setAdapter(adapter);

        cargarCargos();

        btnGuardar.setOnClickListener(v -> guardarCargo());
        btnLimpiar.setOnClickListener(v -> {
            edtCargo.setText("");
            cargoSeleccionado = null;
        });
        btnSalir.setOnClickListener(v -> finish());
    }

    private void cargarCargos() {
        listaCargos.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT car_cod, car_desc FROM cargo ORDER BY car_desc ASC", null);

        if (cursor.moveToFirst()) {
            do {
                listaCargos.add(new Cargo(cursor.getInt(0), cursor.getString(1)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void guardarCargo() {
        String desc = edtCargo.getText() != null ? edtCargo.getText().toString().trim().toUpperCase() : "";
        if (desc.isEmpty()) {
            Toast.makeText(this, "Ingrese un cargo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cargoSeleccionado != null) {
            editarCargo();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor check = db.rawQuery("SELECT car_cod FROM cargo WHERE UPPER(car_desc)=?", new String[]{desc});
        if (check.moveToFirst()) {
            check.close();
            db.close();
            Toast.makeText(this, "Ya existe un cargo con esa descripción", Toast.LENGTH_SHORT).show();
            return;
        }
        check.close();
        db.close();

        db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO cargo (car_desc) VALUES (?)", new Object[]{desc});
        db.close();

        Toast.makeText(this, "Cargo guardado", Toast.LENGTH_SHORT).show();
        edtCargo.setText("");
        cargarCargos();
    }

    private void editarCargo() {
        if (cargoSeleccionado == null) {
            Toast.makeText(this, "Seleccione un cargo primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevo = edtCargo.getText() != null ? edtCargo.getText().toString().trim().toUpperCase() : "";
        if (nuevo.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre válido", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor check = db.rawQuery("SELECT car_cod FROM cargo WHERE UPPER(car_desc)=? AND car_cod<>?", new String[]{nuevo, String.valueOf(cargoSeleccionado.getId())});
        if (check.moveToFirst()) {
            check.close();
            db.close();
            Toast.makeText(this, "Ya existe otro cargo con esa descripción", Toast.LENGTH_SHORT).show();
            return;
        }
        check.close();
        db.close();

        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE cargo SET car_desc=? WHERE car_cod=?", new Object[]{nuevo, cargoSeleccionado.getId()});
        db.close();

        Toast.makeText(this, "Cargo actualizado", Toast.LENGTH_SHORT).show();
        edtCargo.setText("");
        cargoSeleccionado = null;
        cargarCargos();
    }

    private void borrarCargo() {
        if (cargoSeleccionado == null) {
            Toast.makeText(this, "Seleccione un cargo primero", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM cargo WHERE car_cod=?", new Object[]{cargoSeleccionado.getId()});
        db.close();

        Toast.makeText(this, "Cargo eliminado", Toast.LENGTH_SHORT).show();
        edtCargo.setText("");
        cargoSeleccionado = null;
        cargarCargos();
    }

    /* ===================== CLASES INTERNAS ===================== */

    /** Modelo de datos */
    private static class Cargo {
        private int id;
        private String descripcion;

        public Cargo(int id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        public int getId() { return id; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    /** Adapter del RecyclerView (usa item_texto.xml) */
    private static class CargoAdapter extends RecyclerView.Adapter<CargoAdapter.ViewHolder> {

        interface OnItemClickListener {
            void onEditar(Cargo cargo);
            void onBorrar(Cargo cargo);
            void onSeleccion(Cargo cargo);
        }

        private final List<Cargo> listaCargos;
        private final OnItemClickListener listener;

        public CargoAdapter(List<Cargo> listaCargos, OnItemClickListener listener) {
            this.listaCargos = listaCargos;
            this.listener = listener;
        }

        @NonNull
        @Override
        public CargoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_texto, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CargoAdapter.ViewHolder holder, int position) {
            Cargo cargo = listaCargos.get(position);
            holder.txtDescripcion.setText(cargo.getDescripcion());

            holder.itemView.setOnClickListener(v -> listener.onSeleccion(cargo));
            holder.btnEditar.setOnClickListener(v -> listener.onEditar(cargo));
            holder.btnBorrar.setOnClickListener(v -> listener.onBorrar(cargo));
        }

        @Override
        public int getItemCount() {
            return listaCargos.size();
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
