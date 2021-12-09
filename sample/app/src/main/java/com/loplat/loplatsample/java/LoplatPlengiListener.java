package com.loplat.loplatsample.java;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.loplat.placeengine.PlengiListener;
import com.loplat.placeengine.PlengiResponse;
import com.loplat.placeengine.cloud.ResponseMessage;

public class LoplatPlengiListener implements PlengiListener {
    Context mContext = LoplatSampleApplication.getContext();

    @Override
    public void listen(PlengiResponse response) {
        System.out.println("LoplatPlengiListener: " + response.type);

        // init시 전달된 echo code
        String echo_code = response.echo_code;

        if (response.result == PlengiResponse.Result.SUCCESS) {
            String description = "type: " + response.type + "\n";

            // 매장 방문 관련 event (NOT_AVAILABLE / ENTER / LEAVE / NEARBY)
            int event = response.placeEvent;

            /*
             * Lite 요금제를 사용할 경우 실시간 위치기반 메시지 발송 기능 제공에 따라 Advertisement 정보만 제공됩니다.
             */
            if (response.advertisement != null) {
                // loplat X 광고 정보가 있을 때
                // loplat SDK 통한 광고 알림을 사용하지 않고
                // Custom Notification 혹은 직접 이벤트 처리 할 경우 해당 객체를 사용
            }

            // 장소(매장, 섹션)가 인식 되었을 때
            if (response.place != null) {
                String branch = (response.place.tags == null) ? "" : response.place.tags;
                String clientCode = (response.place.client_code == null || !response.place.client_code.isEmpty())
                        ? null : response.place.client_code;

                if (event == PlengiResponse.PlaceEvent.ENTER) {
                    description += "   [ENTER]" + response.place.name + "," + branch + "(" + response.place.loplatid + "), "
                            + response.place.floor + "F, " + String.format("%.3f", response.place.accuracy)
                            + "/" + String.format("%.3f", response.place.threshold);
                } else if (event == PlengiResponse.PlaceEvent.LEAVE) {
                    description += "   [LEAVE]" + response.place.name + "," + branch + "(" + response.place.loplatid + ")";
                } else if (event == PlengiResponse.PlaceEvent.NEARBY) {
                    description += "   [NEARBY]" + response.place.name + "," + branch + "(" + response.place.loplatid + "), "
                            + response.place.floor + "F, " + String.format("%.3f", response.place.accuracy)
                            + "/" + String.format("%.3f", response.place.threshold);
                } else {
                    description += "   [" + event + "]" + response.place.name + "," + branch + "(" + response.place.loplatid + "), "
                            + response.place.floor + "F";
                }

                if (clientCode != null) {
                    description += ", code: "+clientCode;
                }
            }

            // 상권이 인식 되었을 때
            if (response.area != null) {
                description += "\n   ";
                description += "[AREA] " + response.area.id + ", " + response.area.name + ","
                        + response.area.tag + "(" + response.area.lat + "," + response.area.lng + ")";
            }

            // 복합몰이 인식 되었을 때
            if (response.complex != null) {
                description += "\n   ";
                description += "[COMPLEX] " + response.complex.id + "]" + response.complex.name + ","
                        + response.complex.branch_name + "," + response.complex.category;
            }

            // GeoFence 정보
            if (response.geoFence != null) {
                description += "\n   ";
                description += "[GEOFENCE] " + response.geoFence.getFences().size() + "개";
                for (int i=0 ; i<response.geoFence.getFences().size() ; i++) {
                    ResponseMessage.Fence fence = response.geoFence.getFences().get(i);
                    description += "\n      ["+(i+1)+"] " + fence.getGfId() + ", " + fence.getName() + ", " + fence.getDist();
                }
            }

            // Device 위경도
            if (response.location != null) {
                description += "\n   ";
                description += "[DEVICE] (" + response.location.lat + ", " + response.location.lng + ")";
            }

            // 행정구역
            if (response.district != null) {
                description += "\n   ";
                description += "[DISTRICT] ";

                // 행정구역을 활용할 때에는 행정구역 자체의 위경도가 없기 떄문에 Device의 위경도를 사용.
                if (response.location != null) {
                    description += "(" + response.location.lat + ", " + response.location.lng + ") ";
                }

                description += "lv0=" + response.district.lv0Code
                        + ", lv1=" + response.district.lv1Code + "_" + response.district.lv1Name
                        + ", lv2=" + response.district.lv2Code + "_" + response.district.lv2Name
                        + ", lv3=" + response.district.lv3Code + "_" + response.district.lv3Name;
            }

            System.out.println(description);
            sendLoplatResponseToApplication("placeevent", description);
        } else if (response.result == PlengiResponse.Result.FAIL && response.errorReason.equals(PlengiResponse.LOCATION_ACQUISITION_FAIL)) {
            String description = "[" + response.errorReason + "]";
            // Device 위경도
            if (response.location != null) {
                description += "\n   ";
                description += "[DEVICE] (" + response.location.lat + ", " + response.location.lng + ")";
            }

            sendLoplatResponseToApplication("placeevent", description);
        } else {
            String errorReason = response.errorReason;

            sendLoplatResponseToApplication("error", errorReason);
        }
    }

    /**
     * 위치 인식 결과 전달
     */
    private void sendLoplatResponseToApplication(String type, String response) {
        Intent i = new Intent();
        i.setAction("com.loplat.sample.response");
        i.putExtra("type", type);
        i.putExtra("response", response);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }
}
