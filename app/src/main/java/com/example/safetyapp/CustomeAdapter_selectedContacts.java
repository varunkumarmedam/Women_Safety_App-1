package com.example.safetyapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomeAdapter_selectedContacts extends RecyclerView.Adapter<ViewHolder> {

    Home Home;
    List<Model> modelList;
    Context context;

    public CustomeAdapter_selectedContacts(Home home, List<Model> modelList) {
        this.Home = home;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_contactslist, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemView);
        viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Home);
                String[] options = {"Delete"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            Home.deleteData(position);
                        }
                    }
                }).create().show();
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mName.setText(modelList.get(position).getName());
        holder.mPhonenumber.setText(modelList.get(position).getPhonenumber());
        holder.mSelected_type.setText(modelList.get(position).getSelected_type());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}

