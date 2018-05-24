package com.example.kinnplh.uiautomationserver;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import static android.accessibilityservice.AccessibilityService.SHOW_MODE_HIDDEN;
import static android.content.Context.WINDOW_SERVICE;
import static java.lang.Math.abs;

/**
 * Created by kinnplh on 2018/5/14.
 */

public class ServerThread extends Thread {
    final int SERVER_PORT = 10086;

    ServerSocket serverSocket;
    Socket socket;
    BufferedReader reader;
    PrintStream writer;
    boolean threadRunning;
    AccessibilityService service;
    WindowManager manager;
    ServerThread(AccessibilityService service){
        this.service = service;
        manager = (WindowManager) service.getSystemService(WINDOW_SERVICE);
    }
    @Override
    public void run() {
        threadRunning = true;
        while(threadRunning) {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                Log.i("SocketInfo", "Listening...");
                socket = serverSocket.accept();
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintStream(socket.getOutputStream());
                Log.i("SocketInfo", "Accepted");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (socket == null || reader == null)
                continue;
            while (threadRunning) {
                try {
                    String line = reader.readLine();
                    //  Utility.speak(line);
                    if(line == null){
                        serverSocket.close();
                        socket.close();
                        break;
                    }
                    Log.i("cmd", line);
                    String[] line_split = line.split("#");
                    switch (line_split[0]) {
                        case "ACTION-SCROLL":
                            handleScroll(line_split);
                            break;
                        case "ACTION-CLICK":
                            handleClick(line_split);
                            break;
                        case "DUMP_LAYOUT":
                            handleDumpLayout();
                            break;
                        case "INFO-QUERY":
                            handleQuery(line_split);
                            break;
                        case "ACTION-GLOBAL":
                            handleGlobal(line_split);
                            break;
                        default:
                            Log.e("CMD ERROR", String.format("Unknown cmd %s", line));
                            writer.print("UNKNOWN-CMD\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopThread(){
        threadRunning = false;
    }

    private void handleScroll(String[] line_split){
        if(line_split.length != 3){
            writer.print("RES-SCROLL#ErrorFormat\n");
            return;
        }
        String node_id = line_split[1];
        int step = Integer.valueOf(line_split[2]);
        AccessibilityNodeInfo nodeInfo = Utility.getNodeByNodeId(service.getRootInActiveWindow(), node_id);
        if(nodeInfo == null){
            writer.print("RES-SCROLL#NotFound\n");
        } else if(step == 0){
            writer.print("RES-SCROLL#Success\n");
        } else {
            int action = step > 0? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
            step = abs(step);
            for(; step > 0; -- step){
                boolean res = nodeInfo.performAction(action);
                if(!res){
                    writer.print("RES-SCROLL#Failed\n");
                    return;
                }
            }
            writer.print("RES-SCROLL#Success\n");
        }
    }

    private void handleDumpLayout(){
        service.getSoftKeyboardController().setShowMode(SHOW_MODE_HIDDEN);
        AccessibilityNodeInfo root = service.getRootInActiveWindow();
        if(root == null){
            writer.print("RES-DUMP_LAYOUT#Failed\n");
            return;
        }

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("RES-DUMP_LAYOUT#Success#");
        Utility.generateLayoutXML(root, 0, xmlBuilder);
        xmlBuilder.append("\n");
        writer.print(xmlBuilder);
    }

    private void handleQuery(String[] line_split){
        if(line_split.length != 2){
            writer.print("RES-QUERY#ErrorFormat\n");
            return;
        }
        String nodeId = line_split[1];
        AccessibilityNodeInfo node = Utility.getNodeByNodeId(service.getRootInActiveWindow(), nodeId);
        if(node == null){
            writer.print("RES-QUERY#NotFound\n");
        } else {
            writer.print("RES-QUERY#Success\n");
        }
    }

    private void handleClick(String[] line_split){
        if(line_split.length != 2){
            writer.print("RES-CLICK#ErrorFormat\n");
            return;
        }
        String nodeId = line_split[1];
        AccessibilityNodeInfo nodeInfo = Utility.getNodeByNodeId(service.getRootInActiveWindow(), nodeId);
        if(nodeInfo == null){
            writer.print("RES-CLICK#NotFound\n");
        } else {
            boolean clickRes = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if(clickRes){
                writer.print("RES-CLICK#Success\n");
            } else {
                writer.print("RES-CLICK#Failed\n");
            }
        }
    }

    private void handleGlobal(String[] line_split){
        if(line_split.length != 2){
            writer.print("RES-GLOBAL#ErrorFormat\n");
            return;
        }
        String cmd = line_split[1];
        switch (cmd){
            case "GlobalBack":
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                writer.print("RES-GLOBAL#SUCCESS\n");
                break;
            default:
                writer.print("RES-GLOBAL#ErrorFormat\n");
                break;
        }
    }

}
