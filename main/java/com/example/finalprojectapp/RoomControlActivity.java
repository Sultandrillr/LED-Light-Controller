package com.example.finalprojectapp;

import android.util.Log;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import android.os.Handler;

public class RoomControlActivity extends AppCompatActivity {

    private int roomId;
    private String roomName;
    private TextView textViewRoomName;
    private Switch switchPower;
    private Switch switchMotionSensing;
    private Button buttonPickColor;
    private View viewColorPreview;
    private Button buttonUpdate;
    private TextView textViewOccupancy;

    private int currentHue = 0;
    private int currentSaturation = 0;
    private int currentValue = 255;
    private boolean isPowerOn = false;
    private boolean motionSensing = false;
    private int occupancy;

    private Handler debounceHandler = new Handler();
    private Runnable debounceRunnable;
    private static final long DEBOUNCE_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_control);

        roomId = getIntent().getIntExtra("room_id", 0);
        roomName = getIntent().getStringExtra("roomname");
        textViewRoomName = findViewById(R.id.textViewRoomName);
        switchPower = findViewById(R.id.switchPower);
        switchMotionSensing = findViewById(R.id.switchMotionSensing);
        buttonPickColor = findViewById(R.id.buttonPickColor);
        viewColorPreview = findViewById(R.id.viewColorPreview);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        occupancy = getIntent().getIntExtra("Occupancy", 0);
        textViewOccupancy = findViewById(R.id.textViewOccupancy);

        textViewRoomName.setText(roomName);
        loadOccupancy();
        viewColorPreview.setBackgroundColor(hsvToRgb(currentHue, currentSaturation, currentValue));

        buttonPickColor.setOnClickListener(v -> {
            new ColorPickerDialog
                    .Builder(this)
                    .setTitle("Light Color")
                    .setColorShape(ColorShape.CIRCLE)
                    .setDefaultColor(hsvToRgb(currentHue, currentSaturation, currentValue))
                    .setColorListener(new ColorListener() {
                        @Override
                        public void onColorSelected(int color, @NonNull String colorHex) {
                            //Did not set to automatically update lights/database so as to not cause issues with occupancy updates(if user does not yet want to update room capacity)
                            float[] hsv = new float[3];
                            Color.colorToHSV(color, hsv);
                            currentHue =  (int) hsv[0];
                            currentSaturation =  (int) (hsv[1] * 255);
                            currentValue = (int) (hsv[2] * 255);
                            viewColorPreview.setBackgroundColor(color);
                        }
                    }).show();
        });

        buttonUpdate.setOnClickListener(v -> updateLightState());
    }

    public int hsvToRgb(int h, int s, int v){
        float[] hsvTemp = {h, s/255f, v/255f};
        return Color.HSVToColor(hsvTemp);
             //Color.HSVToColor only takes an array of floats. dont remove this
    }
    private void loadOccupancy(){
        API.getOccupancy(roomId, new API.OccupancyCallback() {
            @Override
            public void onSuccess(int people_count) {
                occupancy = people_count;
                textViewOccupancy.setText(String.valueOf(occupancy));
            }

            @Override
            public void onError(String error) {
                Log.e("Failed to load", "Failed to load occupancy"+ error);
            }});
    }

    private void updateLightState(){
        isPowerOn = switchPower.isChecked();
        motionSensing = switchMotionSensing.isChecked();
        API.updateLedState(roomId, isPowerOn, currentHue, currentSaturation, currentValue, new API.UpdateStateCallback() {
            @Override
            public void onSuccess() {
                Log.d("Light successfully updated", "Light Updated Succesfully");
                loadOccupancy();
            }

            @Override
            public void onError(String error) {
                Log.d("Update failed", error);
            }
        });

        API.updateMotion(roomId, motionSensing, new API.UpdateStateCallback() {
            @Override
            public void onSuccess() {
                Log.d("Motion Sensing Updated", "Motion Sensing updated successfully");
            }

            @Override
            public void onError(String error) {
                Log.d("Update failed", error);
            }
        });

        API.updateOccupancy(roomId, occupancy, new API.UpdateStateCallback() {
            @Override
            public void onSuccess() {
                Log.d("Occupancy Updated", "Occupancy updated successfully");
            }

            @Override
            public void onError(String error) {
                Log.d("Update failed", error);
            }
        });



    }
}