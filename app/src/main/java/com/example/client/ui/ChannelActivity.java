package com.example.client.ui;

import static com.example.client.utility.Utility.FILE_DIRECTORY;
import static com.example.client.utility.Utility.IMAGE_DIRECTORY;
import static com.example.client.utility.Utility.getRealPathFromUri;
import static com.example.client.utility.Utility.pxToDp;
import static com.example.client.utility.Utility.socketService;
import static com.example.client.utility.Utility.verifyStoragePermissions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.client.io.SocketService;
import com.example.client.models.AppRepository;
import com.example.client.R;
import com.example.client.ui.adapters.MessageAdapter;
import com.example.client.ui.adapters.UserAdapter;
import com.example.client.utility.Utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;

/**
 * Actividad para un canal existente en el servidor
 */
public class ChannelActivity extends AppCompatActivity implements View.OnClickListener {

    // Definiciones

    // Variables
    private String base64Image;      // Valor en base64 del archivo
    private String nameChannel;      // Nombre del canal
    private boolean refreshIsActive; // Define si esta activa o no la funcion de refresco
    private boolean isRequestUsr;    // Bandera para llevar acabo las solicitudes

    private File file;                      // File del archivo tomado en la camara o elegido del almacenamiento
    private ReceiverMsg receiverMsg;        // Receptor de datos para los mensajes enviados por el SocketService
    private ReceiverUsr receiverUsr;        // Receptor de datos para los usuarios enviados por el SocketService
    private IntentFilter intentFilterMsg;   // Filtro de respuesta de los mensajes por el servicio mediante un Intent
    private IntentFilter intentFilterUsr;   // Filtro de respuesta de los usuarios por el servicio mediante un Intent
    private UserAdapter userAdapter;        // Adaptador de usuarios conectados al canal
    private MessageAdapter messageAdapter;  // Adaptador de mensajes

    private ActivityResultLauncher<Uri> openCamera;     // Actividad para abrir la camara
    private ActivityResultLauncher<String> openGallery; // Actividad para abrir la galeria

    // Componentes de la vista
    private TextView tvDoc;
    private EditText etMessage;
    private ImageView ivImage;
    private LinearLayout llRvMessages;
    private LinearLayout llImageView;
    private LinearLayout llDocView;

    // Parametros de LinearLayout
    private ViewGroup.LayoutParams paramsLlRvMessages; // Parametros RecyclerView
    private ViewGroup.LayoutParams paramsLlImageView;  // Parametros ImageView para imagenes
    private ViewGroup.LayoutParams paramsLlDocView;    // Parametros ImageView para documentos

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        // Inicializacion de componentes de la vista
        tvDoc = findViewById(R.id.tv_doc);
        etMessage = findViewById(R.id.et_message);
        ivImage = findViewById(R.id.iv_image);
        llRvMessages = findViewById(R.id.ll_rv_messages);
        llImageView = findViewById(R.id.ll_image_view);
        llDocView = findViewById(R.id.ll_doc_view);

        // Obtenemos los parametros de los LinearLayout
        paramsLlRvMessages = llRvMessages.getLayoutParams();
        paramsLlImageView = llImageView.getLayoutParams();
        paramsLlDocView = llDocView.getLayoutParams();

        // Definicion e inicializacion de componentes de la vista
        RecyclerView rvMessages = findViewById(R.id.rv_messages);
        RecyclerView rvUsers = findViewById(R.id.rv_users);
        Button btnSend = findViewById(R.id.btn_send);
        Button btnUploadImage = findViewById(R.id.btn_upload_image);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        androidx.appcompat.widget.Toolbar tbChannel = findViewById(R.id.tb_tittle);

        // Obtenemos el nombre del canal proporcionado por HomeActivity y lo asignamos al titulo del toolbar
        nameChannel = getIntent().getStringExtra("name");
        tbChannel.setTitle("# " + nameChannel);

        // Desplegar DraweLayout
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, tbChannel, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Inicializaciones
        refreshIsActive = true;                                                                                    // Activamos la funcion de refresco
        isRequestUsr = true;                                                                                       // Activamos la solicitud de usuarios
        receiverMsg = new ReceiverMsg();                                                                           // Receptor de datos para los mensajes enviados por el SocketService
        receiverUsr = new ReceiverUsr();                                                                           // Receptor de datos para los usuarios enviados por el SocketService
        intentFilterMsg = new IntentFilter("response_msg");                                                  // Filtro de respuesta de los mensajes enviado por el Servicio
        intentFilterUsr = new IntentFilter("response_usr");                                                  // Filtro de respuesta de los usuarios enviado por el Servicio
        userAdapter = new UserAdapter(AppRepository.getInstance().getChannel(nameChannel).getListUsers());         // UserAdapter con los usuarios para el canal
        messageAdapter = new MessageAdapter(AppRepository.getInstance().getChannel(nameChannel).getListMessage()); // MessageAdapter con mensajes para el canal
        LinearLayoutManager llManagerRvMessages = new LinearLayoutManager(this);                            // Administrador de Linearlayout para el RecyclerView de mensajes
        LinearLayoutManager llManagerRvUsers = new LinearLayoutManager(this);                               // Administrador de Linearlayout para el RecyclerView de usuarios


        // El RecyclerView debe asociarse a un LayoutManager y un Adapter
        // Mensajes
        rvMessages.setLayoutManager(llManagerRvMessages); // LayoutManger se encarga de dar posicion a los items
        rvMessages.setAdapter(messageAdapter);  // MessagesAdapter se encarga de convertir los mensajes en cada uno de los items
        // Usuarios conectados
        rvUsers.setLayoutManager(llManagerRvUsers);
        rvUsers.setAdapter(userAdapter);

        // Asignacion de listener a los botones
        btnUploadImage.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        messageAdapter.setOnClickListener(view -> { // Listener para intentar abrir los archivos del RecyclerView

            refreshIsActive = false; // Desactivamos la funcion de refrescot
            String file = AppRepository.getInstance().getChannel(nameChannel).getListMessage().get(rvMessages.getChildAdapterPosition(view)).getBase64file(); // Obtenemos el archivo en base64
            String fileName = AppRepository.getInstance().getChannel(nameChannel).getListMessage().get(rvMessages.getChildAdapterPosition(view)).getContent(); // Obtenemos el nombre del archivo
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FILE_DIRECTORY + File.separator + fileName; // Creamo la ruta para el archivo con el nombre

            byte[] decodedString = Base64.getDecoder().decode(file); //Descodificamos el archivo en base64 a bytes

            try { // Escribimos en el archivo sus bytes
                OutputStream out = new FileOutputStream(filePath);
                out.write(decodedString);
                out.close();
                Utility.FileOpen.openFile(view.getContext(), new File(filePath)); // Intentamos abrir el archivo
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Inicializacion de actividades
        // Manejo de camara, abre la camara y espera un boolean como resultado al tomar y subir la foto
        openCamera = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) { // Manejamos que se haya subido la foto
                setImageBitmap();
            } else {
                Toast.makeText(ChannelActivity.this, "No se cargo la imagen", Toast.LENGTH_SHORT).show();
            }
        });

        // Manejo de galeria, abre la galeria y espera un Uri como resultado
        openGallery = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) { // Manejamos que se haya elegido un archivo

                file = new File(getRealPathFromUri(ChannelActivity.this, result)); // Creamos el archivo con su Uri

                if (new Utility.ImageFileFilter().accept(file)) { // Manejamos si es una imagen o otro tipo de archivo
                    setImageBitmap();
                } else {
                    setFile();
                }
            } else {
                Toast.makeText(ChannelActivity.this, "No se cargo el archivo", Toast.LENGTH_SHORT).show();
            }
        });

        requestData(); // Solicitamos los mensajes y los usuarios para nuestro canal
    }


    /**
     * Si la funcion de refresco esta activada solicita escalodamente esperando por respuesta los usuarios y los canales del canal actual
     * al servicio a través de SocketService, luego llama al metodo de refresco
     */
    public void requestData() {

        if (refreshIsActive) { // Manejamos que este activa la funcion de refresco
            if (SocketService.isRunning()) { // Manejamos que el servicio este en ejecucion
                if (isRequestUsr) { // Primero hacemos la solicitud de usuarios para no mezclar la respuesta del servidor en los mensajes
                    socketService.sendMessage("LIST_USR " + nameChannel); // Solicitamos los usuarios para el canal al servicio
                    LocalBroadcastManager.getInstance(this).registerReceiver(receiverUsr, intentFilterUsr); // Recibimos la respuesta del servicio
                } else {
                    socketService.sendMessage("LIST_MSG " + nameChannel); // Solicitamos los mensajes para el canal al servicio
                    LocalBroadcastManager.getInstance(this).registerReceiver(receiverMsg, intentFilterMsg); // Recibimos la respuesta del servicio
                }
            }
        }

        refresh(); //Llamamos al metodo de refresco
    }

    /**
     * Solicita los mensajes del servidor invocando requestMsgChannel cada medio segundo
     */
    private void refresh() {

        final Handler handler = new Handler();
        final Runnable runnable = this::requestData;
        handler.postDelayed(runnable, 1000 / 2);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {

        // En caso de ser un boton clickeado
        switch (view.getId()) {

            case R.id.btn_upload_image:                // Boton para subir imagen
                verifyStoragePermissions(this); // Verificamos los permisos de almacenamiento
                showOptions();                         // Mostramos las opciones
                break;

            case R.id.btn_send: // Boton para enviar mensaje
                sendMsg();      // Enviamos el mensaje
                break;
        }
    }

    /**
     * Muestra un dialogo con las opciones para subir una foto
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showOptions() {

        final CharSequence[] options = {"Camara", "Archivos", "Cancelar"}; // Opciones para el dialogo
        final AlertDialog.Builder builder = new AlertDialog.Builder(this); //Construimos el dialogo

        builder.setTitle("Elige una opción"); // Titulamos el dialogo

        // Configuramos el dialogo con las opciones y sus acciones
        builder.setItems(options, (dialogInterface, i) -> {

            if (options[i].equals("Camara")) {
                openCamera(); // Llamamos el metodo que activa la camara

            }
            if (options[i].equals("Archivos")) {

                openGallery.launch("*/"); // Lanzamos la actividad para abrir la galeria
            } else {

                dialogInterface.dismiss(); // Descartamos el dialogo en cualquier otro caso
            }
        });

        builder.show(); // Mostramos el dialogo
    }

    /**
     * Crea el archivo donde se guardara la foto tomada por la camara y lanza la actividad de la camara para tomar dicha foto
     */
    private void openCamera() {

        Long consecutive = System.currentTimeMillis() / 1000; // Creamos consecutivo con el tiempo actual en segundos en que se inicia el proceso
        String name = consecutive + ".jpeg"; // Le asignamos el consecutivo como nombre, extension .jpeg

        String filePath = Environment.getExternalStorageDirectory() + File.separator + IMAGE_DIRECTORY + File.separator + name; //Indicamos la ruta de almacenamiento
        file = new File(filePath); //Construimos el archivo con base al path

        openCamera.launch(Uri.fromFile(file)); // Lanzamos la activida de la camara con el uri del archivo donse de guardara la foto
    }

    /**
     * Envia mensaje al servidor segun el estado de los componentes
     */
    private void sendMsg() {

        if (base64Image != null && !base64Image.equals("null")) { // Verificamos si existe el valor de la imagen en base64 a enviar

            if (etMessage.length() > 0) {  // Verificamos si la imagen contiene un mensaje como descripcion

                //Ejecutamos la nueva solicitud con el mensaje y la imagen
                socketService.sendMessage("MSG " + nameChannel + ";;" + etMessage.getText().toString() + ";;" + base64Image);
                etMessage.getText().clear(); // Limpiamos el campo para el mensaje

            } else {

                //Ejecutamos la nueva solicitud con el mensaje null y la imagen
                socketService.sendMessage("MSG " + nameChannel + ";;null;;" + base64Image);
            }

            // Finalizar imagen de previsualizacion ya enviada al servidor
            ivImage.setImageBitmap(null); // Quitamos el bitmap de la imagen
            etMessage.getText().clear();
            etMessage.setEnabled(true);
            base64Image = "null";
            paramsLlImageView.height = 0; // Establecemos en 0 la altura del Layout de la imagen
            paramsLlDocView.height = 0;   // Establecemos en 0 la altura del Layour del documento
            llImageView.setLayoutParams(paramsLlImageView); //Establecemos los nuevos parametros al Layout de la imagen para esconderlo
            llDocView.setLayoutParams(paramsLlDocView); //Establecemos los nuevos parametros al Layout del documento para esconderlo

            // Restablecer la altura de el RecyclerView sin la imagen de previsualizacion
            paramsLlRvMessages.height = pxToDp(701, this); // Restrablecemos la altura de el RecyclerView
            llRvMessages.setLayoutParams(paramsLlRvMessages); // Establecemos los nuevos parametros al Layout de el RecyclerView

        } else if (etMessage.length() > 0) { // Verificamos si tiene longitud el mensaje

            socketService.sendMessage("MSG " + nameChannel + ";;" + etMessage.getText().toString() + ";;null"); //Ejecutamos la nueva solicitud con el mensaje y la imagen en null
            etMessage.getText().clear(); // Limpiamos el campo para el mensaje

        } else {
            Toast.makeText(this, "Escrible un mensaje primero", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Crea la previsualizacion del documento antes de enviar y lo codifica a base64
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setFile() {

        if (file.exists()) { // Verificamos que el archivo se haya creado
            FileInputStream fileStream;

            try {

                paramsLlRvMessages.height = pxToDp(540, this); // Hacemos menos alta la altura del RecyclerView
                llRvMessages.setLayoutParams(paramsLlRvMessages);

                paramsLlDocView.height = pxToDp(163, this); // Establecemos la altura del Layout para la previsualizacion del documento
                llDocView.setLayoutParams(paramsLlDocView);

                tvDoc.setText(file.getName());
                etMessage.setText(file.getName());
                etMessage.setEnabled(false);

                fileStream = new FileInputStream(file);
                byte[] bytesFile = new byte[(int) file.length()]; // Bytes del documento

                fileStream.read(bytesFile);
                fileStream.close();
                base64Image = Base64.getEncoder().encodeToString(bytesFile); // Codificamos a base 64 los bytes del documento

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Crea un bitmap para la imagen para mostrar como vista previa antes de enviar, la comprime y la codifica a base64
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setImageBitmap() {

        if (file.exists()) { // Verificamos que el archivo se haya creado
            InputStream imageStream;

            try {
                imageStream = getContentResolver().openInputStream(Uri.fromFile(file)); // Leemos los datos de la imagen
                Bitmap bitmapImage = BitmapFactory.decodeStream(imageStream); // Creamos el bitmap con los datos de la de imagen

                ByteArrayOutputStream ImageCompress = new ByteArrayOutputStream(); // Outputstream para comprimir el bitmap
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 30, ImageCompress); // Comprimimos la imagen

                paramsLlRvMessages.height = pxToDp(540, this); // Hacemos menos alto la altura de el RecyclerView
                llRvMessages.setLayoutParams(paramsLlRvMessages);

                paramsLlImageView.height = pxToDp(163, this); // Establecemos la altura del Layout para la previsualizacion de la imagen
                llImageView.setLayoutParams(paramsLlImageView);

                ivImage.setImageBitmap(bitmapImage); // La asignamos al componente de previsualizacion

                byte[] bytesImageBitmap = ImageCompress.toByteArray(); // Bytes de la imagen comprimida
                base64Image = Base64.getEncoder().encodeToString(bytesImageBitmap); // Codificamos a base 64 los bytes de la imagen comprimida

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Receptor de datos de usuarios enviados por el SocketService
     */
    public class ReceiverUsr extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String[] responseUsr = intent.getExtras().getStringArray("data_usr");
            if (responseUsr != null) {
                if (!Arrays.toString(responseUsr).isEmpty() && refreshIsActive) { // Nos aseguramos que el estado del metodo de refresco pare el proceso y que la respuesta no este vacia
                    AppRepository.getInstance().setUsersChannel(nameChannel, responseUsr); // Asignamos los usuarios al canal en el repositorio
                    userAdapter.setUsers(AppRepository.getInstance().getChannel(nameChannel).getListUsers()); // Asignamos los usuarios al adaptador de ellos
                }
            }
            isRequestUsr = false; // Desactivamos la solicitud de usuarios para que siga la de mensajes
        }
    }

    /**
     * Receptor de datos de mensajes enviados por el SocketService
     */
    public class ReceiverMsg extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String[] responseMsg = intent.getExtras().getStringArray("data_msg");

            if (responseMsg != null) {
                if (!Arrays.toString(responseMsg).isEmpty() && refreshIsActive) { // Nos aseguramos que el estado del metodo de refresco pare el proceso y que la respuesta no este vacia
                    AppRepository.getInstance().setMessagesChannel(nameChannel, responseMsg); // Asignamos los mensajes al canal en el repositorio
                    messageAdapter.setMessages(AppRepository.getInstance().getChannel(nameChannel).getListMessage()); // Asignamos los mensajes al adaptador de ellos
                }
            }
            isRequestUsr = true; // Activamos la solicitud de usuarios
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshIsActive = true; // Activamos el estado de la funcion de refresco cuando se retome la actividad
    }

    @Override
    protected void onStop() {
        super.onStop();
        refreshIsActive = false; // Paramos la funcion de refresco cuando se pause la actividad
        socketService.sendMessage("LEAVE " + nameChannel); // Ejecutamos la nueva solicitud de desconectar al usuario del canal
    }
}