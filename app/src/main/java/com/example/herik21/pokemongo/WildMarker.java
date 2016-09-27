package com.example.herik21.pokemongo;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Herik21 on 16/09/2016.
 */
public class WildMarker {
    public int pkid;
    public double[] loc;
    public boolean visible;
    public Marker mk;

    public WildMarker(int id,double[] markerloc,boolean visible){
        this.pkid=id;
        this.loc=markerloc;
        this.visible=visible;
    }
    public void setMarker(Marker marker){
        this.mk=marker;
    }
}
