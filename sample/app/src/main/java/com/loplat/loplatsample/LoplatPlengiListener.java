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
            detail += event + " - " + response.place.name;

            if(event == PlengiResponse.PlaceEvent.ENTER) {
                detail += " (IN)";

            } else if (event == PlengiResponse.PlaceEvent.NEARBY) {
                detail += " (Nearby)";

            } else if (event == PlengiResponse.PlaceEvent.LEAVE) {
                // (가장 최근에 인식된 장소 기준) 인식 결과가 IN(Enter)인 장소를 벗어났을 때 leave event 발생
            }

            detail += " (" + response.place.floor + "F)";
            detail += ", client_code: " + response.place.client_code;
            System.out.println(detail);
            sendLoplatResponseToApplication("placeevent", detail);
        }
        else if(response.type == PlengiResponse.ResponseType.PLACE_TRACKING) {
            String detail = "PLACE EVENT: ";

            if(response.place == null) {
                // 특정장소 트래킹하다 벗어난 경우 한번 전달
            }
            else {
                // tracking 결과 값 주기적으로 발생
                // response.place.name
                // response.place.loplatid
                detail += " - " + response.place.name;
                detail += " (" + response.place.floor + "F)";
                detail += ", client_code: " + response.place.client_code;
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
