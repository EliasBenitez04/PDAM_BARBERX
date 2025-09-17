package com.example.app_movil;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AgendamientoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private MaterialAutoCompleteTextView spinnerCliente, spinnerBarbero, spinnerEstado;
    private MaterialAutoCompleteTextView edtFechaReserva, edtFechaAtencion;
    private TextInputEditText edtObservacion;
    private RecyclerView rvServicios, rvAgendamientos;
    private MaterialButton btnAgregarServicio, btnGuardar, btnLimpiar, btnSalir;

    private List<ServicioDetalle> listaDetalle = new ArrayList<>();
    private ServiciosAdapter adapterDetalle;

    private List<AgendamientoItem> listaAgendamientos = new ArrayList<>();
    private AgendamientoAdapter adapterAgendamientos;

    private AdminSQLiteOpenHelper helper;

    private List<Integer> clienteIds = new ArrayList<>();
    private List<Integer> barberoIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamiento);

        helper = new AdminSQLiteOpenHelper(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_agendamiento);
        setSupportActionBar(toolbar);

        // Drawer
        drawer = findViewById(R.id.drawer_layout_agendamiento);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view_agendamiento);
        navigationView.setNavigationItemSelectedListener(this);

        // Campos
        spinnerCliente = findViewById(R.id.spinner_cliente);
        spinnerBarbero = findViewById(R.id.spinner_barbero);
        spinnerEstado = findViewById(R.id.spinner_estado);
        edtFechaReserva = findViewById(R.id.edt_fecha_reserva);
        edtFechaAtencion = findViewById(R.id.edt_fecha_atencion);
        edtObservacion = findViewById(R.id.edt_observacion);

        rvServicios = findViewById(R.id.rv_servicios);
        rvServicios.setLayoutManager(new LinearLayoutManager(this));
        adapterDetalle = new ServiciosAdapter(listaDetalle);
        rvServicios.setAdapter(adapterDetalle);

        rvAgendamientos = findViewById(R.id.rv_agendamientos);
        rvAgendamientos.setLayoutManager(new LinearLayoutManager(this));
        adapterAgendamientos = new AgendamientoAdapter(listaAgendamientos);
        rvAgendamientos.setAdapter(adapterAgendamientos);

        btnAgregarServicio = findViewById(R.id.btn_agregar_servicio);
        btnGuardar = findViewById(R.id.btn_guardar_agendamiento);
        btnLimpiar = findViewById(R.id.btn_limpiar_agendamiento);
        btnSalir = findViewById(R.id.btn_salir_agendamiento);

        cargarClientesDropdown();
        cargarBarberosDropdown();
        cargarEstadosDropdown();
        configurarFechas();

        btnAgregarServicio.setOnClickListener(v -> agregarServicioDetalle());
        btnLimpiar.setOnClickListener(v -> limpiarCampos());
        btnSalir.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarAgendamiento());

        // Cargar agendamientos guardados
        cargarAgendamientosGuardados();
    }

    // -------- Configuración de fechas y horas --------
    private void configurarFechas() {
        edtFechaReserva.setFocusable(false);
        edtFechaReserva.setClickable(true);
        edtFechaReserva.setOnClickListener(v -> mostrarDateTimePicker(edtFechaReserva));

        edtFechaAtencion.setFocusable(false);
        edtFechaAtencion.setClickable(true);
        edtFechaAtencion.setOnClickListener(v -> mostrarDateTimePicker(edtFechaAtencion));
    }

    private void mostrarDateTimePicker(MaterialAutoCompleteTextView edt) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        new DatePickerDialog(this, (view, y, m, d) -> {
            new TimePickerDialog(this, (timePicker, h, min) -> {
                edt.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d %02d:%02d", d, m+1, y, h, min));
            }, hour, minute, true).show();
        }, year, month, day).show();
    }

    // -------- Carga de clientes, barberos y estados --------
    private void cargarClientesDropdown() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT cli_cod, cli_nom || ' ' || cli_ape AS nombre FROM cliente", null);
        List<String> clientes = new ArrayList<>();
        clienteIds.clear();
        while (cursor.moveToNext()) {
            clientes.add(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            clienteIds.add(cursor.getInt(cursor.getColumnIndexOrThrow("cli_cod")));
        }
        cursor.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, clientes);
        spinnerCliente.setAdapter(adapter);
    }

    private void cargarBarberosDropdown() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT fun_cod, fun_nom || ' ' || fun_ape AS nombre FROM funcionario", null);
        List<String> barberos = new ArrayList<>();
        barberoIds.clear();
        while (cursor.moveToNext()) {
            barberos.add(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            barberoIds.add(cursor.getInt(cursor.getColumnIndexOrThrow("fun_cod")));
        }
        cursor.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, barberos);
        spinnerBarbero.setAdapter(adapter);
    }

    private void cargarEstadosDropdown() {
        String[] estados = {"PENDIENTE", "CONFIRMADO", "CANCELADO"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, estados);
        spinnerEstado.setAdapter(adapter);
        spinnerEstado.setText("PENDIENTE", false);
    }

    // -------- Servicios (detalle) --------
    private void agregarServicioDetalle() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT serv_cod, serv_desc, serv_prec FROM servicios", null);
        List<ServicioDetalle> servicios = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                servicios.add(new ServicioDetalle(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2)));
            } while (cursor.moveToNext());
        }
        cursor.close();

        String[] items = new String[servicios.size()];
        for (int i = 0; i < servicios.size(); i++)
            items[i] = servicios.get(i).descripcion + " | $" + servicios.get(i).precio;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Seleccionar Servicio")
                .setItems(items, (dialog, which) -> {
                    listaDetalle.add(servicios.get(which));
                    adapterDetalle.notifyDataSetChanged();
                }).show();
    }

    private void limpiarCampos() {
        spinnerCliente.setText("");
        spinnerBarbero.setText("");
        spinnerEstado.setText("PENDIENTE", false);
        edtFechaReserva.setText("");
        edtFechaAtencion.setText("");
        edtObservacion.setText("");
        listaDetalle.clear();
        adapterDetalle.notifyDataSetChanged();
    }

    // -------- Guardar agendamiento --------
    private void guardarAgendamiento() {
        String clienteSeleccionado = spinnerCliente.getText().toString();
        String barberoSeleccionado = spinnerBarbero.getText().toString();
        String estadoSeleccionado = spinnerEstado.getText().toString();
        String fechaReserva = edtFechaReserva.getText().toString();
        String fechaAtencion = edtFechaAtencion.getText().toString();
        String observacion = edtObservacion.getText().toString();

        if (clienteSeleccionado.isEmpty() || barberoSeleccionado.isEmpty() || fechaReserva.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int clientePos = -1;
        int barberoPos = -1;
        for (int i = 0; i < spinnerCliente.getAdapter().getCount(); i++) {
            if (spinnerCliente.getAdapter().getItem(i).toString().equals(clienteSeleccionado)) {
                clientePos = i;
                break;
            }
        }
        for (int i = 0; i < spinnerBarbero.getAdapter().getCount(); i++) {
            if (spinnerBarbero.getAdapter().getItem(i).toString().equals(barberoSeleccionado)) {
                barberoPos = i;
                break;
            }
        }

        if (clientePos < 0 || barberoPos < 0) {
            Toast.makeText(this, "Cliente o Barbero inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        int clienteId = clienteIds.get(clientePos);
        int barberoId = barberoIds.get(barberoPos);

        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            // Validar existencia
            Cursor cursorCheck = db.rawQuery("SELECT cli_cod FROM cliente WHERE cli_cod=?", new String[]{String.valueOf(clienteId)});
            if (!cursorCheck.moveToFirst()) { cursorCheck.close(); Toast.makeText(this, "Cliente no existe", Toast.LENGTH_SHORT).show(); return; }
            cursorCheck.close();

            cursorCheck = db.rawQuery("SELECT fun_cod FROM funcionario WHERE fun_cod=?", new String[]{String.valueOf(barberoId)});
            if (!cursorCheck.moveToFirst()) { cursorCheck.close(); Toast.makeText(this, "Barbero no existe", Toast.LENGTH_SHORT).show(); return; }
            cursorCheck.close();

            // Insertar agendamiento sin usuario
            db.execSQL(
                    "INSERT INTO agendamiento (cli_cod, fun_cod, agen_date_reserva, agen_date_serv, agen_estado) VALUES (?, ?, ?, ?, ?)",
                    new Object[]{clienteId, barberoId, fechaReserva, fechaAtencion, estadoSeleccionado}
            );

            // Obtener ID
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid() AS id", null);
            int agendamientoId = -1;
            if (cursor.moveToFirst()) agendamientoId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();

            // Insertar detalles
            for (ServicioDetalle s : listaDetalle) {
                db.execSQL(
                        "INSERT INTO det_agendamiento (agen_cod, serv_cod, det_agen_prec, det_agen_estado, det_agen_obs) VALUES (?, ?, ?, ?, ?)",
                        new Object[]{agendamientoId, s.codigo, s.precio, estadoSeleccionado, observacion}
                );
            }

            Toast.makeText(this, "Agendamiento guardado", Toast.LENGTH_SHORT).show();
            limpiarCampos();
            cargarAgendamientosGuardados();

        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
    }


    // -------- Lista de agendamientos guardados --------
    private void cargarAgendamientosGuardados() {
        listaAgendamientos.clear();
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT a.agen_cod, c.cli_nom || ' ' || c.cli_ape AS cliente, " +
                        "f.fun_nom || ' ' || f.fun_ape AS barbero, a.agen_date_reserva, a.agen_date_serv " +
                        "FROM agendamiento a " +
                        "INNER JOIN cliente c ON a.cli_cod = c.cli_cod " +
                        "INNER JOIN funcionario f ON a.fun_cod = f.fun_cod " +
                        "ORDER BY a.agen_cod DESC", null);

        while (cursor.moveToNext()) {
            listaAgendamientos.add(new AgendamientoItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow("agen_cod")),
                    cursor.getString(cursor.getColumnIndexOrThrow("cliente")),
                    cursor.getString(cursor.getColumnIndexOrThrow("barbero")),
                    cursor.getString(cursor.getColumnIndexOrThrow("agen_fecha_reserva")),
                    cursor.getString(cursor.getColumnIndexOrThrow("agen_fecha_atencion")),
                    cursor.getString(cursor.getColumnIndexOrThrow("agen_estado"))
            ));
        }
        cursor.close();
        adapterAgendamientos.notifyDataSetChanged();
    }

    // -------- RecyclerView Adapter Servicios --------
    private class ServiciosAdapter extends RecyclerView.Adapter<ServiciosAdapter.ServicioViewHolder> {
        private List<ServicioDetalle> lista;

        ServiciosAdapter(List<ServicioDetalle> l) { lista = l; }

        @NonNull
        @Override
        public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout ll = new LinearLayout(parent.getContext());
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setPadding(8, 8, 8, 8);

            TextView tv = new TextView(parent.getContext());
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            MaterialButton btnEditar = new MaterialButton(parent.getContext());
            btnEditar.setText("Editar");

            MaterialButton btnBorrar = new MaterialButton(parent.getContext());
            btnBorrar.setText("Borrar");

            ll.addView(tv);
            ll.addView(btnEditar);
            ll.addView(btnBorrar);

            return new ServicioViewHolder(ll, tv, btnEditar, btnBorrar);
        }

        @Override
        public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
            ServicioDetalle s = lista.get(position);
            holder.tv.setText(s.descripcion + " | Precio: " + s.precio);

            holder.btnEditar.setOnClickListener(v -> editarServicio(position));
            holder.btnBorrar.setOnClickListener(v -> {
                lista.remove(position);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() { return lista.size(); }

        class ServicioViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            MaterialButton btnEditar, btnBorrar;

            public ServicioViewHolder(@NonNull View itemView, TextView t, MaterialButton e, MaterialButton b) {
                super(itemView);
                tv = t;
                btnEditar = e;
                btnBorrar = b;
            }
        }
    }

    private void editarServicio(int position) {
        ServicioDetalle s = listaDetalle.get(position);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Editar Servicio: " + s.descripcion);

        final TextInputEditText input = new TextInputEditText(this);
        input.setText(String.valueOf(s.precio));
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            s.precio = Double.parseDouble(input.getText().toString());
            adapterDetalle.notifyItemChanged(position);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // -------- Modelos --------
    private class ServicioDetalle {
        int codigo;
        String descripcion;
        double precio;

        ServicioDetalle(int c, String d, double p) {
            codigo = c;
            descripcion = d;
            precio = p;
        }
    }

    private class AgendamientoItem {
        int id;
        String cliente, barbero, fechaReserva, fechaAtencion, estado;

        AgendamientoItem(int i, String c, String b, String fr, String fa, String e) {
            id = i;
            cliente = c;
            barbero = b;
            fechaReserva = fr;
            fechaAtencion = fa;
            estado = e;
        }
    }

    // -------- Adapter Agendamientos guardados --------
    private class AgendamientoAdapter extends RecyclerView.Adapter<AgendamientoAdapter.AgendamientoViewHolder> {
        private List<AgendamientoItem> lista;

        AgendamientoAdapter(List<AgendamientoItem> l) { lista = l; }

        @NonNull
        @Override
        public AgendamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout ll = new LinearLayout(parent.getContext());
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setPadding(8, 8, 8, 8);

            TextView tvInfo = new TextView(parent.getContext());
            tvInfo.setTextSize(14f);
            tvInfo.setPadding(4, 4, 4, 4);

            MaterialButton btnBorrar = new MaterialButton(parent.getContext());
            btnBorrar.setText("Borrar");

            ll.addView(tvInfo);
            ll.addView(btnBorrar);

            return new AgendamientoViewHolder(ll, tvInfo, btnBorrar);
        }

        @Override
        public void onBindViewHolder(@NonNull AgendamientoViewHolder holder, int position) {
            AgendamientoItem a = lista.get(position);
            holder.tv.setText("Cliente: " + a.cliente + "\nBarbero: " + a.barbero +
                    "\nReserva: " + a.fechaReserva + "\nAtención: " + a.fechaAtencion +
                    "\nEstado: " + a.estado);

            holder.btnBorrar.setOnClickListener(v -> {
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete("agendamiento", "agen_cod=?", new String[]{String.valueOf(a.id)});
                db.delete("det_agendamiento", "agen_cod=?", new String[]{String.valueOf(a.id)});
                lista.remove(position);
                notifyDataSetChanged();
                Toast.makeText(AgendamientoActivity.this, "Agendamiento borrado", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return lista.size(); }

        class AgendamientoViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            MaterialButton btnBorrar;

            public AgendamientoViewHolder(@NonNull View itemView, TextView t, MaterialButton b) {
                super(itemView);
                tv = t;
                btnBorrar = b;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }
}
