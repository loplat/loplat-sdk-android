package com.loplat.loplatsample;

import android.content.Intent;

import com.loplat.placeengine.PlengiListener;
import com.loplat.placeengine.PlengiResponse;

import java.util.List;



public class LoplatPlengiListener implements PlengiListener {

    @Override
    public void listen(PlengiResponse response) {
        System.out.println("LoplatPlengiListener: " + response.type);

        // handle cloud access error
        if(response.result == PlengiResponse.Result.ERROR_CLOUD_ACCESS) {
            String errorReason = response.errorReason;

            sendLoplatResponseToApplication("error", errorReason);
            return;
        }

        // get location information from loplat server (refreshPlace())
        if(response.type == PlengiResponse.ResponseType.PLACE) {

            String name = response.place.name;  // detected place name
            String tags = response.place.tags;
            double lat = response.place.lat;    // detected place location (latitude)
            double lng = response.place.lng;    // detected place location (longitude)
            int floor = response.place.floor;   // detected place's floor info
            String client_code = response.place.client_code;    // client_code

            float accuracy = response.place.accuracy;
            float threshold = response.place.threshold;

            String placeinfo = name + ": " + tags + ", " + floor + ", " +
                                String.format("%.3f", accuracy) + "/" + String.format("%.3f", threshold);

            if(accuracy > threshold) {
                // device is within the detected place
                placeinfo += " (In)";
            }
            else {
                // device is outside the detected place
                placeinfo += " (Nearby)";

                // in case accuracy is 0.1, actual distance is 40~50M
            }

            if(client_code != null) {
                placeinfo += ", client_code: " + client_code;
            }

            sendLoplatResponseToApplication("placeinfo", placeinfo);
        }
        // get events (place enter or place leave)
        else if(response.type == PlengiResponse.ResponseType.PLACE_EVENT) {
            int event = response.placeEvent;

            String detail = "PLACE EVENT: ";
            detail += event;

            if(event == PlengiResponse.PlaceEvent.ENTER) {
                detail += " - " + response.place.name;
                detail += " (" + response.place.floor + "F)";
                detail += ", client_code: " + response.place.client_code;

                // start nearby session in case people enter to a place
                // Plengi.getInstance(null).startNearbySession();
            }
            else if(event == PlengiResponse.PlaceEvent.LEAVE) {

                // stop nearby session in case people leave from a place
                // Plengi.getInstance(null).stopNearbySession();
            }
            System.out.println(detail);
            sendLoplatResponseToApplication("placeevent", detail);
        }
        else if(response.type == PlengiResponse.ResponseType.NEARBY_DEVICE) {
            String colocateInfo = "";

            List<PlengiResponse.Person> persons = response.persons;
            for(PlengiResponse.Person person: persons) {
                colocateInfo += person.uniqueUserId + " ";
            }

            sendLoplatResponseToApplication("nearby", colocateInfo);
        }
    }


    private void sendLoplatResponseToApplication(String type, String response) {
        Intent i = new Intent();
        i.setAction("com.loplat.mode.response");
        i.putExtra("type", type);
        i.putExtra("response", response);
        LoplatSampleApplication.getContext().sendBroadcast(i);
    }
}
