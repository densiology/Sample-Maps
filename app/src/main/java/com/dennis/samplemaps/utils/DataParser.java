package com.dennis.samplemaps.utils;

import com.dennis.samplemaps.models.Leg;
import com.dennis.samplemaps.models.Step;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {
    public List<Leg> parse(JSONObject jObject) {

        List<Leg> legs = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        try {
            jRoutes = jObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    Leg leg = new Leg();

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");

                        /** Add data to steps list */
                        Step step = new Step();
                        step.stepPolyline = decodePoly(polyline);
                        step.htmlInstructions = (String)((JSONObject) jSteps.get(k)).get("html_instructions");
                        HashMap<String, Double> startLocation = new HashMap<>();
                        HashMap<String, Double> endLocation = new HashMap<>();
                        startLocation.put("lat", (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lat"));
                        startLocation.put("lng", (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lng"));
                        endLocation.put("lat", (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("end_location")).get("lat"));
                        endLocation.put("lng", (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("end_location")).get("lng"));
                        step.startLocation = startLocation;
                        step.endLocation = endLocation;
                        leg.steps.add(step);
                    }
                    legs.add(leg);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return legs;
    }

    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
