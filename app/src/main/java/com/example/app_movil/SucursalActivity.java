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

public class SucursalActivity extends AppCompatActivity {

    private TextInputEditText edtSucursal;
    private MaterialButton btnGuardar, btnLimpiar, btnSalir;
    private RecyclerView rvSucursales;

    private AdminSQLiteOpenHelper dbHelper;   // instancia global
    private SucursalAdapter adapter;
    private ArrayList<Sucursal> listaSucursales;
    private Sucursal sucursalSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sucursal);

        Toolbar toolbar = findViewById(R.id.toolbar_sucursal);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtSucursal = findViewById(R.id.edt_sucursal);
        btnGuardar = findViewById(R.id.btn_guardar_sucursal);
        btnLimpiar = findViewById(R.id.btn_limpiar_sucursal);
        btnSalir = findViewById(R.id.btn_salir_sucursal);
        rvSucursales = findViewById(R.id.rv_sucursales);

        // Usamos el helper global (constructor sin parámetros adicionales)
        dbHelper = new AdminSQLiteOpenHelper(this);

        listaSucursales = new ArrayList<>();
        adapter = new SucursalAdapter(listaSucursales, new SucursalAdapter.OnItemClickListener() {
            @Override public void onEditar(Sucursal sucursal) {
                sucursalSeleccionada = sucursal;
                guardarSucursal();
            }
            @Override public void onBorrar(Sucursal sucursal) {
                sucursalSeleccionada = sucursal;
                borrarSucursal();
            }
            @Override public void onSeleccion(Sucursal sucursal) {
                sucursalSeleccionada = sucursal;
                edtSucursal.setText(sucursal.getDescripcion());
            }
        });

        rvSucursales.setLayoutManager(new LinearLayoutManager(this));
        rvSucursales.setAdapter(adapter);

        cargarSucursales();

        btnGuardar.setOnClickListener(v -> guardarSucursal());
        btnLimpiar.setOnClickListener(v -> {
            edtSucursal.setText("");
            sucursalSeleccionada = null;
        });
        btnSalir.setOnClickListener(v -> finish());
    }

    private void cargarSucursales() {
        listaSucursales.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT suc_cod, suc_desc FROM sucursal ORDER BY suc_desc ASC", null);

        if (cursor.moveToFirst()) {
            do {
                listaSucursales.add(new Sucursal(cursor.getInt(0), cursor.getString(1)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void guardarSucursal() {
        String desc = edtSucursal.getText() != null ? edtSucursal.getText().toString().trim().toUpperCase() : "";
        if (desc.isEmpty()) {
            Toast.makeText(this, "Ingrese una sucursal", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor check;
        if (sucursalSeleccionada != null) {
            // Editar: verificar duplicados excluyendo la actual
            check = db.rawQuery("SELECT suc_cod FROM sucursal WHERE UPPER(suc_desc)=? AND suc_cod<>?",
                    new String[]{desc, String.valueOf(sucursalSeleccionada.getId())});
            if (check.moveToFirst()) { check.close(); db.close(); Toast.makeText(this,"Ya existe otra sucursal con esa descripción",Toast.LENGTH_SHORT).show(); return; }
            check.close(); db.close();

            db = dbHelper.getWritableDatabase();
            db.execSQL("UPDATE sucursal SET suc_desc=? WHERE suc_cod=?", new Object[]{desc, sucursalSeleccionada.getId()});
            db.close();
            Toast.makeText(this, "Sucursal actualizada", Toast.LENGTH_SHORT).show();
        } else {
            // Nuevo: verificar duplicados
            check = db.rawQuery("SELECT suc_cod FROM sucursal WHERE UPPER(suc_desc)=?", new String[]{desc});
            if (check.moveToFirst()) { check.close(); db.close(); Toast.makeText(this,"Ya existe una sucursal con esa descripción",Toast.LENGTH_SHORT).show(); return; }
            check.close(); db.close();

            db = dbHelper.getWritableDatabase();
            db.execSQL("INSERT INTO sucursal (suc_desc) VALUES (?)", new Object[]{desc});
            db.close();
            Toast.makeText(this, "Sucursal guardada", Toast.LENGTH_SHORT).show();
        }

        edtSucursal.setText("");
        sucursalSeleccionada = null;
        cargarSucursales();
    }

    private void borrarSucursal() {
        if (sucursalSeleccionada == null) { Toast.makeText(this,"Seleccione una sucursal primero",Toast.LENGTH_SHORT).show(); return; }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM sucursal WHERE suc_cod=?", new Object[]{sucursalSeleccionada.getId()});
        db.close();

        Toast.makeText(this, "Sucursal eliminada", Toast.LENGTH_SHORT).show();
        edtSucursal.setText("");
        sucursalSeleccionada = null;
        cargarSucursales();
    }

    /* ===================== CLASES INTERNAS ===================== */

    private static class Sucursal {
        private int id;
        private String descripcion;

        public Sucursal(int id, String descripcion) { this.id = id; this.descripcion = descripcion; }
        public int getId() { return id; }
        public String getDescripcion() { return descripcion; }
    }

    private static class SucursalAdapter extends RecyclerView.Adapter<SucursalAdapter.ViewHolder> {

        interface OnItemClickListener { void onEditar(Sucursal sucursal); void onBorrar(Sucursal sucursal); void onSeleccion(Sucursal sucursal); }
        private final List<Sucursal> lista; private final OnItemClickListener listener;
        public SucursalAdapter(List<Sucursal> lista, OnItemClickListener listener){this.lista=lista;this.listener=listener;}

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_texto,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Sucursal s = lista.get(position);
            holder.txtDescripcion.setText(s.getDescripcion());
            holder.itemView.setOnClickListener(v->listener.onSeleccion(s));
            holder.btnEditar.setOnClickListener(v->listener.onEditar(s));
            holder.btnBorrar.setOnClickListener(v->listener.onBorrar(s));
        }

        @Override
        public int getItemCount() { return lista.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtDescripcion; Button btnEditar, btnBorrar;
            public ViewHolder(@NonNull View itemView){
                super(itemView);
                txtDescripcion = itemView.findViewById(R.id.txt_item_descripcion);
                btnEditar = itemView.findViewById(R.id.btn_item_editar);
                btnBorrar = itemView.findViewById(R.id.btn_item_borrar);
            }
        }
    }
}
