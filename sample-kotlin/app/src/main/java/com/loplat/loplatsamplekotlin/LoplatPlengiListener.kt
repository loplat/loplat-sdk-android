package com.loplat.loplatsamplekotlin

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.loplat.placeengine.PlengiListener
import com.loplat.placeengine.PlengiResponse

class LoplatPlengiListener : PlengiListener {
    var mContext: Context? = LoplatSampleApplication.getContext()
    override fun listen(response: PlengiResponse) {
        println("LoplatPlengiListener: " + response.type)
        val echo_code = response.echo_code // init시 전달된 echo code

        // handle cloud access error
        if (response.result == PlengiResponse.Result.SUCCESS) {

            // get location information from loplat server (refreshPlace())
            if (response.type == PlengiResponse.ResponseType.PLACE_EVENT
                    || response.type == PlengiResponse.ResponseType.PLACE_TRACKING) {
                var description = """
                type: ${response.type}
                
                """.trimIndent()

                // get events (place enter or place leave)
                val event = response.placeEvent
                if (response.place != null) {
                    val branch = if (response.place.tags == null) "" else response.place.tags
                    val clientCode = if (response.place.client_code == null || !response.place.client_code.isEmpty()) null else response.place.client_code
                    description += if (event == PlengiResponse.PlaceEvent.ENTER) {
                        //Plengi.getInstance(null).startNearbySession();
                        ("   [ENTER]" + response.place.name + "," + branch + "(" + response.place.loplatid + "), "
                                + response.place.floor + "F, " + String.format("%.3f", response.place.accuracy) + "/" + String.format("%.3f", response.place.threshold))
                    } else if (event == PlengiResponse.PlaceEvent.LEAVE) {
                        //Plengi.getInstance(null).stopNearbySession();
                        "   [LEAVE]" + response.place.name + "," + branch + "(" + response.place.loplatid + ")"
                    } else if (event == PlengiResponse.PlaceEvent.NEARBY) {
                        ("   [NEARBY]" + response.place.name + "," + branch + "(" + response.place.loplatid + "), "
                                + response.place.floor + "F, " + String.format("%.3f", response.place.accuracy) + "/" + String.format("%.3f", response.place.threshold))
                    } else {
                        ("   [" + event + "]" + response.place.name + "," + branch + "(" + response.place.loplatid + "), "
                                + response.place.floor + "F")
                    }
                    if (clientCode != null) {
                        description += ", code: $clientCode"
                    }
                }

                // 상권이 인식 되었을 때
                if (response.area != null) {
                    description += "\n   "
                    description += ("[AREA] " + response.area.id + ", " + response.area.name + ","
                            + response.area.tag + "(" + response.area.lat + "," + response.area.lng + ")")
                }

                // 복합몰이 인식 되었을 때
                if (response.complex != null) {
                    description += "\n   "
                    description += ("[COMPLEX] " + response.complex.id + "]" + response.complex.name + ","
                            + response.complex.branch_name + "," + response.complex.category)
                }

                // GeoFence 정보
                if (response.geoFence != null) {
                    description += "\n   "
                    description += "[GEOFENCE] " + response.geoFence.getFences().size + "개"
                    for (i in response.geoFence.getFences().indices) {
                        val fence = response.geoFence.getFences()[i]
                        description += """
      [${i + 1}] ${fence.getGfId()}, ${fence.getName()}, ${fence.getDist()}"""
                    }
                }

                // Device 위경도
                if (response.location != null) {
                    description += "\n   "
                    description += "[DEVICE] (" + response.location.lat + ", " + response.location.lng + ")"
                }

                // 행정구역
                if (response.district != null) {
                    description += "\n   "
                    description += "[DISTRICT] "

                    // 행정구역을 활용할 때에는 행정구역 자체의 위경도가 없기 떄문에 Device의 위경도를 사용.
                    if (response.location != null) {
                        description += "(" + response.location.lat + ", " + response.location.lng + ") "
                    }
                    description += ("lv0=" + response.district.lv0Code
                            + ", lv1=" + response.district.lv1Code + "_" + response.district.lv1Name
                            + ", lv2=" + response.district.lv2Code + "_" + response.district.lv2Name
                            + ", lv3=" + response.district.lv3Code + "_" + response.district.lv3Name)
                }
                if (response.advertisement != null) {
                    // loplat X 광고 정보가 있을 때
                    // loplat SDK 통한 광고 알림을 사용하지 않고
                    // Custom Notification 혹은 직접 이벤트 처리 할 경우 해당 객체를 사용
                }
                println(description)
                sendLoplatResponseToApplication("placeevent", description)
            }
        } else if (response.result == PlengiResponse.Result.FAIL && response.errorReason == PlengiResponse.LOCATION_ACQUISITION_FAIL) {
            var description = "[" + response.errorReason + "]"
            // Device 위경도
            if (response.location != null) {
                description += "\n   "
                description += "[DEVICE] (" + response.location.lat + ", " + response.location.lng + ")"
            }
            sendLoplatResponseToApplication("placeevent", description)
        } else {
            val errorReason = response.errorReason
            sendLoplatResponseToApplication("error", errorReason)
        }
    }

    private fun sendLoplatResponseToApplication(type: String, response: String?) {
        val i = Intent()
        i.action = "com.loplat.sample.response"
        i.putExtra("type", type)
        i.putExtra("response", response)
        mContext?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(i) }

    }
}