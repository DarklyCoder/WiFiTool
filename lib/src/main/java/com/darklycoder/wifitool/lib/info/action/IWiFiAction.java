package com.darklycoder.wifitool.lib.info.action;

import android.support.annotation.NonNull;

import com.darklycoder.wifitool.lib.type.Types;

public abstract class IWiFiAction {

    @Types.ActionStateType
    private int actionState;

    IWiFiAction() {
        setState(Types.ActionStateType.WAITING);
    }

    private String getActionName() {
        return getClass().getSimpleName();
    }

    public int getActionState() {
        return actionState;
    }

    public void setState(@Types.ActionStateType int state) {
        this.actionState = state;
    }

    public void end() {
        this.actionState = Types.ActionStateType.END;
    }

    @NonNull
    @Override
    public String toString() {
        return getActionName() + " | actionState:" + actionState;
    }

}
