package com.example.client.ui.adapters;

import static com.example.client.utility.Utility.pxToDp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.client.R;
import com.example.client.models.Message;

import java.util.Base64;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> implements View.OnClickListener {

    // Definiciones

    // Variables
    private List<Message> messages; // ArrayList para los mensajes, donde cada uno de los items se corresponderan con un ViewHolder
    private View.OnClickListener listener; // Escuchador para cuando se selecciona un item

    /**
     * Especifica como va lucir cada uno de los items del listado
     *
     * @param messages Lista de mensajes
     */
    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * Define el OnClickListener para los items
     *
     * @param listener OnClickListenes
     */
    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }


    @Override
    public int getItemCount() {
        return messages.size(); // Retornamos el tamaño de nuestra lista de mensajes para un canal
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages; // Asignamos los nuevos mensajes para el canalaes a la lista
        notifyDataSetChanged(); // Notificamos al adaptador de cambios en los datos
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onClick(view);
        }
    }

    /**
     * Clase Interna para obtener la referencia view de cada uno de los componentes que aparecen en un item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Definiciones

        //Contexto
        Context context;

        // Componentes de la vista
        TextView tvMsg;
        TextView tvUser;
        TextView tvDate;
        ImageView ivImageMsg;
        LinearLayout llTextViewMsg;
        LinearLayout llImageViewMsg;
        ViewGroup.LayoutParams paramsLlTextViewMsg;  // Parametros de LinearLayout padre del TextView
        ViewGroup.LayoutParams paramsLlImageViewMsg;  // Parametros de LinearLayout padre del ImageView

        /**
         * Constructor: ViewHolder
         *
         * @param view Recibira el CardView para tomar la referencia de los items
         */
        ViewHolder(View view) {
            super(view);

            // Inicializacion de componentes de la vista
            tvUser = view.findViewById(R.id.tv_user);
            tvDate = view.findViewById(R.id.tv_date);
            tvMsg = view.findViewById(R.id.tv_msg);
            ivImageMsg = view.findViewById(R.id.iv_image_msg);
            llTextViewMsg = view.findViewById(R.id.ll_text_view_msg);
            llImageViewMsg = view.findViewById(R.id.ll_image_view_msg);
            paramsLlTextViewMsg = llTextViewMsg.getLayoutParams();   // Obtenemos los parametros del LayoutManager de ImageView
            paramsLlImageViewMsg = llImageViewMsg.getLayoutParams();   // Obtenemos los parametros del LayoutManager de ImageView

            // Inicializaciones
            context = view.getContext(); // Inicializamos el contexto
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // Creamos la nueva vista para el item y retornamos un instancia de ella
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_message, viewGroup, false);
        view.setOnClickListener(this); // Definir OnclickListenes para el item pasando su listener
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        viewHolder.setIsRecyclable(false);
        if (!messages.get(position).getBase64file().contains("null")) { // Si el mensaje contiene una imagen
            String image = messages.get(position).getBase64file(); //Obtenemos la imagen que esta en base64
            byte[] decodedString = Base64.getDecoder().decode(image); //Descodificamos la imagen en bytes
            Bitmap bitmapImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); //Creamos el bitmap para la imagen

            if (bitmapImage != null) {
                viewHolder.ivImageMsg.setImageBitmap(bitmapImage); // Asignamos el bitmap al componente de visualizacion
            } else {
                viewHolder.ivImageMsg.setBackgroundResource(R.drawable.doc);
            }

            viewHolder.paramsLlImageViewMsg.height = pxToDp(200, viewHolder.context); // Establecemos la altura del Layout de la image para su visualizacion
            viewHolder.llImageViewMsg.setLayoutParams(viewHolder.paramsLlImageViewMsg);
            viewHolder.tvUser.setText(messages.get(position).getUser().getAddress()); // Asignamos el usuario emisor
            viewHolder.tvDate.setText(messages.get(position).getDate()); // Asignamos la fecha del mensaje

            if (!messages.get(position).getContent().contains("null")) { // Si la imagen contiene mensaje como descripcion

                viewHolder.paramsLlTextViewMsg.height = ViewGroup.LayoutParams.MATCH_PARENT; // Establecemos la altura del Layout del mensaje para su visualizacion
                viewHolder.llTextViewMsg.setLayoutParams(viewHolder.paramsLlTextViewMsg);

                viewHolder.tvMsg.setText(messages.get(position).getContent()); // Asignamos el mensaje
            }

        } else if (!messages.get(position).getContent().contains("null")) { // Si el contenido del mensaje es diferente a null

            viewHolder.ivImageMsg.setImageDrawable(null);
            viewHolder.paramsLlTextViewMsg.height = ViewGroup.LayoutParams.MATCH_PARENT; // Establecemos la altura del Layout del mensaje para su visualizacion
            viewHolder.llTextViewMsg.setLayoutParams(viewHolder.paramsLlTextViewMsg);

            viewHolder.tvMsg.setText(messages.get(position).getContent()); // Asignamos el mensaje
            viewHolder.tvUser.setText(messages.get(position).getUser().getAddress()); // Asignamos el usuario emisor
            viewHolder.tvDate.setText(messages.get(position).getDate()); // Asignamos la fecha del mensaje
        }
    }

}