package com.example.shred;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends DataLogging {

    public static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    public static Map<Integer, Float[]> map = new TreeMap<>();
    public static Map<Integer, Float[]> fretMap = new ConcurrentHashMap<>();
    public int count = 0;
    public float topHeight = 0.0f;

    public static List<UsbSerialDriver> drivers;
    public static UsbManager manager;
    public static UsbDevice device;
    public static UsbSerialPort port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        drivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (drivers.size() > 0) {
            device = drivers.get(0).getDevice();
            port = drivers.get(0).getPorts().get(0);
            if (!manager.hasPermission(device)) {
                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
                manager.requestPermission(device, usbPermissionIntent);
            } else {
                UsbDeviceConnection connection = manager.openDevice(device);
                try {
                    port.open(connection);
                    port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                    byte[] data = new byte[1];
                    data[0] = 'h';
                    for (int k = 0; k < 1000; k++) {
                        port.write(data, 200);
                    }
                } catch (Exception e) {

                }

            }

        }
//        if (!manager.hasPermission(device)) {
//            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
//            manager.requestPermission(device, usbPermissionIntent);
//        } else {
//            // Permission Granted to Open Device on that Port
//            UsbDeviceConnection connection = manager.openDevice(device);
//            try {
//
//                port.open(connection);
//                port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
//                byte[] data = new byte[1];
//                data[0] = 'h';
//                for (int k = 0; k < 1000; k++) {
//                    port.write(data, 200);
//                }
//
//
//            } catch (Exception e) {
//                try {
//                    port.close();
//                } catch (Exception e2) {
//
//                }
//            }
//

//        result = new ArrayList<UsbSerialPort>();


        View[] strings = new View[6];
        final View[] frets = new View[2];
        strings[0] = findViewById(R.id.string1);
        strings[1] = findViewById(R.id.string2);
        strings[2] = findViewById(R.id.string3);
        strings[3] = findViewById(R.id.string4);
        strings[4] = findViewById(R.id.string5);
        strings[5] = findViewById(R.id.string6);

        frets[0] = findViewById(R.id.fret1);
        frets[1] = findViewById(R.id.fret2);
        for (final View v: strings) {
            v.post(new Runnable() {
                @Override
                public void run() {
                    Float[] myFloats = new Float[2];
                    myFloats[0] = v.getX();
                    myFloats[1] = v.getX() + v.getWidth();
                    map.put(v.getId(), myFloats);
                }
            });
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;

        for (final View v2: frets) {
            v2.post(new Runnable() {
                @Override
                public void run() {
//                    Log.d("Value: ", v2.getY() + ":" + v2.getMeasuredHeight());
                    Float[] myFloats = new Float[2];
                    if (count == 0) {
                        myFloats[0] = 0.0f;
                        myFloats[1] = v2.getY() + v2.getMeasuredHeight();
                        topHeight = myFloats[1];
                        fretMap.put(0, myFloats);
//                        Log.d("VALUES: ", myFloats[0] + ":" + myFloats[1]);
                    } else {
//                        Log.d("in here", "1");
                        myFloats[0] = topHeight;
                        myFloats[1] = v2.getY() + v2.getMeasuredHeight();
                        fretMap.put(1, myFloats);
//                        Log.d("VALUES: ", myFloats[0] + ":" + myFloats[1]);
                        myFloats[0] = v2.getY() + v2.getMeasuredHeight();
                        myFloats[1] = (float) height;
                        fretMap.put(2, myFloats);
//                        Log.d("VALUES: ", myFloats[0] + ":" + myFloats[1]);
                    }
                    count++;
                    if (count == 2) {
                        Float orig = fretMap.get(0)[1];
                        Float[] add = new Float[2];
                        Float[] old = fretMap.get(1);
                        add[0] = orig;
                        add[1] = fretMap.get(2)[0];
                        fretMap.put(1, add);
                    }
                }
            });
        }



    }
}
