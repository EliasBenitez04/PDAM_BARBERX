/*package com.example.app_movil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    public AdminSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Activar las claves foráneas en SQLite
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla ciudad
        db.execSQL("CREATE TABLE ciudad (" +
                "ciu_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ciu_desc TEXT NOT NULL)");

        // Tabla sucursal
        db.execSQL("CREATE TABLE sucursal (" +
                "suc_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "suc_desc TEXT NOT NULL)");

        // Tabla cargo
        db.execSQL("CREATE TABLE cargo (" +
                "car_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "car_desc TEXT NOT NULL)");

        // Tabla cliente
        db.execSQL("CREATE TABLE cliente (" +
                "cli_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "cli_nom TEXT NOT NULL, " +
                "cli_ape TEXT NOT NULL, " +
                "cli_ci TEXT NOT NULL, " +
                "cli_tel TEXT NOT NULL, " +
                "ciu_cod INTEGER NOT NULL, " +
                "FOREIGN KEY (ciu_cod) REFERENCES ciudad(ciu_cod))");

        // Tabla funcionario
        db.execSQL("CREATE TABLE funcionario (" +
                "fun_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "suc_cod INTEGER NOT NULL, " +
                "car_cod INTEGER NOT NULL, " +
                "fun_nom TEXT NOT NULL, " +
                "fun_ape TEXT NOT NULL, " +
                "fun_ci TEXT NOT NULL, " +
                "fun_tel TEXT NOT NULL, " +
                "fun_correo TEXT NOT NULL, " +
                "fun_estado TEXT NOT NULL, " +
                "fecha_contratacion DATE NOT NULL, " +
                "FOREIGN KEY (suc_cod) REFERENCES sucursal(suc_cod), " +
                "FOREIGN KEY (car_cod) REFERENCES cargo(car_cod))");

        // Tabla usuario
        db.execSQL("CREATE TABLE usuario (" +
                "usu_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fun_cod INTEGER NOT NULL, " +
                "usu_name TEXT NOT NULL, " +
                "usu_pass TEXT NOT NULL, " +
                "FOREIGN KEY (fun_cod) REFERENCES funcionario(fun_cod))");

        // Tabla tipo_servicios
        db.execSQL("CREATE TABLE tipo_servicios (" +
                "tip_serv_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tip_serv_desc TEXT NOT NULL)");

        // Tabla servicios
        db.execSQL("CREATE TABLE servicios (" +
                "serv_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tip_serv_cod INTEGER NOT NULL, " +
                "serv_desc TEXT NOT NULL, " +
                "serv_prec NUMERIC NOT NULL, " +
                "FOREIGN KEY (tip_serv_cod) REFERENCES tipo_servicios(tip_serv_cod))");

        // Tabla forma_pagos
        db.execSQL("CREATE TABLE forma_pagos (" +
                "form_pag_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "form_pag_desc TEXT NOT NULL)");

        // Tabla agendamiento
        db.execSQL("CREATE TABLE agendamiento (" +
                "agen_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "cli_cod INTEGER NOT NULL, " +
                "fun_cod INTEGER NOT NULL, " +
                "usu_cod INTEGER NOT NULL, " +
                "agen_date_reserva TIMESTAMP NOT NULL, " +
                "agen_date_serv TIMESTAMP NOT NULL, " +
                "FOREIGN KEY (cli_cod) REFERENCES cliente(cli_cod), " +
                "FOREIGN KEY (fun_cod) REFERENCES funcionario(fun_cod), " +
                "FOREIGN KEY (usu_cod) REFERENCES usuario(usu_cod))");

        // Tabla det_agendamiento
        db.execSQL("CREATE TABLE det_agendamiento (" +
                "agen_cod INTEGER NOT NULL, " +
                "serv_cod INTEGER NOT NULL, " +
                "det_agen_prec NUMERIC NOT NULL, " +
                "det_agen_estado TEXT NOT NULL, " +
                "det_agen_obs TEXT, " +
                "PRIMARY KEY (agen_cod, serv_cod), " +
                "FOREIGN KEY (agen_cod) REFERENCES agendamiento(agen_cod), " +
                "FOREIGN KEY (serv_cod) REFERENCES servicios(serv_cod))");

        // Tabla fin_servicio
        db.execSQL("CREATE TABLE fin_servicio (" +
                "fin_serv_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "agen_cod INTEGER NOT NULL, " +
                "usu_cod INTEGER NOT NULL, " +
                "fin_serv_date TIMESTAMP NOT NULL, " +
                "fin_serv_estado TEXT NOT NULL, " +
                "fin_serv_obs TEXT, " +
                "FOREIGN KEY (agen_cod) REFERENCES agendamiento(agen_cod), " +
                "FOREIGN KEY (usu_cod) REFERENCES usuario(usu_cod))");

        // Tabla pagos
        db.execSQL("CREATE TABLE pagos (" +
                "pag_cod INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fin_serv_cod INTEGER NOT NULL, " +
                "form_pag_cod INTEGER NOT NULL, " +
                "pag_fecha TIMESTAMP NOT NULL, " +
                "FOREIGN KEY (fin_serv_cod) REFERENCES fin_servicio(fin_serv_cod), " +
                "FOREIGN KEY (form_pag_cod) REFERENCES forma_pagos(form_pag_cod))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS pagos");
        db.execSQL("DROP TABLE IF EXISTS fin_servicio");
        db.execSQL("DROP TABLE IF EXISTS det_agendamiento");
        db.execSQL("DROP TABLE IF EXISTS agendamiento");
        db.execSQL("DROP TABLE IF EXISTS forma_pagos");
        db.execSQL("DROP TABLE IF EXISTS servicios");
        db.execSQL("DROP TABLE IF EXISTS tipo_servicios");
        db.execSQL("DROP TABLE IF EXISTS usuario");
        db.execSQL("DROP TABLE IF EXISTS funcionario");
        db.execSQL("DROP TABLE IF EXISTS cliente");
        db.execSQL("DROP TABLE IF EXISTS cargo");
        db.execSQL("DROP TABLE IF EXISTS sucursal");
        db.execSQL("DROP TABLE IF EXISTS ciudad");
        onCreate(db);
    }
}*/
