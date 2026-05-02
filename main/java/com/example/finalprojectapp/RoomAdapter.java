package com.example.finalprojectapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;
    private Context context;

    //This is the constructor
    public RoomAdapter(Context context, List<Room> roomList){
        this.roomList = roomList;
        this.context = context;
    }

    @NonNull
    @Override
    public RoomAdapter.RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomAdapter.RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.roomName.setText(room.getName());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RoomControlActivity.class);
            intent.putExtra("room_id", room.getID());
            intent.putExtra("room_name", room.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomName;
        public RoomViewHolder(View itemview) {
            super(itemview);
            roomName = itemview.findViewById(R.id.textViewRoomName);
        }
    }
}
