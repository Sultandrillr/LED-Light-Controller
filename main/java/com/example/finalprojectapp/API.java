package com.example.finalprojectapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.hardware.camera2.CameraDevice;
import android.os.AsyncTask; //async lets you put tasks in the backgound so the UI doesnt lag
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class API {
    private static final String BASE_URL = "https://qocfzpp98l.execute-api.eu-west-2.amazonaws.com/prod";

    public static void updateLedState(int roomId,
                                      boolean isPowerOn,
                                      int currentHue,
                                      int currentSaturation,
                                      int currentValue,
                                      API.UpdateStateCallback updateStateCallback) {
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... voids){
                try{
                    URL url = new URL(BASE_URL + "/rooms/" + roomId + "/state");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST"); //Testing sum, change back to POST
                    con.setRequestProperty("Content-Type", "application/json");//Im not sure what to put here yet, user agent or prototype
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);

                    con.setDoOutput(true);
                    OutputStream os = con.getOutputStream();
                    String jsonParams = String.format(
                            "{\"is_on\":%b,\"hue\":%d,\"saturation\":%d,\"value\":%d}",
                            isPowerOn, currentHue, currentSaturation, currentValue
                    );
                    os.write(jsonParams.getBytes());
                    os.flush();
                    os.close();

                    int status = con.getResponseCode();
                    con.disconnect();
                    System.out.println("POST Response Code :: " + status);
                    if (status ==HttpURLConnection.HTTP_OK){
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        System.out.println(response.toString());
                    }else{
                        System.out.println("POST Request failed");
                    }


                } catch (ProtocolException e) {
                    throw new RuntimeException(e);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }
    public static void updateMotion(int roomId,
                                    boolean motionSensing,
                                      API.UpdateStateCallback updateStateCallback) {
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... voids){
                try{
                    URL url = new URL(BASE_URL + "/rooms/" + roomId + "/motion");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST"); //Testing sum, change back to POST
                    con.setRequestProperty("Content-Type", "application/json");//Im not sure what to put here yet, user agent or prototype
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);

                    con.setDoOutput(true);
                    OutputStream os = con.getOutputStream();
                    String jsonParams = String.format(
                            "{\"motion_sensing_enabled\":%b}",
                            motionSensing
                    );
                    os.write(jsonParams.getBytes());
                    os.flush();
                    os.close();

                    int status = con.getResponseCode();
                    con.disconnect();

                    System.out.println("POST Response Code :: " + status);
                    if (status ==HttpURLConnection.HTTP_OK){
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        System.out.println(response.toString());
                    }else{
                        System.out.println("POST Request failed");
                    }

                } catch (ProtocolException e) {
                    throw new RuntimeException(e);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    public static void updateOccupancy(int roomId,
                                    int occupancy,
                                    API.UpdateStateCallback updateStateCallback) {
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... voids){
                try{
                    URL url = new URL(BASE_URL + "/rooms/" + roomId + "/occupancy");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST"); //Testing sum, change back to POST
                    con.setRequestProperty("Content-Type", "application/json");//Im not sure what to put here yet, user agent or prototype
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);

                    con.setDoOutput(true);
                    OutputStream os = con.getOutputStream();
                    String jsonParams = String.format(
                            "{\"occupancy\":%d}",
                            occupancy
                    );
                    os.write(jsonParams.getBytes());
                    os.flush();
                    os.close();

                    int status = con.getResponseCode();
                    con.disconnect();

                    System.out.println("POST Response Code :: " + status);
                    if (status ==HttpURLConnection.HTTP_OK){
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        System.out.println(response.toString());
                    }else{
                        System.out.println("POST Request failed");
                    }

                } catch (ProtocolException e) {
                    throw new RuntimeException(e);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    public interface RoomsCallback {
        void onSuccess(List<Room> rooms);

        void onError(String error);
    }

    public static void getRooms(RoomsCallback callback) {
        new AsyncTask<Void, Void, List<Room>>() {
            private String errorMsg = null;

            @Override
            protected List<Room> doInBackground(Void... voids) {
                try {
                    URL url = new URL(BASE_URL + "/rooms");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);

                    int status = con.getResponseCode();
                    System.out.println("GET /rooms Response Code: " + status);
                    if (status == 200) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer content = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        in.close();
                        con.disconnect();

                        System.out.println("GET /rooms Response: " + content.toString());

                        //extract key-values from json content
                        JSONArray jsonArray = new JSONArray(content.toString());
                        List<Room> rooms = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            rooms.add(new Room(obj.getInt("room_id"), obj.getString("name")));
                        }
                        return rooms;
                    } else {
                        errorMsg = "GET request failed. Status: " + status;
                        return null;
                    }

                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void onPostExecute(List<Room> roomList) {
                if (roomList != null) {
                    callback.onSuccess(roomList);
                } else {
                    callback.onError(errorMsg);
                }
            }
        }.execute();

    }

    public interface OccupancyCallback {
        void onSuccess(int occupancy);

        void onError(String error);
    }
    public static void getOccupancy(int roomId, OccupancyCallback callback){
        new AsyncTask<Void, Void, Integer>() {
            private String errorMsg = null;

            @Override
            protected Integer doInBackground(Void... voids) {
                try {
                    URL url = new URL(BASE_URL + "/rooms/" + roomId + "/occupancy");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);

                    int status = con.getResponseCode();
                    if (status == 200) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer content = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        in.close();
                        con.disconnect();

                        JSONObject obj = new JSONObject(content.toString());
                        return obj.getInt("people_count");
                    } else {
                        errorMsg = "GET request failed. Status: " + status;
                        return null;
                    }

                } catch (Exception e) {
                    errorMsg = "Error: " + e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Integer count) {
                if (count != null) {
                    callback.onSuccess(count);
                } else {
                    callback.onError(errorMsg);
                }
            }
        }.execute();
    }

    public static abstract class UpdateStateCallback {
        public abstract void onSuccess();

        public abstract void onError(String error);
    }
}
