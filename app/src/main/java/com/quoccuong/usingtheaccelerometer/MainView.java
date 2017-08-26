package com.quoccuong.usingtheaccelerometer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

import ca.uol.aig.fftpack.RealDoubleFFT;

/**
 * Created by quoccuong on 28/05/2015.
 */
public class MainView extends View implements Runnable, SensorEventListener {


    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorGyscope;

    private AudioRecord audioRecord;
    private RecordAudio recordTask;

    private float x, y, z;
    private float gyr_x, gyr_y, gyr_z;
    private Paint paint;
    private Paint paint2;

    public static boolean pause = true;
    private int width;
    private int height;
    private Bitmap startBitmap;
    private Bitmap stopBitmap;

    private Bitmap saveBitmap;
    public static boolean isSave = false;

    private Bitmap yaw;
    private Bitmap roll;
    private Bitmap yaw2;
    private Bitmap roll2;
    private Bitmap pitch;
    private Bitmap pitch2;
    private MainActivity con;

    private int frequency = 8000;
    private int blockSize = 512;
    private double[] toTransform = null;
    private RealDoubleFFT transformer;


    public MainView(Context con) {
        super(con);
        this.con = (MainActivity) con;
        sensorManager = (SensorManager) con.getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);

        sensorGyscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, sensorGyscope, SensorManager.SENSOR_DELAY_FASTEST);
        setBackgroundColor(Color.BLACK);

        int bufferSize = AudioRecord.getMinBufferSize(frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        transformer = new RealDoubleFFT(blockSize);
        toTransform = null;

        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setTextSize(30);
        startBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap);
        stopBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap2);
        saveBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.save);

        yaw = BitmapFactory.decodeResource(getResources(), R.drawable.yaw1);
        yaw2 = BitmapFactory.decodeResource(getResources(), R.drawable.yaw2);
        roll = BitmapFactory.decodeResource(getResources(), R.drawable.roll1);
        roll2 = BitmapFactory.decodeResource(getResources(), R.drawable.roll2);
        pitch = BitmapFactory.decodeResource(getResources(), R.drawable.pitch1);
        pitch2 = BitmapFactory.decodeResource(getResources(), R.drawable.pitch2);
        try {
            Typeface typeface = Typeface.createFromFile(new File(Environment.getExternalStorageDirectory(), "erbos_draco_1st_open_nbp.ttf"));
            paint.setTypeface(typeface);
        } catch (Exception e) {
            Log.e("qwerty", "error " + e.toString());
        }

        paint2 = new Paint();
        paint2.setColor(Color.BLUE);
        paint2.setTextSize(60);
        paint2.setStrokeWidth(5);

        x = 0;
        y = 0;
        z = 0;
        gyr_x = 0;
        gyr_y = 0;
        gyr_z = 0;

        DisplayMetrics displayMetrics = con.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;


        Thread run = new Thread(this);
        run.start();
    }

    private void drawDottedLine(Canvas canvas, Paint paint, float start_x, float start_y, float stop_x, float stop_y, float[] value) {
        // 1
        if (start_x >= stop_x && start_y >= stop_y) {
            float ix = start_x;
            float iy = start_y;

            float d = (float) Math.sqrt((start_x - stop_x) * (start_x - stop_x) + ((start_y - stop_y) * (start_y - stop_y)));

            float detal_x = (float) (start_x - stop_x) * value[0] / d;
            float detal_y = (float) (start_y - stop_y) * value[0] / d;

            float detal_x1 = (float) (start_x - stop_x) * value[1] / d;
            float detal_y1 = (float) (start_y - stop_y) * value[1] / d;

            float jx = 0, jy = 0;
            while (ix >= stop_x && iy >= stop_y) {
                jx = ix - detal_x;
                jy = iy - detal_y;

                if (jx < stop_x) {
                    jx = stop_x;
                }

                if (jy < stop_y) {
                    jy = stop_y;
                }

                canvas.drawLine(ix, iy, jx, jy, paint);

                ix = jx - detal_x1;
                iy = jy - detal_y1;
            }
            return;
        }

        // 2
        if (start_x <= stop_x && start_y >= stop_y) {
            float d = (float) Math.sqrt((start_x - stop_x) * (start_x - stop_x) + ((start_y - stop_y) * (start_y - stop_y)));

            float detal_x = (float) (value[0] * (stop_x - start_x)) / d;
            float detal_y = (float) (value[0] * (start_y - stop_y)) / d;

            float detal_x1 = (float) (value[1] * (stop_x - start_x)) / d;
            float detal_y1 = (float) (value[1] * (start_y - stop_y)) / d;

            float ix = start_x, iy = start_y;
            float jx = 0, jy = 0;

            while (ix <= stop_x && iy >= stop_y) {
                jx = ix + detal_x;
                jy = iy - detal_y;

                if (jx > stop_x) {
                    jx = stop_x;
                }

                if (jy < stop_y) {
                    jy = stop_y;
                }

                canvas.drawLine(ix, iy, jx, jy, paint);

                ix = jx + detal_x1;
                iy = jy - detal_y1;
            }
            return;
        }

        drawDottedLine(canvas, paint, stop_x, stop_y, start_x, start_y, value);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(20);
            } catch (Exception e) {
            }

            if (!pause) {
                postInvalidate();

                MainActivity.list_x.add(x);
                MainActivity.list_y.add(y);
                MainActivity.list_z.add(z);

                MainActivity.list_gyr_x.add(gyr_x);
                MainActivity.list_gyr_y.add(gyr_y);
                MainActivity.list_gyr_z.add(gyr_z);
                MainActivity.list_time.add(System.currentTimeMillis());
            }
        }
    }

    class MyPoint {
        float x;
        float y;

        public MyPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private MyPoint getPoint2DFromPoint3D(float x, float y, float z) {
        float point_x = 0, point_y = 0;

        point_y = (float) (750 + y * 20 * Math.sin(Math.PI / 4) + z * 20);
        point_x = (float) (width / 2 - x * 20 - y * 20 * Math.cos(Math.PI / 4));

        MyPoint myPoint = new MyPoint(point_x, point_y);
        return myPoint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        MyPoint vector = getPoint2DFromPoint3D(x, y, z);
//        MyPoint x0 = getPoint2DFromPoint3D(x, 0, 0);
//        MyPoint y0 = getPoint2DFromPoint3D(0, y, 0);
//        MyPoint z0 = getPoint2DFromPoint3D(0, 0, z);
//
//        if (z > 0) {
//            paint2.setStrokeWidth(5);
//            paint2.setColor(Color.BLUE);
//            drawDottedLine(canvas, paint2, width / 2, 750, vector.x, vector.y, new float[]{25, 5});
//            paint2.setStrokeWidth(1);
//            paint2.setColor(Color.GREEN);
//            drawDottedLine(canvas, paint2, vector.x, vector.y, vector.x, vector.y - z * 20, new float[]{25, 5});
//            drawDottedLine(canvas, paint2, vector.x, vector.y - z * 20, x0.x, x0.y, new float[]{25, 5});
//            drawDottedLine(canvas, paint2, vector.x, vector.y - z * 20, y0.x, y0.y, new float[]{25, 5});
//            drawDottedLine(canvas, paint2, vector.x, vector.y, z0.x, z0.y, new float[]{25, 5});
//        }
//
//        MyPoint a = getPoint2DFromPoint3D(100 / 20, (-16 * 100 / 9) / 20, 0);
//        MyPoint b = getPoint2DFromPoint3D(-100 / 20, (-16 * 100 / 9) / 20, 0);
//        MyPoint c = getPoint2DFromPoint3D(-100 / 20, (16 * 100 / 9) / 20, 0);
//        MyPoint d = getPoint2DFromPoint3D(100 / 20, (16 * 100 / 9) / 20, 0);


        canvas.drawText("ACCELEROMETER", 20, 50, paint);
        canvas.drawText("x ", 20, 100, paint);
        canvas.drawText("y ", 20, 250, paint);
        canvas.drawText("z ", 20, 400, paint);

        paint2.setStrokeWidth(2);
        paint2.setColor(Color.WHITE);
        canvas.drawLine(50, 100, width, 100, paint2);

        Paint paint3 = new Paint();
        Paint paint4 = new Paint();
        paint3.setStrokeWidth(2);
        paint3.setColor(Color.BLUE);
        paint4.setStrokeWidth(2);
        paint4.setColor(Color.YELLOW);

        paint2.setColor(Color.RED);
        if (MainActivity.list_x.size() <= width - 50) {
            for (int i = 0; i < MainActivity.list_x.size() - 1; i++) {
                canvas.drawLine(50 + i, (float) (100 - MainActivity.list_x.get(i) * 5), 50 + i + 1, (float) (100 - MainActivity.list_x.get(i + 1) * 5), paint2);
                canvas.drawLine(50 + i, (float) (100 - MainActivity.list_y.get(i) * 5), 50 + i + 1, (float) (100 - MainActivity.list_y.get(i + 1) * 5), paint3);
                canvas.drawLine(50 + i, (float) (100 - MainActivity.list_z.get(i) * 5), 50 + i + 1, (float) (100 - MainActivity.list_z.get(i + 1) * 5), paint4);
            }
        } else {
            int j = 0;
            for (int i = MainActivity.list_x.size() - width + 50; i < MainActivity.list_x.size() - 1; i++, j++) {
                canvas.drawLine(50 + j, (float) (100 - MainActivity.list_x.get(i) * 5), 50 + j + 1, (float) (100 - MainActivity.list_x.get(i + 1) * 5), paint2);
                canvas.drawLine(50 + j, (float) (100 - MainActivity.list_y.get(i) * 5), 50 + j + 1, (float) (100 - MainActivity.list_y.get(i + 1) * 5), paint3);
                canvas.drawLine(50 + j, (float) (100 - MainActivity.list_z.get(i) * 5), 50 + j + 1, (float) (100 - MainActivity.list_z.get(i + 1) * 5), paint4);
            }
        }
//
//        paint2.setStrokeWidth(3);
//        // ad
//        paint2.setColor(Color.GREEN);
//        canvas.drawLine(a.x, a.y, d.x, d.y, paint2);
//        paint2.setColor(Color.RED);
//
//        // z
//
//        canvas.drawLine(width / 2, 500, width / 2, 750, paint2);
//        drawDottedLine(canvas, paint2, width / 2, 750, width / 2, 1100, new float[]{25, 5});
//        canvas.drawText("z", width / 2, 1100 + 20, paint);
//        canvas.drawLine(width / 2, 1100, width / 2 + (float) (25 * Math.sin(Math.PI / 4)), 1100 - (float) (25 * Math.sin(Math.PI / 4)), paint2);
//        canvas.drawLine(width / 2, 1100, width / 2 - (float) (25 * Math.sin(Math.PI / 4)), 1100 - (float) (25 * Math.sin(Math.PI / 4)), paint2);
//        if (gyr_z > 0.05) {
//            canvas.drawBitmap(yaw, width / 2 - yaw.getWidth() / 2, 500 + yaw.getHeight() / 2, paint);
//        } else if (gyr_z < -0.05) {
//            canvas.drawBitmap(yaw2, width / 2 - yaw.getWidth() / 2, 500 + yaw2.getHeight() / 2, paint);
//        }
//
//        // bc
//        paint2.setStrokeWidth(9);
//        paint2.setColor(Color.GREEN);
//        canvas.drawLine(b.x, b.y, c.x, c.y, paint2);
//        paint2.setStrokeWidth(3);
//        paint2.setColor(Color.RED);
//
//
//        //canvas.drawBitmap(roll, (float) (width / 2 + 250 + 100 - roll.getWidth() * 1.5), 750 - roll.getHeight() / 2, paint);
//        canvas.drawLine(width / 2 - 250 - 100, 750, width / 2 + 250 + 100, 750, paint2);
//        canvas.drawText("x", width / 2 - 250 - 20 - 100, 750, paint);
//        canvas.drawLine(width / 2 - 250 - 100, 750, width / 2 - 250 - 100 + (float) (25 * Math.sin(Math.PI / 4)), 750 - (float) (25 * Math.sin(Math.PI / 4)), paint2);
//        canvas.drawLine(width / 2 - 250 - 100, 750, width / 2 - 250 - 100 + (float) (25 * Math.sin(Math.PI / 4)), 750 + (float) (25 * Math.sin(Math.PI / 4)), paint2);
//        if (gyr_x > 0.05) {
//            canvas.drawBitmap(roll, (float) (width / 2 + 250 + 100 - roll2.getWidth() * 1.5), 750 - roll.getHeight() / 2, paint);
//        } else if (gyr_x < -0.05) {
//            canvas.drawBitmap(roll2, (float) (width / 2 + 250 + 100 - roll2.getWidth() * 1.5), 750 - roll.getHeight() / 2, paint);
//        }
//
//        // ab, cd
//        paint2.setColor(Color.GREEN);
//        canvas.drawLine(a.x, a.y, b.x, b.y, paint2);
//        paint2.setStrokeWidth(9);
//        canvas.drawLine(c.x, c.y, d.x, d.y, paint2);
//        paint2.setStrokeWidth(3);
//        paint2.setColor(Color.RED);
//
//        // y
//
//        canvas.drawLine(width / 2 + 250, 500, width / 2 - 250, 1000, paint2);
//        canvas.drawLine(width / 2 - 250, 1000, width / 2 - 250 + 25, 1000, paint2);
//        canvas.drawLine(width / 2 - 250, 1000, width / 2 - 250, 1000 - 25, paint2);
//        canvas.drawText("y", width / 2 - 250, 1000 + 25, paint);
//        if (gyr_y > 0.05) {
//            canvas.drawBitmap(pitch, width / 2 + 230 - pitch.getWidth() / 2, 520 - pitch.getHeight() / 2, paint);
//        } else if (gyr_y < -0.05) {
//            canvas.drawBitmap(pitch2, width / 2 + 230 - pitch.getWidth() / 2, 520 - pitch2.getHeight() / 2, paint);
//        }
//
//
//        if (z < 0) {
//            paint2.setStrokeWidth(5);
//            paint2.setColor(Color.BLUE);
//            canvas.drawLine(width / 2, 750, vector.x, vector.y, paint2);
//            paint2.setStrokeWidth(1);
//            paint2.setColor(Color.GREEN);
//            drawDottedLine(canvas, paint2, vector.x, vector.y, vector.x, vector.y - z * 20, new float[]{25, 5});
//            drawDottedLine(canvas, paint2, vector.x, vector.y - z * 20, x0.x, x0.y, new float[]{25, 5});
//            drawDottedLine(canvas, paint2, vector.x, vector.y - z * 20, y0.x, y0.y, new float[]{25, 5});
//            drawDottedLine(canvas, paint2, vector.x, vector.y, z0.x, z0.y, new float[]{25, 5});
//        }

        int gyrY = 550;
        canvas.drawText("GYROSCOPE", 20, gyrY, paint);
        if (gyr_x > 0) {
            canvas.drawText("x ", 20, gyrY + 50, paint);
        } else {
            canvas.drawText("x ", 20, gyrY + 50, paint);
        }

        if (gyr_y > 0) {
            canvas.drawText("y ", 20, gyrY + 200, paint);
        } else {
            canvas.drawText("y ", 20, gyrY + 200, paint);
        }

        if (gyr_z > 0) {
            canvas.drawText("z ", 20, gyrY + 350, paint);
        } else {
            canvas.drawText("z ", 20, gyrY + 350, paint);
        }

        paint2.setStrokeWidth(2);
        paint2.setColor(Color.BLUE);
        canvas.drawLine(50, gyrY + 50, width, gyrY + 50, paint2);
        canvas.drawLine(50, gyrY + 200, width, gyrY + 200, paint2);
        canvas.drawLine(50, gyrY + 350, width, gyrY + 350, paint2);

        paint2.setColor(Color.RED);
        if (MainActivity.list_gyr_x.size() <= width - 50) {
            for (int i = 0; i < MainActivity.list_gyr_x.size() - 1; i++) {
                canvas.drawLine(50 + i, (float) (gyrY + 50 - MainActivity.list_gyr_x.get(i) * 25), 50 + i + 1, (float) (gyrY + 50 - MainActivity.list_gyr_x.get(i + 1) * 25), paint2);
                canvas.drawLine(50 + i, (float) (gyrY + 200 - MainActivity.list_gyr_y.get(i) * 25), 50 + i + 1, (float) (gyrY + 200 - MainActivity.list_gyr_y.get(i + 1) * 25), paint2);
                canvas.drawLine(50 + i, (float) (gyrY + 350 - MainActivity.list_gyr_z.get(i) * 25), 50 + i + 1, (float) (gyrY + 350 - MainActivity.list_gyr_z.get(i + 1) * 25), paint2);
            }
        } else {
            int j = 0;
            for (int i = MainActivity.list_gyr_x.size() - width + 50; i < MainActivity.list_gyr_x.size() - 1; i++, j++) {
                canvas.drawLine(50 + j, (float) (gyrY + 50 - MainActivity.list_gyr_x.get(i) * 25), 50 + j + 1, (float) (gyrY + 50 - MainActivity.list_gyr_x.get(i + 1) * 25), paint2);
                canvas.drawLine(50 + j, (float) (gyrY + 200 - MainActivity.list_gyr_y.get(i) * 25), 50 + j + 1, (float) (gyrY + 200 - MainActivity.list_gyr_y.get(i + 1) * 25), paint2);
                canvas.drawLine(50 + j, (float) (gyrY + 350 - MainActivity.list_gyr_z.get(i) * 25), 50 + j + 1, (float) (gyrY + 350 - MainActivity.list_gyr_z.get(i + 1) * 25), paint2);
            }
        }


        int audioY = gyrY + 500;
        canvas.drawText("AUDIO RECORD", 20, audioY, paint);
        paint2.setStrokeWidth(3);
        paint2.setColor(Color.BLUE);
        //canvas.drawLine(50, audioY + 50, width, audioY + 50, paint2);

        if (toTransform != null) {
            paint2.setColor(Color.RED);
            for (int i = 0; i < toTransform.length; i++) {
                canvas.drawLine(5 + i, (int) (audioY + 50 - toTransform[i] / 20), 5 + i, audioY + 50, paint2);
            }
        }

        if (!pause) {
            canvas.drawBitmap(stopBitmap, width - stopBitmap.getWidth(), height - stopBitmap.getHeight() - 100, paint);
            if (isSave) {
                canvas.drawBitmap(saveBitmap, width - saveBitmap.getWidth() - stopBitmap.getWidth() - 100, height - saveBitmap.getHeight() - 100, paint);
            }
        } else {
            canvas.drawBitmap(startBitmap, width - startBitmap.getWidth(), height - startBitmap.getHeight() - 100, paint);
            if (isSave) {
                canvas.drawBitmap(saveBitmap, width - saveBitmap.getWidth() - startBitmap.getWidth() - 100, height - saveBitmap.getHeight() - 100, paint);
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getX() > width - startBitmap.getWidth() && event.getY() >= height - 100 - startBitmap.getHeight() && event.getY() <= height - 100) {
            pause = !pause;
            isSave = pause;
            if (!pause) {
                recordTask = new RecordAudio();
                recordTask.execute();
            } else {
                recordTask.cancel(true);
            }
        }

        if (event.getX() >= width - saveBitmap.getWidth() - startBitmap.getWidth() - 100 && event.getX() <= width - startBitmap.getWidth() - 100 &&
                event.getY() >= height - 100 - startBitmap.getHeight() && event.getY() <= height - 100) {
            con.startActivity();
            invalidate();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
        }
        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyr_x = event.values[0];
            gyr_y = event.values[1];
            gyr_z = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class RecordAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected void onProgressUpdate(double[]... values) {
            postInvalidate();
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                toTransform = new double[blockSize];
                short[] buffer = new short[blockSize];

                audioRecord.startRecording();
                while (!pause) {
                    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i]; // 32768.0; // signed
                    }
                    //transformer.ft(toTransform);
                    publishProgress(toTransform);
                }

                audioRecord.stop();

            } catch (Exception ex) {

            }
            return null;
        }
    }
}
