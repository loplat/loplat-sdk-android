package com.example.sample_kotlin

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.loplat.placeengine.PlengiListener
import com.loplat.placeengine.PlengiResponse
import com.loplat.placeengine.cloud.ResponseMessage

class LoplatPlengiListener : PlengiListener {
    var mContext: Context = LoplatSampleApplication.context!!
    override fun listen(response: PlengiResponse) {
        System.out.println("LoplatPlengiListener: " + response.type)
        val echo_code: String = response.echo_code // init시 전달된 echo code

        // handle cloud access error
        if (response.result === PlengiResponse.Result.SUCCESS) {

            // get location information from loplat server (refreshPlace())
            if (response.type === PlengiResponse.ResponseType.PLACE_EVENT
                || response.type === PlengiResponse.ResponseType.PLACE_TRACKING
            ) {
                var description = """
                type: ${response.type.toString()}
                
                """.trimIndent()

                // get events (place enter or place leave)
                val event: Int = response.placeEvent
                if (response.place != null) {
                    val branch = if (response.place.tags == null) "" else response.place.tags
                    val clientCode: String? =
                        if (response.place.client_code == null || !response.place.client_code.isEmpty()) null else response.place.client_code
                    description += if (event == PlengiResponse.PlaceEvent.ENTER) {
                        //Plengi.getInstance(null).startNearbySession();
                        ("   [ENTER]" + response.place.name.toString() + "," + branch + "(" + response.place.loplatid.toString() + "), "
                                + response.place.floor.toString() + "F, " + java.lang.String.format(
                            "%.3f",
                            response.place.accuracy
                        )
                            .toString() + "/" + java.lang.String.format(
                            "%.3f",
                            response.place.threshold
                        ))
                    } else if (event == PlengiResponse.PlaceEvent.LEAVE) {
                        //Plengi.getInstance(null).stopNearbySession();
                        "   [LEAVE]" + response.place.name.toString() + "," + branch + "(" + response.place.loplatid.toString() + ")"
                    } else if (event == PlengiResponse.PlaceEvent.NEARBY) {
                        ("   [NEARBY]" + response.place.name.toString() + "," + branch + "(" + response.place.loplatid.toString() + "), "
                                + response.place.floor.toString() + "F, " + java.lang.String.format(
                            "%.3f",
                            response.place.accuracy
                        )
                            .toString() + "/" + java.lang.String.format(
                            "%.3f",
                            response.place.threshold
                        ))
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
                    description += ("[AREA] " + response.area.id.toString() + ", " + response.area.name.toString() + ","
                            + response.area.tag.toString() + "(" + response.area.lat.toString() + "," + response.area.lng.toString() + ")")
                }

                // 복합몰이 인식 되었을 때
                if (response.complex != null) {
                    description += "\n   "
                    description += ("[COMPLEX] " + response.complex.id.toString() + "]" + response.complex.name.toString() + ","
                            + response.complex.branch_name.toString() + "," + response.complex.category)
                }

                // GeoFence 정보
                if (response.geoFence != null) {
                    description += "\n   "
                    description += "[GEOFENCE] " + response.geoFence.getFences().size
                        .toString() + "개"
                    for (i in 0 until response.geoFence.getFences().size) {
                        val fence: ResponseMessage.Fence = response.geoFence.getFences().get(i)
                        description += """
      [${i + 1}] ${fence.getGfId()}, ${fence.getName()}, ${fence.getDist()}"""
                    }
                }

                // Device 위경도
                if (response.location != null) {
                    description += "\n   "
                    description += "[DEVICE] (" + response.location.lat.toString() + ", " + response.location.lng.toString() + ")"
                }

                // 행정구역
                if (response.district != null) {
                    description += "\n   "
                    description += "[DISTRICT] "

                    // 행정구역을 활용할 때에는 행정구역 자체의 위경도가 없기 떄문에 Device의 위경도를 사용.
                    if (response.location != null) {
                        description += "(" + response.location.lat.toString() + ", " + response.location.lng.toString() + ") "
                    }
                    description += "lv0=" + response.district.lv0Code
                        .toString() + ", lv1=" + response.district.lv1Code.toString() + "_" + response.district.lv1Name
                        .toString() + ", lv2=" + response.district.lv2Code.toString() + "_" + response.district.lv2Name
                        .toString() + ", lv3=" + response.district.lv3Code.toString() + "_" + response.district.lv3Name
                }
                if (response.advertisement != null) {
                    // loplat X 광고 정보가 있을 때
                    // loplat SDK 통한 광고 알림을 사용하지 않고
                    // Custom Notification 혹은 직접 이벤트 처리 할 경우 해당 객체를 사용
                }
                println(description)
                sendLoplatResponseToApplication("placeevent", description)
            }
        } else if (response.result === PlengiResponse.Result.FAIL && response.errorReason.equals(
                PlengiResponse.LOCATION_ACQUISITION_FAIL
            )
        ) {
            var description = "[" + response.errorReason.toString() + "]"
            // Device 위경도
            if (response.location != null) {
                description += "\n   "
                description += "[DEVICE] (" + response.location.lat.toString() + ", " + response.location.lng.toString() + ")"
            }
            sendLoplatResponseToApplication("placeevent", description)
        } else {
            val errorReason: String = response.errorReason
            sendLoplatResponseToApplication("error", errorReason)
        }
    }

    private fun sendLoplatResponseToApplication(type: String, response: String?) {
        val i = Intent()
        i.setAction("com.loplat.sample.response")
        i.putExtra("type", type)
        i.putExtra("response", response)
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i)
    }
}