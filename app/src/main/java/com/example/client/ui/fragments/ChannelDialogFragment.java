package com.example.client.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.example.client.io.SocketService;
import com.example.client.R;

import static com.example.client.utility.Utility.socketService;

import com.google.android.material.textfield.TextInputLayout;

public class ChannelDialogFragment extends DialogFragment {

    // Definiciones

    // Componentes de la vista
    private EditText etNameChannel;
    private TextInputLayout tilNameChannel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_channel, container, false);  // Creamos la vista del dialogo con dialog_channel.xml

        // Inicializacion de componentes de la vista
        etNameChannel = view.findViewById(R.id.etNameChannel);
        tilNameChannel = view.findViewById(R.id.tilNameChannel);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        toolbar.setTitle("Crear canal"); // Titulamos el toolbar del dialogo
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar); // Indicamos que nuestro toolbar es personalizado

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar(); // Boton de cancelar el dialogo
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }

        setHasOptionsMenu(true); //Indicamos que tendremos opciones de menu
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Se encarga de no asignar un titulo, así podremos colocar botones de guardar y cancelar en su lugar
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.save_menu, menu); //Indicamos cual es el xml del menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.save) { //Cuando use la opcion guardar validamos el formulario
            validateForm();
            return true;
        } else if (id == android.R.id.home) { //Cuando use la opcion cancelar cerramos el dialogo
            dismiss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Valida cada uno de los campos del formulario para ver si ha sido llenado correctamente,
     * de ser asi envia la solicitud al servidor para crear el canal, en caso contrario retorna
     */
    private void validateForm() {

        if (validateIsEmpty(etNameChannel, tilNameChannel)) { // Manejamos que el campo del nombre del nuevo canal no este vacio
            return;
        }

        System.out.println(SocketService.isRunning());
        if (SocketService.isRunning()) {
            socketService.sendMessage("CREATE " + etNameChannel.getText().toString().trim()); // Creamos una nueva solicitud con el fin de crear el nuevo canal
            dismiss(); // Descartamos el dialogo
        }
        etNameChannel.getText().clear(); // Limpiamos el campo del dialogo
    }

    /**
     * Valida el campo para el nuevo nombre del canal
     *
     * @return true si esta vacio, falso si no
     */
    private boolean validateIsEmpty(EditText editText, TextInputLayout textInputLayout) {

        if (editText.getText().toString().trim().isEmpty()) { //Validamos si el campo etNameChannel esta vacio para retornar falso
            textInputLayout.setError(getString(R.string.error_isEmpty)); //Colocamos error al input
            return true;
        } else {
            textInputLayout.setErrorEnabled(false); //Si no esta vacio quitamos los errores del input
        }
        return false;
    }

}

