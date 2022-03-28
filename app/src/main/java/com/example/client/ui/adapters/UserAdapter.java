package com.example.client.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.client.R;
import com.example.client.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    // Definiciones
    // Variables
    private List<User> users; //ArrayList para los usuarios del canal, donde cada uno de los items se corresponderan con un ViewHolder

    /**
     * Especifica como va lucir cada uno de los items del listado
     *
     * @param users Lista de usuarios de un canal
     */
    public UserAdapter(List<User> users) {
        this.users = users;
    }

    @Override
    public int getItemCount() {
        return users.size(); // Retornamos el tamaño de nuestra lista de usuarios
    }

    public void setUsers(List<User> users) {
        this.users = users; // Asignamos los nuevos usuarios a la lista
        notifyDataSetChanged(); // Notificamos al adaptador de cambios en los datos
    }

    /**
     * Clase interna para obtener la referencia view de cada uno de los componentes que aparecen en un item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Definiciones
        // Componentes de la vista
        TextView tvNameUser;

        /**
         * Constructor: ViewHolder
         *
         * @param view Recibira el CardView para tomar la referencia de los items
         */
        ViewHolder(View view) {
            super(view);

            // Inicializacion de componentes de la vista
            tvNameUser = view.findViewById(R.id.tv_name_user);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // Creamos la nueva vista para el item y retornamos un instancia de ella
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_user, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Asignacion de valores a cada componente de la vista
        viewHolder.tvNameUser.setText(users.get(position).getAddress());
    }
}