package com.dennis.samplemaps;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.dennis.samplemaps.models.Leg;
import com.dennis.samplemaps.models.Step;
import com.dennis.samplemaps.utils.DataParser;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.List;

public class PointsParser extends AsyncTask<String, Integer, List<Leg>> {

    TaskLoadedCallback taskCallback;
    String directionMode = "driving";

    public PointsParser(Context mContext, String directionMode) {
        this.taskCallback = (TaskLoadedCallback) mContext;
        this.directionMode = directionMode;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<Leg> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<Leg> legs = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            DataParser parser = new DataParser();
            legs = parser.parse(jObject);

        } catch (Exception e) {
            Log.d("mylog", e.toString());
            e.printStackTrace();
        }
        return legs;
    }

    // Executes in UI thread after the parsing process
    @Override
    protected void onPostExecute(List<Leg> result) {
        PolylineOptions lineOptions = new PolylineOptions();

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {

            // Adding all the points in the route to LineOptions
            for (Leg leg : result) {
                for (Step step : leg.steps) {
                    lineOptions.addAll(step.stepPolyline);
                }
            }
            if (directionMode.equalsIgnoreCase("walking")) {
                lineOptions.width(10);
                lineOptions.color(Color.MAGENTA);
            } else {
                lineOptions.width(20);
                lineOptions.color(Color.RED);
            }

        }

        // Drawing polyline in the Google Map for the i-th route
        taskCallback.onTaskDone(lineOptions, result);
    }
}
