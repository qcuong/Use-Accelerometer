package com.quoccuong.usingtheaccelerometer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class MainActivity extends Activity{
    private MainView mainView;
    public static ArrayList<Float> list_x = new ArrayList<>();
    public static ArrayList<Float> list_y = new ArrayList<>();
    public static ArrayList<Float> list_z = new ArrayList<>();

    public static ArrayList<Float> list_gyr_x = new ArrayList<>();
    public static ArrayList<Float> list_gyr_y = new ArrayList<>();
    public static ArrayList<Float> list_gyr_z = new ArrayList<>();
    public static ArrayList<Long> list_time = new ArrayList<>();
    public static ArrayList<Long> list_audio = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainView = new MainView(this);
        setContentView(mainView);
    }

    public void startActivity(){
        startActivity(new Intent(MainActivity.this, SaveLogFile.class));
    }


}
