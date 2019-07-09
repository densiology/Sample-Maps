package com.dennis.samplemaps.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Step {

    public HashMap<String, Double> startLocation = new HashMap<>();
    public HashMap<String, Double> endLocation = new HashMap<>();
    public String htmlInstructions = "";
    public List<LatLng> stepPolyline = new ArrayList<>();
}
