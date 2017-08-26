package com.quoccuong.usingtheaccelerometer;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;


public class SaveLogFile extends Activity {
    private Button btn_save;
    private Button btn_cacel;
    private Button btn_reset;
    private EditText edit_file_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_log_file);

        btn_save = (Button) findViewById(R.id.btn_save);
        btn_cacel = (Button) findViewById(R.id.btn_cacel);
        btn_reset = (Button) findViewById(R.id.btn_reset);
        edit_file_name = (EditText) findViewById(R.id.edit_file_name);

        btn_cacel.setOnClickListener(new MyOnClickListenner());
        btn_save.setOnClickListener(new MyOnClickListenner());
        btn_reset.setOnClickListener(new MyOnClickListenner());
    }

    class MyOnClickListenner implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_cacel) {
                finish();
                return;
            }

            if (v.getId() == R.id.btn_save) {
                String fileName = edit_file_name.getText().toString();
                if (fileName.isEmpty()) {
                    Toast.makeText(SaveLogFile.this, "Enter log file name.", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        Calendar calendar = Calendar.getInstance();
                        DecimalFormat df = new DecimalFormat("0.00000000");
                        File myFile = new File("sdcard/log_data/" + fileName + ".csv");
                        FileWriter fileWriter = new FileWriter(myFile);
                        for (int i = 0; i < MainActivity.list_time.size(); i++){
                            calendar.setTime(new Date(MainActivity.list_time.get(i)));
                            fileWriter.write("" + calendar.get(Calendar.YEAR)+ "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + "@" +
                                    calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND) +  ";" +
                                    df.format(MainActivity.list_x.get(i)) + ";" + df.format(MainActivity.list_y.get(i)) + ";" + df.format(MainActivity.list_z.get(i)) + ";" +
                                    df.format(MainActivity.list_gyr_x.get(i)) + ";" + df.format(MainActivity.list_gyr_y.get(i)) + ";" + df.format(MainActivity.list_gyr_z.get(i)) + "\r\n");
                        }
                        fileWriter.flush();
                        fileWriter.close();

//                        WritableWorkbook workbook = Workbook.createWorkbook(myFile);
//                        WritableSheet sheet = workbook.createSheet("calling_01", 0);
//
//                        for (int i = 0; i < MainActivity.list_time.size(); i++) {
//                            sheet.addCell(new DateTime(0, i, new Date(MainActivity.list_time.get(i))));
//                            sheet.addCell(new Number(1, i, MainActivity.list_x.get(i)));
//                            sheet.addCell(new Number(2, i, MainActivity.list_y.get(i)));
//                            sheet.addCell(new Number(3, i, MainActivity.list_z.get(i)));
//                            sheet.addCell(new Number(4, i, MainActivity.list_gyr_x.get(i)));
//                            sheet.addCell(new Number(5, i, MainActivity.list_gyr_y.get(i)));
//                            sheet.addCell(new Number(6, i, MainActivity.list_gyr_z.get(i)));
//                        }
//
//                        workbook.write();
//                        workbook.close();

//                        CSVWriter csvWriter = new CSVWriter(fileWriter, ';');
//
//                        for (int i = 0; i < MainActivity.list_time.size(); i++) {
//                            String[] list = new String[7];
//                            list[0] = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + "@" +
//                                    calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND);
//                            list[1] = "" + MainActivity.list_x.get(i);
//                            list[2] = "" + MainActivity.list_y.get(i);
//                            list[3] = "" + MainActivity.list_z.get(i);
//                            list[4] = "" + MainActivity.list_gyr_x.get(i);
//                            list[5] = "" + MainActivity.list_gyr_y.get(i);
//                            list[6] = "" + MainActivity.list_gyr_z.get(i);
//                            csvWriter.writeNext(list);
//                        }
//
//                        csvWriter.flush();
//                        csvWriter.close();


                        MainActivity.list_x = new ArrayList<>();
                        MainActivity.list_y = new ArrayList<>();
                        MainActivity.list_z = new ArrayList<>();
                        MainActivity.list_gyr_x = new ArrayList<>();
                        MainActivity.list_gyr_y = new ArrayList<>();
                        MainActivity.list_gyr_z = new ArrayList<>();
                        MainActivity.list_time = new ArrayList<>();

                        MainView.pause = true;
                        MainView.isSave = false;
                        Toast.makeText(SaveLogFile.this, "Save log file " + fileName, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(SaveLogFile.this, "Don't save file.", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
                return;
            }

            if (v.getId() == R.id.btn_reset) {
                MainActivity.list_x = new ArrayList<>();
                MainActivity.list_y = new ArrayList<>();
                MainActivity.list_z = new ArrayList<>();
                MainActivity.list_gyr_x = new ArrayList<>();
                MainActivity.list_gyr_y = new ArrayList<>();
                MainActivity.list_gyr_z = new ArrayList<>();
                MainActivity.list_time = new ArrayList<>();
                MainActivity.list_x = new ArrayList<>();
                MainActivity.list_y = new ArrayList<>();
                MainActivity.list_z = new ArrayList<>();
                MainActivity.list_gyr_x = new ArrayList<>();
                MainActivity.list_gyr_y = new ArrayList<>();
                MainActivity.list_gyr_z = new ArrayList<>();
                MainActivity.list_time = new ArrayList<>();

                MainView.pause = true;
                MainView.isSave = false;

                finish();
            }
        }
    }

}
