package com.example.kinnplh.uiautomationserver;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Rect;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Locale;

/**
 * Created by kinnplh on 2018/5/14.
 */

public class Utility {
    public static TextToSpeech tts;
    static boolean ttsPrepared;
    static AccessibilityService service;
    public static void init(AccessibilityService s){
        Utility.service = s;
        if(tts == null){
            tts = new TextToSpeech(s, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    ttsPrepared = (i == TextToSpeech.SUCCESS);
                    if(!ttsPrepared){
                        tts = null;
                    } else {
                        tts.setLanguage(Locale.CHINESE);
                    }
                }
            });
        }
    }

    public static void shutdownTts(){
        if(tts != null){
            tts.shutdown();
            tts = null;
        }
    }

    public static void speak(String text){
        if(!ttsPrepared){
            Log.e("error", "speak: No tts available");
        } else {
            Log.i("info", "speak: " + text);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "text to speak: " + text);
        }
    }

    public static AccessibilityNodeInfo getNodeByNodeId(AccessibilityNodeInfo startNode, String relativeNodeId){
        if(startNode == null){
            return null;
        }
        int indexEnd = relativeNodeId.indexOf(';');
        if(indexEnd < 0){
            // 不存在分号，说明已经结束了
            if(startNode.getClassName().toString().equals(relativeNodeId)){
                return startNode;
            } else {
                return null;
            }
        }

        String focusPart = relativeNodeId.substring(0, indexEnd);
        int indexDivision = focusPart.indexOf('|');
        int childIndex = Integer.valueOf(focusPart.substring(indexDivision + 1));
        String crtPartClass = focusPart.substring(0, indexDivision);
        String remainPart = relativeNodeId.substring(indexEnd + 1);
        if(startNode.getClassName().toString().equals(crtPartClass) && childIndex >= 0 && childIndex < startNode.getChildCount()){
            return getNodeByNodeId(startNode.getChild(childIndex), remainPart);
        } else {
            return null;
        }
    }

    public static void generateLayoutXML(AccessibilityNodeInfo crtRoot, int indexInParent, StringBuilder builder){
        // 生成描述这个节点及其子节点的 xml 字符串
        builder.append("<node ");
        appendField("index", indexInParent, builder);
        appendField("text", crtRoot.getText(), builder);
        appendField("resource-id", crtRoot.getViewIdResourceName(), builder);
        appendField("class", crtRoot.getClassName(), builder);
        appendField("package", crtRoot.getPackageName(), builder);
        appendField("content-desc", crtRoot.getContentDescription(), builder);
        appendField("checkable", crtRoot.isCheckable(), builder);
        appendField("checked", crtRoot.isChecked(), builder);
        appendField("clickable", crtRoot.isClickable(), builder);
        appendField("enabled", crtRoot.isEnabled(), builder);
        appendField("focusable", crtRoot.isFocusable(), builder);
        appendField("focused", crtRoot.isFocused(), builder);
        appendField("scrollable", crtRoot.isScrollable(), builder);
        appendField("long-clickable", crtRoot.isLongClickable(), builder);
        appendField("password", crtRoot.isPassword(), builder);
        appendField("selected", crtRoot.isSelected(), builder);
        appendField("editable", crtRoot.isEditable(), builder);
        appendField("accessibilityFocused", crtRoot.isAccessibilityFocused(), builder);
        appendField("dismissable", crtRoot.isDismissable(), builder);

        Rect r = new Rect();
        crtRoot.getBoundsInScreen(r);
        builder.append("bounds=\"").append('[').append(r.left).append(',').append(r.top).append("][").append(r.right).append(',').append(r.bottom).append(']').append('"');
        if(crtRoot.getChildCount() == 0){
            builder.append("/>");
        } else {
            builder.append(">");
            for(int i = 0; i < crtRoot.getChildCount(); ++ i){
                generateLayoutXML(crtRoot.getChild(i), i, builder);
            }
            builder.append("</node>");
        }
    }

    static void appendField(String name, String value, StringBuilder builder){
        builder.append(name).append("=\"").append(value == null? "": value).append("\" ");
    }

    static void appendField(String name, int value, StringBuilder builder){
        builder.append(name).append("=\"").append(value).append("\" ");
    }

    static void appendField(String name, CharSequence value, StringBuilder builder){
        builder.append(name).append("=\"").append(value == null? "": value).append("\" ");
    }

    static void appendField(String name, boolean value, StringBuilder builder){
        builder.append(name).append("=\"").append(value? "true": "false").append("\" ");
    }
}
