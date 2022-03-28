package com.example.client.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.client.R;
import com.example.client.models.Channel;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> implements View.OnClickListener {

    // Definiciones

    // Variables
    private List<Channel> channels; //ArrayList para los canales, donde cada uno de los items se corresponderan con un ViewHolder
    private View.OnClickListener listener; // Escuchador para cuando se selecciona un item

    /**
     * Especifica como va lucir cada uno de los items del listado
     *
     * @param channels Lista de canales
     */
    public ChannelAdapter(List<Channel> channels) {
        this.channels = channels;
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
        return channels.size(); // Retornamos el tamaño de nuestra lista de canales
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels; // Asignamos los nuevos canalaes a la lista
        notifyDataSetChanged(); // Notificamos al adaptador de cambios en los datos
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onClick(view);
        }
    }

    /**
     * Clase interna para obtener la referencia view de cada uno de los componentes que aparecen en un item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Definiciones
        // Componentes de la vista
        TextView nameChannel;

        /**
         * Constructor: ViewHolder
         *
         * @param view Recibira el CardView para tomar la referencia de los items
         */
        ViewHolder(View view) {
            super(view);

            // Inicializacion de componentes de la vista
            nameChannel = view.findViewById(R.id.tv_name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // Creamos la nueva vista para el item y retornamos un instancia de ella
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_channel, viewGroup, false);
        view.setOnClickListener(this); // Definir OnclickListenes para el item pasando su listener

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Asignacion de valores a cada componente de la vista
        viewHolder.nameChannel.setText(channels.get(position).getName());
    }

}