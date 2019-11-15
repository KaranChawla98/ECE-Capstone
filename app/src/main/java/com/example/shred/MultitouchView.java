package com.example.shred;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultitouchView extends View {

    private static final int SIZE = 90;
    public Lock myLock = new ReentrantLock();
    public  Map<Integer, Float[]> xMap;
    public  Map<Integer, Float[]> yMap;
    public int[] colors = { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.MAGENTA, Color.CYAN};


    public static SparseArray<PointF> mActivePointers;
    public static Map<PointF, int[]> colorIndexMap = new ConcurrentHashMap<>();
    private Paint mPaint;
    private Paint textPaint;


    public MultitouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mActivePointers = new SparseArray<PointF>();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // set painter color to a color you like
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(20);
    }

    public int[] mapColor(PointF p) {
        yMap = MainActivity.fretMap;
//        for(Map.Entry<Integer, Float[]> entry: yMap.entrySet()) {
//            Log.d("INDEX:", entry.getValue()[0] + ":" + entry.getValue()[1]);
//        }

        Float minXDistance = 9999.99f;
        Float minYDistance = 9999.99f;
        int minXIndex = 0;
        int minYIndex = 0;
        int count = 0;
//        Log.d("Y Value: ", p.y + "");
        for(Map.Entry<Integer, Float[]> entry: xMap.entrySet()) {
            Float f1 = entry.getValue()[0];
            Float f2 = entry.getValue()[1];
            if (Math.abs(p.x - f1) < minXDistance) {
                minXDistance = Math.abs(p.x - f1);
                minXIndex = count;
            }
            if (Math.abs(p.x - f2) < minXDistance) {
                minXIndex = count;
                minXDistance = Math.abs(p.x - f2);
            }
            count++;
        }
        count = 0;
            for(Map.Entry<Integer, Float[]> entry: yMap.entrySet()) {
            Float f1 = entry.getValue()[0];
            Float f2 = entry.getValue()[1];
            if (f1 <= p.y && p.y <= f2) {
//                Log.d("Between:", f1 +":" + f2);
                minYIndex = count;
                return new int[] {minXIndex, minYIndex};
            }
//            if (Math.abs(p.y - f1) < minYDistance) {
//                minYDistance = Math.abs(p.y - f1);
//                minYIndex = count;
//            }
//            if (Math.abs(p.y - f2) < minYDistance) {
//                minYIndex = count;
//                minYDistance = Math.abs(p.y - f2);
//            }
            count++;
        }
        return new int[] {minXIndex, minYIndex};
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xMap = MainActivity.map;
        yMap = MainActivity.fretMap;
        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                // We have a new pointer. Lets add it to the list of pointers

                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);
                mActivePointers.put(pointerId, f);
                myLock.lock();
                resetColor();
                myLock.unlock();
                break;
            }
            case MotionEvent.ACTION_MOVE: { // a pointer was moved
                for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                    PointF point = mActivePointers.get(event.getPointerId(i));
                    if (point != null) {
                        point.x = event.getX(i);
                        point.y = event.getY(i);
                    }
                    myLock.lock();
                    resetColor();
                    myLock.unlock();
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {

                mActivePointers.remove(pointerId);
                myLock.lock();
                resetColor();
                myLock.unlock();
                break;
            }
        }
        invalidate();

        return true;
    }


    // Calculate distance of point to all view's coordinates (set color appropriately)

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw all pointers
        myLock.lock();
        resetColor();
        myLock.unlock();
        for (int size = mActivePointers.size(), i = 0; i < size; i++) {
            PointF point = mActivePointers.valueAt(i);
            if (point != null) {
                int[] index = mapColor(point);
                mPaint.setColor(colors[index[0]]);
//                Log.d("Position:", "X:" + index[0] + " Y:" + index[1]);
                colorIndexMap.put(point, index);
            }
            canvas.drawCircle(point.x, point.y, SIZE, mPaint);
        }
        if (MainActivity.device != null) {
            canvas.drawText("DEVICE: " + MainActivity.drivers.get(0).getPorts().toString(), 10, 40, textPaint);
        } else {
            canvas.drawText("DEVICE: " + "[]", 10, 40, textPaint);
        }
//        canvas.drawText("Total pointers: " + mActivePointers.size(), 10, 40 , textPaint);
    }

    public synchronized void resetColor() {
        colorIndexMap = new ConcurrentHashMap<>();
        for (int size = mActivePointers.size(), i = 0; i < size; i++) {
            PointF point = mActivePointers.valueAt(i);
            if (point != null) {
                int[] index = mapColor(point);
                mPaint.setColor(colors[index[0]]);
                colorIndexMap.put(point, index);
            }
        }
    }

}