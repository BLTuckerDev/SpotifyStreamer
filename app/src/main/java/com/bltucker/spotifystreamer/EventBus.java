package com.bltucker.spotifystreamer;

import com.squareup.otto.Bus;

public final class EventBus {

    private static final EventBus eventBus = new EventBus();

    public static EventBus getEventBus(){
        return eventBus;
    }

    private Bus ottoBus = new Bus();

    private EventBus(){

    }


    public void register(Object object){
        ottoBus.register(object);
    }

    public void unregister(Object object){
        ottoBus.unregister(object);
    }


    public void fireEvent(Object event){
        ottoBus.post(event);
    }

}
