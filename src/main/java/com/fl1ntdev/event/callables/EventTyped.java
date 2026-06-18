package com.fl1ntdev.event.callables;


import com.fl1ntdev.event.Event;

public abstract class EventTyped implements Event, Typed {

    private final byte type;

    protected EventTyped(byte eventType) {
        type = eventType;
    }

    @Override
    public byte getType() {
        return type;
    }

}