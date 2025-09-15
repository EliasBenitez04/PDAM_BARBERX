package com.example.app_movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MenuPrincipal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

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
        } else if (id == R.id.nav_formas_pago) {
            startActivity(new Intent(this, FormaPagosActivity.class));
        } else if (id == R.id.nav_pagos) {
            startActivity(new Intent(this, PagosActivity.class));
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
