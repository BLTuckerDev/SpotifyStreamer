package com.bltucker.spotifystreamer.playback;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

public final class PlaybackServiceConnection implements ServiceConnection {

    private PlaybackService boundService;

    private boolean isBound;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.boundService = ((PlaybackService.PlaybackServiceBinder) service).getService();
        this.isBound = true;
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.boundService = null;
        this.isBound = false;
    }


    public void unbind(Context context){
        if(isBound){
            context.unbindService(this);
        }
    }

    public boolean isActive(){
        return this.boundService != null;
    }


    public PlaybackService getBoundService(){
        return this.boundService;
    }

}
