package com.example.client.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.view.View;
import android.widget.Toast;

import com.example.client.io.SocketService;
import com.example.client.models.AppRepository;
import com.example.client.R;
import com.example.client.ui.adapters.ChannelAdapter;
import com.example.client.ui.fragments.ChannelDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static com.example.client.utility.Utility.socketService;

import java.util.Arrays;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    // Definiciones

    // Variables
    private boolean refreshIsActive;       // Define si esta activa o no la funcion de refresco
    private boolean serviceIsBound;        // Bandera que indica si el servicio esta vinculado
    private Intent serviceIntent;          // Intento donde se creara el servicio
    private ReceiverChn receiverChn;       // Receptor de datos para los canales enviados por el SocketService
    private IntentFilter intentFilterChn;  // Filtro de respuesta de los canales por el servicio, mediante un Intent
    private ChannelAdapter channelAdapter; // Adaptador de canales

    // Componentes de la vista
    private RecyclerView rvChannels;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Definiciones politicas de stictmode para la aplicacion,
        // fuerza a la fase de desarrollo a comportarse mejor dentro del dispositivo
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //Binding the activity to the service to perform client-server operations
        //start service on create
        serviceIntent = new Intent(this, SocketService.class);
        startService(serviceIntent);
        doBindService();

        // Inicializacion de componentes de la vista
        rvChannels = findViewById(R.id.rvListChannels);
        floatingActionButton = findViewById(R.id.floatingActionButton);

        // Inicializaciones
        refreshIsActive = true;                                                         // Activamos la funcion de refresco
        receiverChn = new ReceiverChn();                                                // Receptor de datos para los canales enviados por el SocketService
        intentFilterChn = new IntentFilter("response_chn");                       // Filtro de respuesta de los canales enviado por el Servicio
        channelAdapter = new ChannelAdapter(AppRepository.getInstance().getChannels()); // ChannelAdapter con los canales
        LinearLayoutManager llManager = new LinearLayoutManager(this);           // Administrador de Linearlayout para el RecyclerView

        // El RecyclerView debe asociarse a un LayoutManager y un Adapter
        rvChannels.setLayoutManager(llManager); // Layout Manger se encarga de dar posicion a los items
        rvChannels.setAdapter(channelAdapter); // Channel Adapter se encarga de convertir los datos en cada uno de los items

        // Asignacion de listener a los botones
        floatingActionButton.setOnClickListener(this);
        channelAdapter.setOnClickListener(view -> { // Listenes para los items de el RecyclerView

            refreshIsActive = false; // Desactivamos la funcion de refresco

            Intent intent = new Intent(view.getContext(), ChannelActivity.class); // Intentamos ir a la actividad el canal
            String nameChannel = AppRepository.getInstance().getChannels().get(rvChannels.getChildAdapterPosition(view)).getName(); // Nombre del canal donde iremos
            intent.putExtra("name", nameChannel); // Enviamos el nombre del canal a la actividad
            Toast.makeText(view.getContext(), "Canal: " + nameChannel, Toast.LENGTH_SHORT).show(); // Informamos el canal al usuario

            if (SocketService.isRunning()) {
                socketService.sendMessage("JOIN " + nameChannel); // Ejecutamos la nueva solicitud de conexion de usuario a un canal
            }
            view.getContext().startActivity(intent); // Iniciamos el intento
        });

        // Scroll Listener para nuestro RecyclerView con el fin de esconder o mostrar el boton flotante segun el estado del scroll
        rvChannels.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) { //Enseñamos el boton flotante cuando este estatico el scroll
                    floatingActionButton.show();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dx > 0 || dy > 0 && floatingActionButton.isShown()) { //Escondemos el boton flotante cuando se mueva el scroll y este este visible
                    floatingActionButton.hide();
                }

                if (dx > 0 || dy < 0 && floatingActionButton.isShown()) { //Escondemos el boton flotante cuando se mueva el scroll y este este visible
                    floatingActionButton.hide();
                }
            }
        });

        requestChannels(); // Solicitamos los canales
    }

    /**
     * Si la funcion de refresco esta activa solicita los canales al servicio a través de SocketService para listar
     * los canales existentes, luego llama al metodo de refresco
     */
    public void requestChannels() {

        if (refreshIsActive) { // Manejamos que este activa la funcion de refresco

            if (SocketService.isRunning()) { // Manejamos que el servicio este en ejecucion
                socketService.sendMessage("LIST_CHN"); // Solicitamos los canales al servicio
                LocalBroadcastManager.getInstance(this).registerReceiver(receiverChn, intentFilterChn);
            }
        }
        refresh(); //Llamamos al metodo de refresco
    }

    /**
     * Solicita los canales del servidor cada medio segundo
     */
    private void refresh() {

        final Handler handler = new Handler();
        final Runnable runnable = this::requestChannels;
        handler.postDelayed(runnable, 1000 / 2);
    }

    /**
     * Crea y muestra el fragmento de creacion de canales
     */
    private void showCreateChannelDialog() {

        FragmentManager fragmentManager = getSupportFragmentManager(); // Iniciamos el administrador de fragmento
        ChannelDialogFragment channelDialogFragment = new ChannelDialogFragment(); // Iniciamos el fragmento de creacion de canales

        FragmentTransaction transaction = fragmentManager.beginTransaction(); // Definimos la transaccion
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN); // Colocamos el tipo de transaccion

        //La transaccion, recibe donde se mostrara nuestro dialogo y nuestro dialogo, android.R.id.content: se refiere que es a toda pantalla
        transaction.add(android.R.id.content, channelDialogFragment).addToBackStack(null).commit();
    }

    /**
     * Manejo de conexion al servicio
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            socketService = ((SocketService.LocalBinder) service).getService(); // Obtiene el servicio y lo vincula
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            socketService = null; // Desvincula el servicio
        }
    };

    /**
     * Intenta vincular el servici
     */
    private void doBindService() {
        if (bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)) {
            serviceIsBound = true;
        }
    }


    /**
     * Desvincula el servicio
     */
    private void doUnbindService() {
        if (serviceIsBound) {
            unbindService(mConnection);
            serviceIsBound = false;
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.floatingActionButton:
                showCreateChannelDialog(); //Enseñamos el dialogo de creacion de canales cuando se presione el boton flotante
                break;
        }
    }

    /**
     * Receptor de datos de canales enviados por el SocketService
     */
    public class ReceiverChn extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String[] response = intent.getExtras().getStringArray("data_chn");

            if (response != null) {
                if (!Arrays.toString(response).isEmpty() && refreshIsActive && response.length == 3) { // Nos aseguramos que el estado del metodo de refresco pare el proceso y que la respuesta no este vacia
                    AppRepository.getInstance().saveChannels(response); // Asignamos los canales a el repositorio
                    channelAdapter.setChannels(AppRepository.getInstance().getChannels()); // Asignamos los canales al adaptador de ellos
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshIsActive = true; // Activamos el estado de la funcion de refresco cuando se retome la actividad
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService(); // Desvinculamos el servicio
    }
}



