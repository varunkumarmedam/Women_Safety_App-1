package com.example.safetyapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {

    TextView mName, mPhonenumber, mSelected_for, mSelected_type;
    View mView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;

        mName = itemView.findViewById(R.id.contact_name);
        mPhonenumber = itemView.findViewById(R.id.contact_no);
        mSelected_for = itemView.findViewById(R.id.selected_for);
        mSelected_type = itemView.findViewById(R.id.selected_type);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               mClickListener.onItemClick(view, getAdapterPosition());
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mClickListener.onItemLongClick(view, getAdapterPosition());
                return true;
            }
        });
    }
    private ViewHolder.ClickListener mClickListener;
    public interface  ClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public void setOnClickListener(ViewHolder.ClickListener clickListener){
       mClickListener = clickListener;
    }
}
