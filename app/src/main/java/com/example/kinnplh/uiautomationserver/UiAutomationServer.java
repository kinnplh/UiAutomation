package com.example.kinnplh.uiautomationserver;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by kinnplh on 2018/5/14.
 */

public class UiAutomationServer extends AccessibilityService {

    ServerThread thread;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if(thread == null) {
            Utility.init(this);
            thread = new ServerThread(this);
            thread.start();
        }
    }

   /* @Override
    public void onCreate() {
        super.onCreate();
        if(thread == null) {
            Utility.init(this);
            thread = new ServerThread(this);
            thread.start();
        }
    }
*/
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //Log.i("NewEvent", accessibilityEvent.toString());
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.shutdownTts();
        thread.stopThread();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
