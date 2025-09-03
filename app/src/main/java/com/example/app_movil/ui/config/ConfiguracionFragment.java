package com.example.app_movil.ui.config;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.CheckBox;
import android.widget.Toast;
import com.example.app_movil.R;

public class ConfiguracionFragment extends Fragment {

    public ConfiguracionFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_configuracion, container, false);

        Switch switchNotificaciones = root.findViewById(R.id.switch_notificaciones);
        CheckBox checkBoxOscuro = root.findViewById(R.id.checkbox_oscuro);

        // Listener para Switch (solo prueba)
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(),
                    "Notificaciones " + (isChecked ? "Activadas" : "Desactivadas"),
                    Toast.LENGTH_SHORT).show();
        });

        // Listener para CheckBox Modo Oscuro
        checkBoxOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        return root;
    }
}
