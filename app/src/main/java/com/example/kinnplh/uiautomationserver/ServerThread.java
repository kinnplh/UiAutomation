package com.example.kinnplh.uiautomationserver;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

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
    ServerThread(AccessibilityService service){
        this.service = service;
    }
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            Log.i("SocketInfo", "Listening...");
            socket = serverSocket.accept();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintStream(socket.getOutputStream());
            Log.i("SocketInfo", "Accepted");
        } catch (IOException e){
            e.printStackTrace();
        }

        if(socket == null || reader == null)
            return;
        threadRunning = true;
        while (threadRunning){
            try {
                String line = reader.readLine();
                Utility.speak(line);
                Log.i("cmd", line);
                String[] line_split = line.split("#");
                switch (line_split[0]){
                    case "ACTION-SCROLL":
                        handleScroll(line_split);
                        break;
                    case "DUMP_LAYOUT":
                        handleDumpLayout();
                        break;
                    default:
                        Log.e("CMD ERROR", String.format("Unknown cmd %s", line));
                        writer.print("UNKNOWN-CMD\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
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
}
