package com.example.shred;

import android.app.Activity;
import android.graphics.PointF;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataLogging extends Activity {

    public DataLogging() {

        Thread t = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    UsbSerialPort port = MainActivity.port;
                    Map<PointF, int[]> map = MultitouchView.colorIndexMap;
                    Set<Map.Entry<PointF, int[]>> eset = map.entrySet();
                    byte[] divider = new byte[1];
                    if (port != null) {
                        try {
                            synchronized (eset) {
                                divider[0] = 'x';
                                port.write(divider, 200);
                                byte[] data = computeData(eset);
                                port.write(data, 200);
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
        });
        t.start();

    }

    public static byte[] computeData(Set<Map.Entry<PointF, int[]>> eset) {

        Map<Integer, Integer> intMap = new HashMap<>();

        for (Map.Entry<PointF, int[]> entry : eset) {
            if (!intMap.containsKey(entry.getValue()[0])) {
                intMap.put(entry.getValue()[0], entry.getValue()[1]);
            } else {
                int max = Math.max(entry.getValue()[1], intMap.get(entry.getValue()[0]));
                intMap.put(entry.getValue()[0], max);
            }
        }

        byte[] data = new byte[6];

        for (int k = 0; k <= 5; k++) {
            if (!intMap.containsKey(k)) {
                data[k] = '0';
            } else {
                int val = intMap.get(k) + 1;
                if (val == 1) {
                    data[k] = '1';
                } else if (val == 2) {
                    data[k] = '2';
                } else {
                    data[k] = '3';
                }
            }
        }
        return data;
    }

}
