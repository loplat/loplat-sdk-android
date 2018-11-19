package com.loplat.loplatsample;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.loplat.placeengine.PlengiListener;
import com.loplat.placeengine.PlengiResponse;

import java.util.List;



public class LoplatPlengiListener implements PlengiListener {
    Context mContext = LoplatSampleApplication.getContext();

    @Override
    public void listen(PlengiResponse response) {
        System.out.println("LoplatPlengiListener: " + response.type);

        Intent i = new Intent();
        i.setPackage(mContext.getPackageName());
        // handle cloud access error
        if(response.result != PlengiResponse.Result.SUCCESS) {
            String errorReason = response.errorReason;

            sendLoplatResponseToApplication("error", errorReason);
            return;
        }

        String echo_code = response.echo_code;
        i.putExtra("echo_code", echo_code);
        // get location information from loplat server (refreshPlace())
        if(response.type == PlengiResponse.ResponseType.PLACE_EVENT
                || response.type == PlengiResponse.ResponseType.PLACE_TRACKING
                || response.type == PlengiResponse.ResponseType.PLACE_ADV_TRACKING) {
            // get events (place enter or place leave)

            int event = response.placeEvent;

            String description = response.type + ": ";
            if (response.place != null) {
                String branch = (response.place.tags == null) ? "" : response.place.tags;
                String clientCode = (response.place.client_code == null || !response.place.client_code.isEmpty())
                        ? null : response.place.client_code;
                if (event == PlengiResponse.PlaceEvent.ENTER) {
                    //Plengi.getInstance(null).startNearbySession();
                    description = "[ENTER]" + response.place.name + "," + branch + "(" + response.place.loplatid + "), "
                            + response.place.floor + "F, " + String.format("%.3f", response.place.accuracy)
                            + "/" + String.format("%.3f", response.place.threshold);
                } else if (event == PlengiResponse.PlaceEvent.LEAVE) {
                    //Plengi.getInstance(null).stopNearbySession();
                    description = "[LEAVE]" + response.place.name + "," + branch + "(" + response.place.loplatid + ")";
                } else if (event == PlengiResponse.PlaceEvent.NEARBY) {
                    description = "[NEARBY]" + response.place.name + "," + branch + "(" + response.place.loplatid + "), "
                            + response.place.floor + "F, " + String.format("%.3f", response.place.accuracy)
                            + "/" + String.format("%.3f", response.place.threshold);
                } else {
                    description = "[" + event + "]" + response.place.name + "," + branch + "(" + response.place.loplatid + "), "
                            + response.place.floor + "F";
                }

                if (clientCode != null) {
                    description += ", code: "+clientCode;
                }

            }

            if (response.area != null) {
                if (response.place != null) {
                    description += "\n   ";
                }
                description += "[" + response.area.id + "]" + response.area.name + ","
                        + response.area.tag + "(" + response.area.lat + "," + response.area.lng + ")";
            }
            if (response.complex != null) {
                if (response.place != null) {
                    description += "\n   ";
                }
                description += "[" + response.complex.id + "]" + response.complex.name + ","
                        + response.complex.branch_name + "," + response.complex.category;
            }

            System.out.println(description);
            sendLoplatResponseToApplication("placeevent", description);
        }
    }


    private void sendLoplatResponseToApplication(String type, String response) {
        Intent i = new Intent();
        i.setAction("com.loplat.sample.response");
        i.putExtra("type", type);
        i.putExtra("response", response);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }
}
