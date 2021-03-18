package com.example.safetyapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CustomeAdapter_contacts extends RecyclerView.Adapter<ViewHolder> {

    read_contacts ReadContacts;
    List<Model> modelList;
    Context context;

    public CustomeAdapter_contacts(read_contacts readContacts, List<Model> modelList) {
        this.ReadContacts = readContacts;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemView);
        viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ReadContacts);
                String[] options = {"Only Call", "Only Message", "Both Call & Message"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            ReadContacts.onlyCall(position);
                        }
                        if (i == 1) {
                            ReadContacts.onlyMessage(position);
                        }
                        if (i == 2) {
                            ReadContacts.bothCall_Message(position);
                        }
                    }
                }).create().show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mName.setText(modelList.get(position).getName());
        holder.mPhonenumber.setText(modelList.get(position).getPhonenumber());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
