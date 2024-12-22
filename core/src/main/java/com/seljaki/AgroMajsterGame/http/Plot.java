package com.seljaki.AgroMajsterGame.http;

import com.seljaki.AgroMajsterGame.utils.Geolocation;

public class Plot {
    public int id;
    public String title;
    public String note;
    public String plotNumber;
    public int cadastralMunicipality;
    public boolean archived;
    public Geolocation[] coordinates;
}
