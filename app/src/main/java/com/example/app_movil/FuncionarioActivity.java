package com.example.app_movil;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FuncionarioActivity extends AppCompatActivity {

    // UI
    private MaterialAutoCompleteTextView spSucursal, spCargo, spEstado;
    private TextInputEditText edtNombre, edtApellido, edtCI, edtTelefono, edtCorreo, edtFecha;
    private MaterialButton btnGuardar, btnLimpiar, btnSalir;
    private RecyclerView rvFuncionarios;

    // DB
    private AdminSQLiteOpenHelper dbHelper;

    // Datos referenciales
    private final List<RefItem> sucursales = new ArrayList<>();
    private final List<RefItem> cargos = new ArrayList<>();
    private Integer sucursalIdSeleccionada = null;
    private Integer cargoIdSeleccionado = null;

    // Lista + selección
    private final ArrayList<FuncionarioRow> lista = new ArrayList<>();
    private FuncionarioAdapter adapter;
    private FuncionarioRow seleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funcionario);

        Toolbar toolbar = findViewById(R.id.toolbar_funcionario);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Bind UI
        spSucursal = findViewById(R.id.spinner_sucursal);
        spCargo = findViewById(R.id.spinner_cargo);
        spEstado = findViewById(R.id.spinner_estado_funcionario);
        edtNombre = findViewById(R.id.edt_nombre_funcionario);
        edtApellido = findViewById(R.id.edt_apellido_funcionario);
        edtCI = findViewById(R.id.edt_ci_funcionario);
        edtTelefono = findViewById(R.id.edt_telefono_funcionario);
        edtCorreo = findViewById(R.id.edt_correo_funcionario);
        edtFecha = findViewById(R.id.edt_fecha_contratacion);
        btnGuardar = findViewById(R.id.btn_guardar_funcionario);
        btnLimpiar = findViewById(R.id.btn_limpiar_funcionario);
        btnSalir = findViewById(R.id.btn_salir_funcionario);
        rvFuncionarios = findViewById(R.id.rv_funcionarios);

        // Usar helper global
        dbHelper = new AdminSQLiteOpenHelper(this);

        // Adapter
        adapter = new FuncionarioAdapter(lista, new FuncionarioAdapter.OnItemClickListener() {
            @Override public void onSeleccion(FuncionarioRow f) {
                seleccionado = f;
                setSpinnerById(spSucursal, sucursales, f.sucCod);
                setSpinnerById(spCargo, cargos, f.carCod);
                sucursalIdSeleccionada = f.sucCod;
                cargoIdSeleccionado = f.carCod;

                edtNombre.setText(f.nom);
                edtApellido.setText(f.ape);
                edtCI.setText(f.ci);
                edtTelefono.setText(f.tel);
                edtCorreo.setText(f.correo);
                spEstado.setText(f.estado, false);
                edtFecha.setText(f.fecha);
            }
            @Override public void onEditar(FuncionarioRow f) {
                seleccionado = f;
                guardarFuncionario(true);
            }
            @Override public void onBorrar(FuncionarioRow f) {
                seleccionado = f;
                borrarFuncionario();
            }
        });
        rvFuncionarios.setLayoutManager(new LinearLayoutManager(this));
        rvFuncionarios.setAdapter(adapter);

        // Cargar referenciales y lista
        cargarSucursales();
        cargarCargos();
        configurarEstado();
        cargarLista();

        setupDropdown(spSucursal);
        setupDropdown(spCargo);
        setupDropdown(spEstado);

        edtFecha.setOnClickListener(v -> mostrarDatePicker());

        spSucursal.setOnItemClickListener((p, v, pos, id) -> {
            if (pos >= 0 && pos < sucursales.size()) sucursalIdSeleccionada = sucursales.get(pos).id;
        });
        spCargo.setOnItemClickListener((p, v, pos, id) -> {
            if (pos >= 0 && pos < cargos.size()) cargoIdSeleccionado = cargos.get(pos).id;
        });

        btnGuardar.setOnClickListener(v -> guardarFuncionario(false));
        btnLimpiar.setOnClickListener(v -> limpiar());
        btnSalir.setOnClickListener(v -> finish());
    }

    /* ===================== CARGA ===================== */
    private void cargarSucursales() {
        sucursales.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT suc_cod, suc_desc FROM sucursal ORDER BY suc_desc ASC", null);
        if (c.moveToFirst()) do { sucursales.add(new RefItem(c.getInt(0), c.getString(1))); } while (c.moveToNext());
        c.close(); db.close();
        spSucursal.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mapNombres(sucursales)));
        spSucursal.setText("", false);
    }

    private void cargarCargos() {
        cargos.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT car_cod, car_desc FROM cargo ORDER BY car_desc ASC", null);
        if (c.moveToFirst()) do { cargos.add(new RefItem(c.getInt(0), c.getString(1))); } while (c.moveToNext());
        c.close(); db.close();
        spCargo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mapNombres(cargos)));
        spCargo.setText("", false);
    }

    private void configurarEstado() {
        String[] estados = {"ACTIVO", "INACTIVO"};
        spEstado.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, estados));
        spEstado.setText("ACTIVO", false);
    }

    private void cargarLista() {
        lista.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT f.fun_cod, f.suc_cod, s.suc_desc, f.car_cod, c.car_desc, " +
                        "f.fun_nom, f.fun_ape, f.fun_ci, f.fun_tel, f.fun_correo, f.fun_estado, f.fecha_contratacion " +
                        "FROM funcionario f " +
                        "JOIN sucursal s ON s.suc_cod=f.suc_cod " +
                        "JOIN cargo c ON c.car_cod=f.car_cod " +
                        "ORDER BY f.fun_ape ASC, f.fun_nom ASC", null);
        if (c.moveToFirst()) do {
            lista.add(new FuncionarioRow(
                    c.getInt(0), c.getInt(1), c.getString(2),
                    c.getInt(3), c.getString(4),
                    c.getString(5), c.getString(6), c.getString(7),
                    c.getString(8), c.getString(9), c.getString(10), c.getString(11)
            ));
        } while (c.moveToNext());
        c.close(); db.close();
        adapter.notifyDataSetChanged();
    }

    /* ===================== CRUD ===================== */
    private void guardarFuncionario(boolean desdeEditarBoton) {
        if (sucursalIdSeleccionada == null) { toast("Seleccione una sucursal"); return; }
        if (cargoIdSeleccionado == null) { toast("Seleccione un cargo"); return; }

        String nom = safeUpper(edtNombre);
        String ape = safeUpper(edtApellido);
        String ci  = safeText(edtCI);
        String tel = safeText(edtTelefono);
        String correo = safeLower(edtCorreo);
        String estado = safeText(spEstado);
        String fecha = safeText(edtFecha);

        if (nom.isEmpty()) { toast("Ingrese el nombre"); return; }
        if (ape.isEmpty()) { toast("Ingrese el apellido"); return; }
        if (ci.isEmpty())  { toast("Ingrese CI"); return; }
        if (tel.isEmpty()) { toast("Ingrese teléfono"); return; }
        if (correo.isEmpty()) { toast("Ingrese correo"); return; }
        if (fecha.isEmpty()) { toast("Seleccione la fecha de contratación"); return; }
        if (ci.length() < 5) { toast("CI demasiado corto"); return; }
        if (tel.length() < 6) { toast("Teléfono demasiado corto"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { toast("Correo inválido"); return; }
        if (!estado.equals("ACTIVO") && !estado.equals("INACTIVO")) { toast("Estado inválido"); return; }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor chk;

        if (seleccionado == null) {
            chk = db.rawQuery("SELECT fun_cod FROM funcionario WHERE fun_ci=? OR LOWER(fun_correo)=?", new String[]{ci, correo});
            if (chk.moveToFirst()) { chk.close(); db.close(); toast("CI o correo ya registrados"); return; }
            chk.close(); db.close();

            db = dbHelper.getWritableDatabase();
            db.execSQL("INSERT INTO funcionario (suc_cod, car_cod, fun_nom, fun_ape, fun_ci, fun_tel, fun_correo, fun_estado, fecha_contratacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{sucursalIdSeleccionada, cargoIdSeleccionado, nom, ape, ci, tel, correo, estado, fecha});
            db.close();
            toast("Funcionario guardado");
        } else {
            chk = db.rawQuery("SELECT fun_cod FROM funcionario WHERE (fun_ci=? OR LOWER(fun_correo)=?) AND fun_cod<>?",
                    new String[]{ci, correo, String.valueOf(seleccionado.id)});
            if (chk.moveToFirst()) { chk.close(); db.close(); toast("CI o correo ya registrados en otro funcionario"); return; }
            chk.close(); db.close();

            db = dbHelper.getWritableDatabase();
            db.execSQL("UPDATE funcionario SET suc_cod=?, car_cod=?, fun_nom=?, fun_ape=?, fun_ci=?, fun_tel=?, fun_correo=?, fun_estado=?, fecha_contratacion=? WHERE fun_cod=?",
                    new Object[]{sucursalIdSeleccionada, cargoIdSeleccionado, nom, ape, ci, tel, correo, estado, fecha, seleccionado.id});
            db.close();
            toast("Funcionario actualizado");
        }

        limpiar();
        cargarLista();
        if (desdeEditarBoton) seleccionado = null;
    }

    private void borrarFuncionario() {
        if (seleccionado == null) { toast("Seleccione un registro primero"); return; }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM funcionario WHERE fun_cod=?", new Object[]{seleccionado.id});
        db.close();
        toast("Funcionario eliminado");
        limpiar();
        cargarLista();
        seleccionado = null;
    }

    /* ===================== UTIL ===================== */
    private void mostrarDatePicker() {
        final Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR), m = cal.get(Calendar.MONTH), d = cal.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, (view, year, month, day) -> {
            edtFecha.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month+1, day));
        }, y, m, d).show();
    }

    private void limpiar() {
        spSucursal.setText("", false);
        spCargo.setText("", false);
        spEstado.setText("ACTIVO", false);
        sucursalIdSeleccionada = null;
        cargoIdSeleccionado = null;

        edtNombre.setText("");
        edtApellido.setText("");
        edtCI.setText("");
        edtTelefono.setText("");
        edtCorreo.setText("");
        edtFecha.setText("");
        edtNombre.requestFocus();

        seleccionado = null;
    }

    private void setupDropdown(MaterialAutoCompleteTextView v) {
        v.setOnClickListener(x -> v.showDropDown());
        v.setOnFocusChangeListener((x, hasFocus) -> { if (hasFocus) v.showDropDown(); });
    }

    private List<String> mapNombres(List<RefItem> list) {
        List<String> nombres = new ArrayList<>();
        for (RefItem it : list) nombres.add(it.nombre);
        return nombres;
    }

    private void setSpinnerById(MaterialAutoCompleteTextView spinner, List<RefItem> data, int id) {
        for (int i=0;i<data.size();i++) if (data.get(i).id==id) { spinner.setText(data.get(i).nombre,false); return; }
        spinner.setText("", false);
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }

    private String safeUpper(TextInputEditText e) { return e.getText()==null?"":e.getText().toString().trim().toUpperCase(Locale.ROOT); }
    private String safeText(TextInputEditText e) { return e.getText()==null?"":e.getText().toString().trim(); }
    private String safeText(MaterialAutoCompleteTextView e) { return e.getText()==null?"":e.getText().toString().trim(); }
    private String safeLower(TextInputEditText e) { return e.getText()==null?"":e.getText().toString().trim().toLowerCase(Locale.ROOT); }

    /* ===================== MODELOS ===================== */
    private static class RefItem { final int id; final String nombre; RefItem(int id,String nombre){this.id=id;this.nombre=nombre;} @NonNull @Override public String toString(){return nombre;} }
    private static class FuncionarioRow {
        final int id, sucCod, carCod;
        final String sucDesc, carDesc, nom, ape, ci, tel, correo, estado, fecha;
        FuncionarioRow(int id,int sucCod,String sucDesc,int carCod,String carDesc,String nom,String ape,String ci,String tel,String correo,String estado,String fecha){
            this.id=id; this.sucCod=sucCod; this.carCod=carCod; this.sucDesc=sucDesc; this.carDesc=carDesc;
            this.nom=nom; this.ape=ape; this.ci=ci; this.tel=tel; this.correo=correo; this.estado=estado; this.fecha=fecha;
        }
    }

    /* ===================== ADAPTER ===================== */
    private static class FuncionarioAdapter extends RecyclerView.Adapter<FuncionarioAdapter.VH> {
        interface OnItemClickListener { void onSeleccion(FuncionarioRow item); void onEditar(FuncionarioRow item); void onBorrar(FuncionarioRow item); }
        private final List<FuncionarioRow> data; private final OnItemClickListener listener;
        FuncionarioAdapter(List<FuncionarioRow> data, OnItemClickListener listener){this.data=data;this.listener=listener;}
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_funcionario,parent,false); return new VH(v);}
        @Override public void onBindViewHolder(@NonNull VH h,int pos){FuncionarioRow f=data.get(pos);h.txtLinea1.setText(f.ape+", "+f.nom);h.txtLinea2.setText("CI: "+f.ci+"  •  Tel: "+f.tel);h.txtLinea3.setText(f.sucDesc+" • "+f.carDesc+" • "+f.estado+" • "+f.fecha);h.itemView.setOnClickListener(v->listener.onSeleccion(f)); h.btnEditar.setOnClickListener(v->listener.onEditar(f)); h.btnBorrar.setOnClickListener(v->listener.onBorrar(f));}
        @Override public int getItemCount(){return data.size();}
        static class VH extends RecyclerView.ViewHolder{TextView txtLinea1,txtLinea2,txtLinea3;Button btnEditar,btnBorrar;VH(@NonNull View itemView){super(itemView);txtLinea1=itemView.findViewById(R.id.txt_fun_linea1);txtLinea2=itemView.findViewById(R.id.txt_fun_linea2);txtLinea3=itemView.findViewById(R.id.txt_fun_linea3);btnEditar=itemView.findViewById(R.id.btn_item_editar);btnBorrar=itemView.findViewById(R.id.btn_item_borrar);}}
    }
}
