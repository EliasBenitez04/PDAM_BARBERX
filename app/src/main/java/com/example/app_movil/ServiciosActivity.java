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
import android.widget.Spinner;
import android.widget.ArrayAdapter;
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

public class ServiciosActivity extends AppCompatActivity {

    private Spinner spnTipoServicio;
    private TextInputEditText edtServicio, edtPrecio;
    private MaterialButton btnGuardar, btnLimpiar, btnSalir;
    private RecyclerView rvServicios;

    private AdminSQLiteOpenHelper dbHelper;
    private ServicioAdapter adapter;
    private ArrayList<Servicio> listaServicios;
    private Servicio servicioSeleccionado = null;
    private ArrayList<TipoServicio> listaTipos; // para llenar el spinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios);

        Toolbar toolbar = findViewById(R.id.toolbar_servicio);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ==============================================
        // findViewById seguro y verificación de null
        // ==============================================
        spnTipoServicio = findViewById(R.id.spn_tipo_servicio);
        if (spnTipoServicio == null) {
            Toast.makeText(this, "Error: Spinner no encontrado en layout", Toast.LENGTH_LONG).show();
            return; // evita que la app crashee
        }

        edtServicio = findViewById(R.id.edt_servicio);
        edtPrecio = findViewById(R.id.edt_precio);
        btnGuardar = findViewById(R.id.btn_guardar_servicio);
        btnLimpiar = findViewById(R.id.btn_limpiar_servicio);
        btnSalir = findViewById(R.id.btn_salir_servicio);
        rvServicios = findViewById(R.id.rv_servicios);

        dbHelper = new AdminSQLiteOpenHelper(this);

        listaServicios = new ArrayList<>();
        listaTipos = new ArrayList<>();

        cargarTiposServicio(); // llena el spinner

        adapter = new ServicioAdapter(listaServicios, new ServicioAdapter.OnItemClickListener() {
            @Override
            public void onEditar(Servicio servicio) {
                servicioSeleccionado = servicio;
                cargarServicioEnCampos();
            }

            @Override
            public void onBorrar(Servicio servicio) {
                servicioSeleccionado = servicio;
                borrarServicio();
            }

            @Override
            public void onSeleccion(Servicio servicio) {
                servicioSeleccionado = servicio;
                cargarServicioEnCampos();
            }
        });

        rvServicios.setLayoutManager(new LinearLayoutManager(this));
        rvServicios.setAdapter(adapter);

        cargarServicios();

        btnGuardar.setOnClickListener(v -> guardarServicio());
        btnLimpiar.setOnClickListener(v -> limpiarCampos());
        btnSalir.setOnClickListener(v -> finish());
    }

    private void cargarTiposServicio() {
        listaTipos.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT tip_serv_cod, tip_serv_desc FROM tipo_servicios ORDER BY tip_serv_desc ASC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                listaTipos.add(new TipoServicio(cursor.getInt(0), cursor.getString(1)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        // ==============================================
        // ArrayAdapter seguro para Spinner
        // ==============================================
        ArrayList<String> nombresTipos = new ArrayList<>();
        if (listaTipos.isEmpty()) {
            nombresTipos.add("[Sin Tipos]");
        } else {
            for (TipoServicio t : listaTipos) nombresTipos.add(t.getDescripcion());
        }

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresTipos);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnTipoServicio.setAdapter(adapterSpinner);
    }

    private void cargarServicios() {
        listaServicios.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT s.serv_cod, s.tip_serv_cod, s.serv_desc, s.serv_prec, t.tip_serv_desc " +
                        "FROM servicios s " +
                        "INNER JOIN tipo_servicios t ON s.tip_serv_cod = t.tip_serv_cod " +
                        "ORDER BY s.serv_desc ASC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                listaServicios.add(new Servicio(
                        cursor.getInt(0),       // serv_cod
                        cursor.getInt(1),       // tip_serv_cod
                        cursor.getString(2),    // serv_desc
                        cursor.getDouble(3),    // serv_prec
                        cursor.getString(4)     // tipo_desc
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        adapter.notifyDataSetChanged();
    }

    private void cargarServicioEnCampos() {
        if (servicioSeleccionado != null) {
            edtServicio.setText(servicioSeleccionado.getDescripcion());
            edtPrecio.setText(String.valueOf(servicioSeleccionado.getPrecio()));
            for (int i = 0; i < listaTipos.size(); i++) {
                if (listaTipos.get(i).getId() == servicioSeleccionado.getTipoId()) {
                    spnTipoServicio.setSelection(i);
                    break;
                }
            }
        }
    }

    private void limpiarCampos() {
        edtServicio.setText("");
        edtPrecio.setText("");
        spnTipoServicio.setSelection(0);
        servicioSeleccionado = null;
    }

    private void guardarServicio() {
        String desc = edtServicio.getText() != null ? edtServicio.getText().toString().trim().toUpperCase() : "";
        String precioStr = edtPrecio.getText() != null ? edtPrecio.getText().toString().trim() : "";

        if (desc.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese un precio válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (listaTipos.isEmpty()) {
            Toast.makeText(this, "No hay tipos de servicio disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        int tipoId = listaTipos.get(spnTipoServicio.getSelectedItemPosition()).getId();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (servicioSeleccionado == null) {
            Cursor check = db.rawQuery("SELECT serv_cod FROM servicios WHERE UPPER(serv_desc)=?", new String[]{desc});
            if (check.moveToFirst()) {
                check.close();
                db.close();
                Toast.makeText(this, "Ya existe un servicio con esa descripción", Toast.LENGTH_SHORT).show();
                return;
            }
            check.close();
            db.execSQL("INSERT INTO servicios (tip_serv_cod, serv_desc, serv_prec) VALUES (?,?,?)",
                    new Object[]{tipoId, desc, precio});
            Toast.makeText(this, "Servicio guardado", Toast.LENGTH_SHORT).show();
        } else {
            Cursor check = db.rawQuery("SELECT serv_cod FROM servicios WHERE UPPER(serv_desc)=? AND serv_cod<>?",
                    new String[]{desc, String.valueOf(servicioSeleccionado.getId())});
            if (check.moveToFirst()) {
                check.close();
                db.close();
                Toast.makeText(this, "Ya existe otro servicio con esa descripción", Toast.LENGTH_SHORT).show();
                return;
            }
            check.close();

            db.execSQL("UPDATE servicios SET tip_serv_cod=?, serv_desc=?, serv_prec=? WHERE serv_cod=?",
                    new Object[]{tipoId, desc, precio, servicioSeleccionado.getId()});
            Toast.makeText(this, "Servicio actualizado", Toast.LENGTH_SHORT).show();
        }

        db.close();
        limpiarCampos();
        cargarServicios();
    }

    private void borrarServicio() {
        if (servicioSeleccionado == null) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM servicios WHERE serv_cod=?", new Object[]{servicioSeleccionado.getId()});
        db.close();
        Toast.makeText(this, "Servicio eliminado", Toast.LENGTH_SHORT).show();
        limpiarCampos();
        cargarServicios();
    }


/* ===================== CLASES INTERNAS ===================== */

    private static class Servicio {
        private int id;
        private int tipoId;
        private String descripcion;
        private double precio;
        private String tipoDescripcion; // para mostrar en RecyclerView

        public Servicio(int id, int tipoId, String descripcion, double precio, String tipoDescripcion) {
            this.id = id;
            this.tipoId = tipoId;
            this.descripcion = descripcion;
            this.precio = precio;
            this.tipoDescripcion = tipoDescripcion;
        }

        public int getId() { return id; }
        public int getTipoId() { return tipoId; }
        public String getDescripcion() { return descripcion; }
        public double getPrecio() { return precio; }
        public String getTipoDescripcion() { return tipoDescripcion; }

        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public void setPrecio(double precio) { this.precio = precio; }
        public void setTipoId(int tipoId) { this.tipoId = tipoId; }
        public void setTipoDescripcion(String tipoDescripcion) { this.tipoDescripcion = tipoDescripcion; }
    }

    private static class TipoServicio {
        private int id;
        private String descripcion;

        public TipoServicio(int id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        public int getId() { return id; }
        public String getDescripcion() { return descripcion; }
    }

    private static class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder> {

        interface OnItemClickListener {
            void onEditar(Servicio servicio);
            void onBorrar(Servicio servicio);
            void onSeleccion(Servicio servicio);
        }

        private final List<Servicio> lista;
        private final OnItemClickListener listener;

        public ServicioAdapter(List<Servicio> lista, OnItemClickListener listener) {
            this.lista = lista;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_servicio, parent, false);
            return new ServicioViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
            Servicio s = lista.get(position);
            holder.tvDescripcion.setText(s.getDescripcion());
            holder.tvTipo.setText(s.getTipoDescripcion());
            holder.tvPrecio.setText(String.valueOf(s.getPrecio()));

            holder.btnEditar.setOnClickListener(v -> listener.onEditar(s));
            holder.btnBorrar.setOnClickListener(v -> listener.onBorrar(s));
            holder.itemView.setOnClickListener(v -> listener.onSeleccion(s));
        }

        @Override
        public int getItemCount() { return lista.size(); }

        static class ServicioViewHolder extends RecyclerView.ViewHolder {
            TextView tvDescripcion, tvTipo, tvPrecio;
            Button btnEditar, btnBorrar;

            ServicioViewHolder(View itemView) {
                super(itemView);
                tvDescripcion = itemView.findViewById(R.id.txt_item_descripcion);
                tvTipo = itemView.findViewById(R.id.txt_item_tipo);
                tvPrecio = itemView.findViewById(R.id.txt_item_precio);
                btnEditar = itemView.findViewById(R.id.btn_item_editar);
                btnBorrar = itemView.findViewById(R.id.btn_item_borrar);
            }
        }
    }

}
