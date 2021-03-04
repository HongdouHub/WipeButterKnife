package com.chivas.wipe.utils;

import com.intellij.openapi.ui.Messages;

public class Log {

    private Log() {
        //
    }

    public static void d(String message) {
        Messages.showMessageDialog(message, "Debug", Messages.getInformationIcon());
    }

}
