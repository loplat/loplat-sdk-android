package com.loplat.loplatsample;

import android.content.Context;
import android.content.Intent;

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
        if(response.result == PlengiResponse.Result.ERROR_CLOUD_ACCESS) {
            String errorReason = response.errorReason;

            sendLoplatResponseToApplication("error", errorReason);
            return;
        }

        // get location information from loplat server (refreshPlace())
        if(response.type == PlengiResponse.ResponseType.PLACE) {
            String description = "";
            if (response.place != null) {
                String name = response.place.name;  // detected place name
                String branch = (response.place.tags == null) ? "": response.place.tags;
                int floor = response.place.floor;   // detected place's floor info
                String client_code = response.place.client_code;    // client_code

                float accuracy = response.place.accuracy;
                float threshold = response.place.threshold;

                description = "[PLACE]"+ name + ": " + branch + ", " + floor + ", " +
                        String.format("%.3f", accuracy) + "/" + String.format("%.3f", threshold);

                if(accuracy > threshold) {
                    // device is within the detected place
                    description += " (In)";
                } else {
                    // device is outside the detected place
                    description += " (Nearby)";
                }

                if(client_code != null && !client_code.isEmpty()) {
                    description += ", client_code: " + client_code;
                }
            }

            if (response.area != null) {
                if (response.place != null) {
                    description += "\n    ";
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
            sendLoplatResponseToApplication("placeinfo", description);
        } else if(response.type == PlengiResponse.ResponseType.PLACE_EVENT
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
        } else if(response.type == PlengiResponse.ResponseType.NEARBY_DEVICE) {
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
        i.setPackage(LoplatSampleApplication.getContext().getPackageName());
        i.setAction("com.loplat.sample.response");
        i.putExtra("type", type);
        i.putExtra("response", response);
        LoplatSampleApplication.getContext().sendBroadcast(i);
    }
}
