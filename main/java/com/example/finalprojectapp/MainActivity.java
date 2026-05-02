package com.example.finalprojectapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RoomAdapter adapter;
    private List<Room> roomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewRooms);

        //sample data for testing
        roomList = new ArrayList<>();
        adapter = new RoomAdapter(this, roomList);
        API.getRooms(new API.RoomsCallback(){
            @Override
            public void onSuccess(List<Room> rooms){
                roomList.clear();
                roomList.addAll(rooms);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                System.out.println("API Related Error");
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}