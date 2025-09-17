package com.example.app_movil;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ClienteActivity extends AppCompatActivity {

    private Spinner spnCiudad;
    private EditText edtNombre, edtApellido, edtCI, edtTelefono;
    private MaterialButton btnGuardar, btnLimpiar, btnSalir;
    private RecyclerView rvClientes;

    private ArrayList<String> listaCiudades;
    private ArrayList<Integer> listaCiuCod;
    private List<Cliente> listaClientes;
    private ClienteAdapter clienteAdapter;

    private Cliente clienteSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        spnCiudad = findViewById(R.id.spinner_ciudad_cliente);
        edtNombre = findViewById(R.id.edt_nombre_cliente);
        edtApellido = findViewById(R.id.edt_apellido_cliente);
        edtCI = findViewById(R.id.edt_ci_cliente);
        edtTelefono = findViewById(R.id.edt_telefono_cliente);
        btnGuardar = findViewById(R.id.btn_guardar_cliente);
        btnLimpiar = findViewById(R.id.btn_limpiar_cliente);
        btnSalir = findViewById(R.id.btn_salir_cliente);
        rvClientes = findViewById(R.id.rv_clientes);

        rvClientes.setLayoutManager(new LinearLayoutManager(this));
        listaClientes = new ArrayList<>();

        cargarCiudades();
        listarClientes();

        btnGuardar.setOnClickListener(v -> guardarCliente());
        btnLimpiar.setOnClickListener(v -> limpiarCampos());
        btnSalir.setOnClickListener(v -> finish());

        MaterialToolbar toolbar = findViewById(R.id.toolbar_cliente);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void cargarCiudades() {
        listaCiudades = new ArrayList<>();
        listaCiuCod = new ArrayList<>();

        AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(this);

        SQLiteDatabase db = conn.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT ciu_cod, ciu_desc FROM ciudad", null);
        if(cursor.moveToFirst()){
            do{
                listaCiuCod.add(cursor.getInt(0));
                listaCiudades.add(cursor.getString(1));
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listaCiudades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCiudad.setAdapter(adapter);
    }

    private void guardarCliente() {
        String nombre = edtNombre.getText().toString().trim();
        String apellido = edtApellido.getText().toString().trim();
        String ci = edtCI.getText().toString().trim();
        String tel = edtTelefono.getText().toString().trim();
        int posCiudad = spnCiudad.getSelectedItemPosition();

        if(TextUtils.isEmpty(nombre)) { edtNombre.setError("Ingrese nombre"); return; }
        if(TextUtils.isEmpty(apellido)) { edtApellido.setError("Ingrese apellido"); return; }
        if(TextUtils.isEmpty(ci)) { edtCI.setError("Ingrese CI"); return; }
        if(TextUtils.isEmpty(tel)) { edtTelefono.setError("Ingrese teléfono"); return; }
        if(posCiudad == -1) { Toast.makeText(this,"Seleccione ciudad",Toast.LENGTH_SHORT).show(); return; }

        int ciuCod = listaCiuCod.get(posCiudad);

        AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(this);

        SQLiteDatabase db = conn.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("cli_nom", nombre);
        values.put("cli_ape", apellido);
        values.put("cli_ci", ci);
        values.put("cli_tel", tel);
        values.put("ciu_cod", ciuCod);

        if(clienteSeleccionado == null){
            long result = db.insert("cliente", null, values);
            if(result != -1) Toast.makeText(this,"Cliente guardado", Toast.LENGTH_SHORT).show();
        } else {
            int result = db.update("cliente", values, "cli_cod=?", new String[]{String.valueOf(clienteSeleccionado.cliCod)});
            if(result != -1) Toast.makeText(this,"Cliente actualizado", Toast.LENGTH_SHORT).show();
            clienteSeleccionado = null;
        }

        db.close();
        listarClientes();
        limpiarCampos();
    }

    private void listarClientes() {
        listaClientes.clear();
        AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(this);

        SQLiteDatabase db = conn.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT c.cli_cod, c.cli_nom, c.cli_ape, c.cli_ci, c.cli_tel, c.ciu_cod, ciu_desc " +
                "FROM cliente c INNER JOIN ciudad ci ON c.ciu_cod = ci.ciu_cod", null);

        if(cursor.moveToFirst()){
            do{
                listaClientes.add(new Cliente(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getString(6)
                ));
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();

        clienteAdapter = new ClienteAdapter(listaClientes);
        rvClientes.setAdapter(clienteAdapter);
    }

    private void limpiarCampos(){
        edtNombre.setText("");
        edtApellido.setText("");
        edtCI.setText("");
        edtTelefono.setText("");
        if(!listaCiudades.isEmpty()) spnCiudad.setSelection(0);
        clienteSeleccionado = null;
    }

    // ================= Modelo =================
    private static class Cliente {
        int cliCod;
        String nombre, apellido, ci, tel;
        int ciuCod;
        String ciudad;

        Cliente(int cliCod, String nombre, String apellido, String ci, String tel, int ciuCod, String ciudad){
            this.cliCod = cliCod;
            this.nombre = nombre;
            this.apellido = apellido;
            this.ci = ci;
            this.tel = tel;
            this.ciuCod = ciuCod;
            this.ciudad = ciudad;
        }
    }

    // ================= Adapter =================
    private class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>{
        private List<Cliente> lista;

        ClienteAdapter(List<Cliente> lista){
            this.lista = lista;
        }

        @NonNull
        @Override
        public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
            return new ClienteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
            Cliente c = lista.get(position);
            holder.tvNombre.setText(c.nombre + " " + c.apellido);
            holder.tvCI.setText("CI: " + c.ci);
            holder.tvTel.setText("Tel: " + c.tel);
            holder.tvCiudad.setText("Ciudad: " + c.ciudad);

            // Editar
            holder.btnEditar.setOnClickListener(v -> {
                edtNombre.setText(c.nombre);
                edtApellido.setText(c.apellido);
                edtCI.setText(c.ci);
                edtTelefono.setText(c.tel);

                int pos = listaCiuCod.indexOf(c.ciuCod);
                if(pos != -1) spnCiudad.setSelection(pos);

                clienteSeleccionado = c;
            });

            // Borrar
            holder.btnBorrar.setOnClickListener(v -> {
                AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(ClienteActivity.this);

                SQLiteDatabase db = conn.getWritableDatabase();
                db.delete("cliente","cli_cod=?", new String[]{String.valueOf(c.cliCod)});
                db.close();
                Toast.makeText(ClienteActivity.this,"Cliente eliminado", Toast.LENGTH_SHORT).show();
                listarClientes();
            });
        }

        @Override
        public int getItemCount() { return lista.size(); }

        class ClienteViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvCI, tvTel, tvCiudad;
            MaterialButton btnEditar, btnBorrar;

            ClienteViewHolder(@NonNull View itemView){
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tv_nombre_cliente);
                tvCI = itemView.findViewById(R.id.tv_ci_cliente);
                tvTel = itemView.findViewById(R.id.tv_telefono_cliente);
                tvCiudad = itemView.findViewById(R.id.tv_ciudad_cliente);
                btnEditar = itemView.findViewById(R.id.btn_editar_cliente);
                btnBorrar = itemView.findViewById(R.id.btn_borrar_cliente);
            }
        }
    }
}
