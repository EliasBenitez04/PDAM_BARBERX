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

public class TipoServicioActivity extends AppCompatActivity {

    private TextInputEditText edtTipoServicio;
    private MaterialButton btnGuardar, btnLimpiar, btnSalir;
    private RecyclerView rvTiposServicio;

    private AdminSQLiteOpenHelper dbHelper;
    private TipoServicioAdapter adapter;
    private ArrayList<TipoServicio> listaTipos;
    private TipoServicio seleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipo_servicio);

        Toolbar toolbar = findViewById(R.id.toolbar_tipo_servicio);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtTipoServicio = findViewById(R.id.edt_tipo_servicio);
        btnGuardar = findViewById(R.id.btn_guardar_tipo_servicio);
        btnLimpiar = findViewById(R.id.btn_limpiar_tipo_servicio);
        btnSalir = findViewById(R.id.btn_salir_tipo_servicio);
        rvTiposServicio = findViewById(R.id.rv_tipos_servicio);

        dbHelper = new AdminSQLiteOpenHelper(this);

        listaTipos = new ArrayList<>();

        adapter = new TipoServicioAdapter(listaTipos, new TipoServicioAdapter.OnItemClickListener() {
            @Override public void onSeleccion(TipoServicio t) {
                seleccionado = t;
                edtTipoServicio.setText(t.getDescripcion());
            }
            @Override public void onEditar(TipoServicio t) {
                seleccionado = t;
                editar();
            }
            @Override public void onBorrar(TipoServicio t) {
                seleccionado = t;
                borrar();
            }
        });

        rvTiposServicio.setLayoutManager(new LinearLayoutManager(this));
        rvTiposServicio.setAdapter(adapter);

        cargar();

        btnGuardar.setOnClickListener(v -> guardar());
        btnLimpiar.setOnClickListener(v -> { edtTipoServicio.setText(""); seleccionado = null; });
        btnSalir.setOnClickListener(v -> finish());
    }

    private void cargar() {
        listaTipos.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT tip_serv_cod, tip_serv_desc FROM tipo_servicios ORDER BY tip_serv_desc ASC", null);
        if (c.moveToFirst()) {
            do { listaTipos.add(new TipoServicio(c.getInt(0), c.getString(1))); } while (c.moveToNext());
        }
        c.close(); db.close();
        adapter.notifyDataSetChanged();
    }

    private void guardar() {
        String desc = edtTipoServicio.getText() != null ? edtTipoServicio.getText().toString().trim().toUpperCase() : "";
        if (desc.isEmpty()) { Toast.makeText(this, "Ingrese un tipo de servicio", Toast.LENGTH_SHORT).show(); return; }

        if (seleccionado != null) { editar(); return; }

        // Duplicado
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor chk = db.rawQuery("SELECT tip_serv_cod FROM tipo_servicios WHERE UPPER(tip_serv_desc)=?", new String[]{desc});
        if (chk.moveToFirst()) { chk.close(); db.close(); Toast.makeText(this, "Ya existe un tipo de servicio con esa descripción", Toast.LENGTH_SHORT).show(); return; }
        chk.close(); db.close();

        // Insert
        db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO tipo_servicios (tip_serv_desc) VALUES (?)", new Object[]{desc});
        db.close();

        Toast.makeText(this, "Tipo de servicio guardado", Toast.LENGTH_SHORT).show();
        edtTipoServicio.setText("");
        cargar();
    }

    private void editar() {
        if (seleccionado == null) { Toast.makeText(this, "Seleccione un registro primero", Toast.LENGTH_SHORT).show(); return; }

        String nuevo = edtTipoServicio.getText() != null ? edtTipoServicio.getText().toString().trim().toUpperCase() : "";
        if (nuevo.isEmpty()) { Toast.makeText(this, "Ingrese una descripción válida", Toast.LENGTH_SHORT).show(); return; }

        // Duplicado excluyendo el actual
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor chk = db.rawQuery("SELECT tip_serv_cod FROM tipo_servicios WHERE UPPER(tip_serv_desc)=? AND tip_serv_cod<>?",
                new String[]{nuevo, String.valueOf(seleccionado.getId())});
        if (chk.moveToFirst()) { chk.close(); db.close(); Toast.makeText(this, "Ya existe otro tipo de servicio con esa descripción", Toast.LENGTH_SHORT).show(); return; }
        chk.close(); db.close();

        // Update
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE tipo_servicios SET tip_serv_desc=? WHERE tip_serv_cod=?", new Object[]{nuevo, seleccionado.getId()});
        db.close();

        Toast.makeText(this, "Tipo de servicio actualizado", Toast.LENGTH_SHORT).show();
        edtTipoServicio.setText("");
        seleccionado = null;
        cargar();
    }

    private void borrar() {
        if (seleccionado == null) { Toast.makeText(this, "Seleccione un registro primero", Toast.LENGTH_SHORT).show(); return; }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM tipo_servicios WHERE tip_serv_cod=?", new Object[]{seleccionado.getId()});
        db.close();

        Toast.makeText(this, "Tipo de servicio eliminado", Toast.LENGTH_SHORT).show();
        edtTipoServicio.setText("");
        seleccionado = null;
        cargar();
    }

    /* ===================== CLASES INTERNAS ===================== */

    private static class TipoServicio {
        private int id;
        private String descripcion;
        public TipoServicio(int id, String descripcion) { this.id = id; this.descripcion = descripcion; }
        public int getId() { return id; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String d) { this.descripcion = d; }
    }

    private static class TipoServicioAdapter extends RecyclerView.Adapter<TipoServicioAdapter.ViewHolder> {

        interface OnItemClickListener {
            void onSeleccion(TipoServicio item);
            void onEditar(TipoServicio item);
            void onBorrar(TipoServicio item);
        }

        private final List<TipoServicio> items;
        private final OnItemClickListener listener;

        public TipoServicioAdapter(List<TipoServicio> items, OnItemClickListener listener) {
            this.items = items; this.listener = listener;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_texto, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position) {
            TipoServicio item = items.get(position);
            h.txtDescripcion.setText(item.getDescripcion());
            h.itemView.setOnClickListener(v -> listener.onSeleccion(item));
            h.btnEditar.setOnClickListener(v -> listener.onEditar(item));
            h.btnBorrar.setOnClickListener(v -> listener.onBorrar(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtDescripcion;
            Button btnEditar, btnBorrar;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtDescripcion = itemView.findViewById(R.id.txt_item_descripcion);
                btnEditar = itemView.findViewById(R.id.btn_item_editar);
                btnBorrar = itemView.findViewById(R.id.btn_item_borrar);
            }
        }
    }

}
