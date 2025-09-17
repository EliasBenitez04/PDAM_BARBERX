package com.example.app_movil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "app_movil.bd_pam3";
    private static final int DATABASE_VERSION = 2; // Aumenta la versión

    public AdminSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // --- Crear tablas ---
        db.execSQL("CREATE TABLE ciudad (ciu_cod INTEGER PRIMARY KEY AUTOINCREMENT, ciu_desc TEXT NOT NULL)");
        db.execSQL("CREATE TABLE sucursal (suc_cod INTEGER PRIMARY KEY AUTOINCREMENT, suc_desc TEXT NOT NULL)");
        db.execSQL("CREATE TABLE cargo (car_cod INTEGER PRIMARY KEY AUTOINCREMENT, car_desc TEXT NOT NULL)");
        db.execSQL("CREATE TABLE cliente (cli_cod INTEGER PRIMARY KEY AUTOINCREMENT, cli_nom TEXT NOT NULL, cli_ape TEXT NOT NULL, cli_ci TEXT NOT NULL, cli_tel TEXT NOT NULL, ciu_cod INTEGER NOT NULL, FOREIGN KEY (ciu_cod) REFERENCES ciudad(ciu_cod))");
        db.execSQL("CREATE TABLE funcionario (fun_cod INTEGER PRIMARY KEY AUTOINCREMENT, suc_cod INTEGER NOT NULL, car_cod INTEGER NOT NULL, fun_nom TEXT NOT NULL, fun_ape TEXT NOT NULL, fun_ci TEXT NOT NULL, fun_tel TEXT NOT NULL, fun_correo TEXT NOT NULL, fun_estado TEXT NOT NULL, fecha_contratacion DATE NOT NULL, FOREIGN KEY (suc_cod) REFERENCES sucursal(suc_cod), FOREIGN KEY (car_cod) REFERENCES cargo(car_cod))");
        db.execSQL("CREATE TABLE usuario (usu_cod INTEGER PRIMARY KEY AUTOINCREMENT, fun_cod INTEGER NOT NULL, usu_name TEXT NOT NULL, usu_pass TEXT NOT NULL, FOREIGN KEY (fun_cod) REFERENCES funcionario(fun_cod))");
        db.execSQL("CREATE TABLE tipo_servicios (tip_serv_cod INTEGER PRIMARY KEY AUTOINCREMENT, tip_serv_desc TEXT NOT NULL)");
        db.execSQL("CREATE TABLE servicios (serv_cod INTEGER PRIMARY KEY AUTOINCREMENT, tip_serv_cod INTEGER NOT NULL, serv_desc TEXT NOT NULL, serv_prec NUMERIC NOT NULL, FOREIGN KEY (tip_serv_cod) REFERENCES tipo_servicios(tip_serv_cod))");
        db.execSQL("CREATE TABLE forma_pagos (form_pag_cod INTEGER PRIMARY KEY AUTOINCREMENT, form_pag_desc TEXT NOT NULL)");
        db.execSQL("CREATE TABLE agendamiento (" +
                "agen_cod INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cli_cod INTEGER NOT NULL," +
                "fun_cod INTEGER NOT NULL," +
                "agen_date_reserva TIMESTAMP NOT NULL," +
                "agen_date_serv TIMESTAMP NOT NULL," +
                "agen_estado TEXT NOT NULL DEFAULT 'PENDIENTE'," +
                "FOREIGN KEY (cli_cod) REFERENCES cliente(cli_cod)," +
                "FOREIGN KEY (fun_cod) REFERENCES funcionario(fun_cod)" +
                ")");
        db.execSQL("CREATE TABLE det_agendamiento (agen_cod INTEGER NOT NULL, serv_cod INTEGER NOT NULL, det_agen_prec NUMERIC NOT NULL, det_agen_estado TEXT NOT NULL, det_agen_obs TEXT, PRIMARY KEY (agen_cod, serv_cod), FOREIGN KEY (agen_cod) REFERENCES agendamiento(agen_cod), FOREIGN KEY (serv_cod) REFERENCES servicios(serv_cod))");
        db.execSQL("CREATE TABLE fin_servicio (fin_serv_cod INTEGER PRIMARY KEY AUTOINCREMENT, agen_cod INTEGER NOT NULL, usu_cod INTEGER NOT NULL, fin_serv_date TIMESTAMP NOT NULL, fin_serv_estado TEXT NOT NULL, fin_serv_obs TEXT, FOREIGN KEY (agen_cod) REFERENCES agendamiento(agen_cod), FOREIGN KEY (usu_cod) REFERENCES usuario(usu_cod))");
        db.execSQL("CREATE TABLE pagos (pag_cod INTEGER PRIMARY KEY AUTOINCREMENT, fin_serv_cod INTEGER NOT NULL, form_pag_cod INTEGER NOT NULL, pag_fecha TIMESTAMP NOT NULL, FOREIGN KEY (fin_serv_cod) REFERENCES fin_servicio(fin_serv_cod), FOREIGN KEY (form_pag_cod) REFERENCES forma_pagos(form_pag_cod))");

        // --- Insertar datos iniciales ---
        ContentValues cv = new ContentValues();

        // Ciudad de prueba
        cv.put("ciu_desc", "Asunción");
        long idCiudad = db.insert("ciudad", null, cv);

        // Sucursal de prueba
        cv.clear();
        cv.put("suc_desc", "Sucursal Central");
        long idSucursal = db.insert("sucursal", null, cv);

        // Cargo de prueba
        cv.clear();
        cv.put("car_desc", "Administrador");
        long idCargo = db.insert("cargo", null, cv);

        // Funcionario de prueba
        cv.clear();
        cv.put("suc_cod", idSucursal);
        cv.put("car_cod", idCargo);
        cv.put("fun_nom", "Admin");
        cv.put("fun_ape", "Principal");
        cv.put("fun_ci", "12345678");
        cv.put("fun_tel", "0981123456");
        cv.put("fun_correo", "admin@correo.com");
        cv.put("fun_estado", "Activo");
        cv.put("fecha_contratacion", "2025-09-15");
        long idFuncionario = db.insert("funcionario", null, cv);

        // Usuario de prueba
        cv.clear();
        cv.put("fun_cod", idFuncionario);
        cv.put("usu_name", "admin");
        cv.put("usu_pass", "1234");
        db.insert("usuario", null, cv);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // --- Agregar columna nueva sin borrar datos existentes ---
        if (oldVersion < 2) {
            // Verificamos si la columna ya existe
            Cursor cursor = db.rawQuery("PRAGMA table_info(agendamiento)", null);
            boolean columnaExiste = false;
            while (cursor.moveToNext()) {
                String nombreColumna = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (nombreColumna.equals("agen_estado")) {
                    columnaExiste = true;
                    break;
                }
            }
            cursor.close();

            if (!columnaExiste) {
                db.execSQL("ALTER TABLE agendamiento ADD COLUMN agen_estado TEXT NOT NULL DEFAULT 'PENDIENTE'");
            }
        }
    }
}
