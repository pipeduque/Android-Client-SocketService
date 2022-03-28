package com.example.client.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;

import com.example.client.io.SocketService;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Clase para variables y funciones estaticas como utilidad
 */
public class Utility {

    public static final String SERVER_IP = "192.x.x.x"; // Constante del host
    public static final int SERVER_PORT = 3000; // Constante del puerto

    public static SocketService socketService; // Variable del servicio para crear solicitudes en diferentes actividades

    // Constantes de permisos de almacenamiento
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Constantes para la carpeta de archivo
    public static final String IMAGE_DIRECTORY = "DCIM/Camera";
    public static final String FILE_DIRECTORY = "Download";

    /**
     * Convierte pixeles (px) a pixeles de densidad (dp), para que las
     * dimensiones dadas se muestren correctamente en pantalla.
     *
     * @param px pixel
     * @return pixeles de densidad
     */
    public static int pxToDp(int px, Context context) {

        float density = context.getResources().getDisplayMetrics().density; // densidad de la pantalla del dispositivo
        return Math.round((float) px * density);
    }

    /**
     * Verifica si la aplicacion tiene permisos para escribir en el almacenamiento del dispositivo
     * Si la aplicacion no tiene permisos entonces el usuario debe darle permisos a la aplicacion
     *
     * @param activity activity
     */
    public static void verifyStoragePermissions(Activity activity) {

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE); // Verificamos si existen los permisos de escritura

        if (permission != PackageManager.PERMISSION_GRANTED) { // Si no tenemos permisos

            // Solicitamos los permisos al usuario
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * Obtiene el path real de un Uri
     * @param context contexto de la actividad
     * @param uri Uri del archivo
     * @return Real path
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        String[] filePathColumn = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn,
                null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            return picturePath;
        }
        return "picturePath";
    }

    /**
     * Reconoce si un archivo es un tipo de imagen o no
     */
    public static class ImageFileFilter implements FileFilter {

        private final String[] okFileExtensions = new String[]{
                "jpg",
                "png",
                "gif",
                "jpeg"
        };

        /**
         * Confirma si el archivo es aceptable como tipo imagen
         * @param file archivo
         * @return Verdadero si es imagen, falso si no
         */
        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Abre un archivo
     */
    public static class FileOpen {

        /**
         * Abre un archivo segun su tipo
         * @param context Contexto
         * @param file Archivo
         * @throws IOException Excepcion al manipular el archivo
         */
        public static void openFile(Context context, File file) throws IOException {
            // Create URI
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);

            if (file.toString().contains(".doc") || file.toString().contains(".docx")) { // Chequea el tipo de extension del archivo y aplica el tipo al intent

                intent.setDataAndType(uri, "application/msword"); // Word document
            } else if (file.toString().contains(".pdf")) {

                intent.setDataAndType(uri, "application/pdf");  // PDF file
            } else if (file.toString().contains(".ppt") || file.toString().contains(".pptx")) {

                intent.setDataAndType(uri, "application/vnd.ms-powerpoint"); // Powerpoint file
            } else if (file.toString().contains(".xls") || file.toString().contains(".xlsx")) {

                intent.setDataAndType(uri, "application/vnd.ms-excel");  // Excel file
            } else if (file.toString().contains(".zip") || file.toString().contains(".rar")) {

                intent.setDataAndType(uri, "application/x-wav");  // WAV audio file
            } else if (file.toString().contains(".rtf")) {

                intent.setDataAndType(uri, "application/rtf");  // RTF file
            } else if (file.toString().contains(".wav") || file.toString().contains(".mp3")) {

                intent.setDataAndType(uri, "audio/x-wav"); // WAV audio file
            } else if (file.toString().contains(".gif")) {

                intent.setDataAndType(uri, "image/gif"); // GIF file
            } else if (file.toString().contains(".jpg") || file.toString().contains(".jpeg") || file.toString().contains(".png")) {

                intent.setDataAndType(uri, "image/jpeg"); // JPG file
            } else if (file.toString().contains(".txt")) {

                intent.setDataAndType(uri, "text/plain"); // Text file
            } else if (file.toString().contains(".3gp") || file.toString().contains(".mpg") || file.toString().contains(".mpeg") || file.toString().contains(".mpe") || file.toString().contains(".mp4") || file.toString().contains(".avi")) {

                intent.setDataAndType(uri, "video/*"); // Video files
            } else {
                intent.setDataAndType(uri, "*/*"); // Otro tipo de archivo android enseñara posibilidades de como abrirlo
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}



