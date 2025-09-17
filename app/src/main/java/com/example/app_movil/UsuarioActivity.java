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

public class UsuarioActivity extends AppCompatActivity {

    private Spinner spnFuncionario;
    private EditText edtUsuario, edtPassword;
    private MaterialButton btnGuardar, btnLimpiar, btnSalir;
    private RecyclerView rvUsuarios;

    private ArrayList<String> listaFuncionarios;
    private ArrayList<Integer> listaFunCod;
    private List<Usuario> listaUsuarios;
    private UsuarioAdapter usuarioAdapter;

    private int usuarioEditandoId = -1; // <--- bandera para saber si edita

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        // Referencias UI
        spnFuncionario = findViewById(R.id.spn_funcionario);
        edtUsuario = findViewById(R.id.edt_usuario);
        edtPassword = findViewById(R.id.edt_password);
        btnGuardar = findViewById(R.id.btn_guardar_usuario);
        btnLimpiar = findViewById(R.id.btn_limpiar_usuario);
        btnSalir = findViewById(R.id.btn_salir_usuario);
        rvUsuarios = findViewById(R.id.rv_usuarios);

        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        listaUsuarios = new ArrayList<>();

        cargarFuncionarios();
        listarUsuarios();

        btnGuardar.setOnClickListener(v -> {
            if (usuarioEditandoId == -1) {
                guardarUsuario();
            } else {
                actualizarUsuario(); // <---
            }
        });

        btnLimpiar.setOnClickListener(v -> limpiarCampos());
        btnSalir.setOnClickListener(v -> finish());

        MaterialToolbar toolbar = findViewById(R.id.toolbar_usuario);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // ================= CRUD =================

    private void cargarFuncionarios() {
        listaFuncionarios = new ArrayList<>();
        listaFunCod = new ArrayList<>();

        AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = conn.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT fun_cod, fun_nom || ' ' || fun_ape AS nombre FROM funcionario",
                null
        );
        if (cursor.moveToFirst()) {
            do {
                listaFunCod.add(cursor.getInt(0));
                listaFuncionarios.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                listaFuncionarios
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFuncionario.setAdapter(adapter);
    }

    private void guardarUsuario() {
        String nombreUsuario = edtUsuario.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        int posFuncionario = spnFuncionario.getSelectedItemPosition();

        if (TextUtils.isEmpty(nombreUsuario)) {
            edtUsuario.setError("Ingrese un nombre de usuario");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Ingrese una contraseña");
            return;
        }
        if (posFuncionario == -1) {
            Toast.makeText(this, "Seleccione un funcionario", Toast.LENGTH_SHORT).show();
            return;
        }

        int funCod = listaFunCod.get(posFuncionario);

        AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(this);

        SQLiteDatabase db = conn.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("fun_cod", funCod);
        values.put("usu_name", nombreUsuario);
        values.put("usu_pass", password);

        long result = db.insert("usuario", null, values);
        db.close();

        if (result != -1) {
            Toast.makeText(this, "Usuario guardado", Toast.LENGTH_SHORT).show();
            listarUsuarios();
            limpiarCampos();
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarUsuario() { // <---
        String nombreUsuario = edtUsuario.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        int posFuncionario = spnFuncionario.getSelectedItemPosition();

        if (TextUtils.isEmpty(nombreUsuario)) {
            edtUsuario.setError("Ingrese un nombre de usuario");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Ingrese una contraseña");
            return;
        }

        int funCod = listaFunCod.get(posFuncionario);

        AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(this);

        SQLiteDatabase db = conn.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("fun_cod", funCod);
        values.put("usu_name", nombreUsuario);
        values.put("usu_pass", password);

        int result = db.update("usuario", values, "usu_cod = ?", new String[]{String.valueOf(usuarioEditandoId)});
        db.close();

        if (result > 0) {
            Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show();
            listarUsuarios();
            limpiarCampos();
            usuarioEditandoId = -1;
            btnGuardar.setText("Guardar");
        } else {
            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarUsuario(int usuCod) { // <---
        AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(this);

        SQLiteDatabase db = conn.getWritableDatabase();
        int result = db.delete("usuario", "usu_cod = ?", new String[]{String.valueOf(usuCod)});
        db.close();

        if (result > 0) {
            Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
            listarUsuarios();
        } else {
            Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    private void listarUsuarios() {
        listaUsuarios.clear();
        AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(this);

        SQLiteDatabase db = conn.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT u.usu_cod, u.fun_cod, u.usu_name, u.usu_pass, f.fun_nom " +
                "FROM usuario u INNER JOIN funcionario f ON u.fun_cod = f.fun_cod", null);

        if (cursor.moveToFirst()) {
            do {
                int usuCod = cursor.getInt(0);
                int funCod = cursor.getInt(1);
                String usuName = cursor.getString(2);
                String usuPass = cursor.getString(3);
                String funNom = cursor.getString(4);

                listaUsuarios.add(new Usuario(usuCod, funCod, usuName, usuPass, funNom));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        usuarioAdapter = new UsuarioAdapter(listaUsuarios);
        rvUsuarios.setAdapter(usuarioAdapter);
    }

    private void limpiarCampos() {
        edtUsuario.setText("");
        edtPassword.setText("");
        if (!listaFuncionarios.isEmpty()) {
            spnFuncionario.setSelection(0);
        }
    }

    // ================= Modelo interno =================
    private static class Usuario {
        private int usuCod;
        private int funCod;
        private String usuName;
        private String usuPass;
        private String funcionarioNombre;

        public Usuario(int usuCod, int funCod, String usuName, String usuPass, String funcionarioNombre) {
            this.usuCod = usuCod;
            this.funCod = funCod;
            this.usuName = usuName;
            this.usuPass = usuPass;
            this.funcionarioNombre = funcionarioNombre;
        }
    }

    // ================= Adapter interno =================
    private class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {
        private List<Usuario> listaUsuarios;

        public UsuarioAdapter(List<Usuario> listaUsuarios) {
            this.listaUsuarios = listaUsuarios;
        }

        @NonNull
        @Override
        public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_usuario, parent, false);
            return new UsuarioViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
            Usuario usuario = listaUsuarios.get(position);
            holder.tvUsuarioName.setText(usuario.usuName);
            holder.tvFuncionario.setText("Funcionario: " + usuario.funcionarioNombre);

            // Botón Editar <---
            holder.btnEditar.setOnClickListener(v -> {
                edtUsuario.setText(usuario.usuName);
                edtPassword.setText(usuario.usuPass);
                int pos = listaFunCod.indexOf(usuario.funCod);
                if (pos != -1) spnFuncionario.setSelection(pos);
                usuarioEditandoId = usuario.usuCod;
                btnGuardar.setText("Guardar");
            });

            // Botón Borrar <---
            holder.btnBorrar.setOnClickListener(v -> eliminarUsuario(usuario.usuCod));
        }

        @Override
        public int getItemCount() {
            return listaUsuarios.size();
        }

        class UsuarioViewHolder extends RecyclerView.ViewHolder {
            TextView tvUsuarioName, tvFuncionario;
            MaterialButton btnEditar, btnBorrar;

            public UsuarioViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUsuarioName = itemView.findViewById(R.id.tv_usuario_name);
                tvFuncionario = itemView.findViewById(R.id.tv_funcionario);
                btnEditar = itemView.findViewById(R.id.btn_editar_usuario);
                btnBorrar = itemView.findViewById(R.id.btn_borrar_usuario);
            }
        }
    }
}
