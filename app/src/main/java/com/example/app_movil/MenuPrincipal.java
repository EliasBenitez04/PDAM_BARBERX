package com.example.app_movil;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.LinkedHashMap;
import java.util.Map;

public class MenuPrincipal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private AdminSQLiteOpenHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        helper = new AdminSQLiteOpenHelper(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Mostrar resumen en GridLayout
        mostrarResumenDB();
    }

    // 🔹 Se agrega onResume para actualizar datos dinámicamente
    @Override
    protected void onResume() {
        super.onResume();
        mostrarResumenDB(); // Recarga los datos cuando la actividad vuelve a primer plano
    }

    private void mostrarResumenDB() {
        SQLiteDatabase db = helper.getReadableDatabase();
        GridLayout grid = findViewById(R.id.grid_resumen);
        grid.removeAllViews();

        // Map de alias para que se vea bonito y ordenado
        Map<String, String> aliasTablas = new LinkedHashMap<>();
        aliasTablas.put("ciudad", "Ciudades");
        aliasTablas.put("sucursal", "Sucursales");
        aliasTablas.put("cargo", "Cargos");
        aliasTablas.put("cliente", "Clientes");
        aliasTablas.put("funcionario", "Funcionarios");
        aliasTablas.put("usuario", "Usuarios");
        aliasTablas.put("tipo_servicios", "Tipo Servicios");
        aliasTablas.put("servicios", "Servicios");
        aliasTablas.put("agendamiento", "Agendamientos");
        aliasTablas.put("fin_servicio", "Fin Servicios");

        for (Map.Entry<String, String> entry : aliasTablas.entrySet()) {
            String tabla = entry.getKey();
            String alias = entry.getValue();

            Cursor cursor = db.rawQuery("SELECT COUNT(*) AS total FROM " + tabla, null);
            int total = 0;
            if (cursor.moveToFirst()) {
                total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
            }
            cursor.close();

            // Crear tarjeta (LinearLayout)
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.parseColor("#FFEBEE")); // color de fondo suave
            card.setPadding(24, 24, 24, 24);
            card.setGravity(Gravity.CENTER);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(16, 16, 16, 16);
            params.width = 0;
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            card.setLayoutParams(params);

            // Alias (nombre)
            TextView tvAlias = new TextView(this);
            tvAlias.setText(alias);
            tvAlias.setTextColor(Color.parseColor("#333333"));
            tvAlias.setTextSize(14f);
            tvAlias.setGravity(Gravity.CENTER);

            // Total
            TextView tvTotal = new TextView(this);
            tvTotal.setText(String.valueOf(total));
            tvTotal.setTextColor(Color.parseColor("#000000"));
            tvTotal.setTextSize(20f);
            tvTotal.setGravity(Gravity.CENTER);

            card.addView(tvAlias);
            card.addView(tvTotal);

            grid.addView(card);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_view_agendamiento) {
            startActivity(new Intent(this, AgendamientoActivity.class));
        } else if (id == R.id.nav_servicios) {
            startActivity(new Intent(this, ServiciosActivity.class));
        } else if (id == R.id.nav_fin_servicio) {
            startActivity(new Intent(this, FinServicioActivity.class));
        } else if (id == R.id.nav_cargos) {
            startActivity(new Intent(this, CargoActivity.class));
        } else if (id == R.id.nav_ciudades) {
            startActivity(new Intent(this, CiudadActivity.class));
        } else if (id == R.id.nav_sucursales) {
            startActivity(new Intent(this, SucursalActivity.class));
        } else if (id == R.id.nav_clientes) {
            startActivity(new Intent(this, ClienteActivity.class));
        } else if (id == R.id.nav_usuarios) {
            startActivity(new Intent(this, UsuarioActivity.class));
        } else if (id == R.id.nav_tipo_servicio) {
            startActivity(new Intent(this, TipoServicioActivity.class));
        } else if (id == R.id.nav_funcionarios) {
            startActivity(new Intent(this, FuncionarioActivity.class));
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
