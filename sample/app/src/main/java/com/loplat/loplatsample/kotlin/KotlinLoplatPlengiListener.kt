package com.loplat.loplatsample.kotlin

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.loplat.placeengine.PlengiListener
import com.loplat.placeengine.PlengiResponse
import com.loplat.placeengine.PlengiResponse.PlaceEvent.*
import com.loplat.placeengine.PlengiResponse.ResponseType.PLACE_EVENT
import com.loplat.placeengine.PlengiResponse.ResponseType.PLACE_TRACKING

class KotlinLoplatPlengiListener : PlengiListener{
    private val context = KotlinLoplatSampleApplication.getContext()

    override fun listen(response: PlengiResponse) {
        System.out.println("LoplatPlengiListener: ${response.type}")

        // init시 전달된 echo code
        val echo_code = response.echo_code

        if (response.result == PlengiResponse.Result.SUCCESS) {
            if (response.type == PLACE_EVENT || response.type == PLACE_TRACKING) {
                var description = "type: ${response.type}\n"

                // 매장 방문 관련 event (NOT_AVAILABLE / ENTER / LEAVE / NEARBY)
                val event = response.placeEvent

                // 매장이 인식 되었을 때
                response.place?.let {
                    val branch = if (it.tags == null) "" else it.tags
                    val clientCode = if (it.client_code == null || !it.client_code.isEmpty()) {
                        null
                    } else {
                        it.client_code
                    }
                    val accuracy = String.format("%.3f", response.place.accuracy)
                    val threshold = String.format("%.3f", response.place.threshold)

                    when (event) {
                        ENTER -> description += "   [ENTER]${it.name},$branch(${it.loplatid}), ${it.floor}F, $accuracy/$threshold"
                        LEAVE -> description += "   [LEAVE]${it.name},$branch(${it.loplatid})"
                        NEARBY -> description += "   [NEARBY]${it.name},$branch(${it.loplatid}), ${it.floor}F, $accuracy/$threshold"
                        else -> description += "   [$event]${it.name},$branch(${it.loplatid}), ${it.floor}F"
                    }

                    clientCode?.let {
                        description += ", code: $clientCode"
                    }
                }

                // 상권이 인식 되었을 때
                response.area?.let {
                    description += "\n   [AREA] ${it.id}, ${it.name},${it.tag}(${it.lat},${it.lng})"
                }

                // 복합몰이 인식 되었을 때
                response.complex?.let {
                    description += "\n   [COMPLEX] ${it.id}]${it.name},${it.branch_name},${it.category}"
                }

                // GeoFence 정보
                response.geoFence?.let {
                    description += "\n   [GEOFENCE] ${it.getFences().size}개"
                    var index = 0
                    it.getFences().forEach { fence ->
                        description += "\n   [${index + 1}] ${fence.getGfId()}, ${fence.getName()}, ${fence.getDist()}"
                        index++
                    }
                }

                // Device 위경도
                response.location?.let {
                    description += "\n   [DEVICE] (${it.lat}, ${it.lng})"
                }

                // 행정구역
                response.district?.let {
                    description += "\n   [DISTRICT] "

                    // 행정구역을 활용할 때에는 행정구역 자체의 위경도가 없기 떄문에 Device의 위경도를 사용.
                    response.location?.let { location ->
                        description += "(${location.lat}, ${location.lng}) "
                    }

                    description += "lv0=${it.lv0Code}" +
                            ", lv1=${it.lv1Code}_${it.lv1Name}" +
                            ", lv2=${it.lv2Code}_${it.lv2Name}" +
                            ", lv3=${it.lv3Code}_${it.lv3Name}"

                }

                response.advertisement?.let {
                    // loplat X 광고 정보가 있을 때
                    // loplat SDK 통한 광고 알림을 사용하지 않고
                    // Custom Notification 혹은 직접 이벤트 처리 할 경우 해당 객체를 사용
                }

            }
        } else if (response.result == PlengiResponse.Result.FAIL
                && response.errorReason.equals(PlengiResponse.LOCATION_ACQUISITION_FAIL)) {
            var description = "[${response.errorReason}]"

            // Device 위경도
            response.location?.let {
                description += "\n[DEVICE] (${it.lat}, ${it.lng})"
            }

            sendLoplatResponseToApplication("placeevent", description)
        } else {
            sendLoplatResponseToApplication("error", response.errorReason)
        }
    }

    /**
     * 위치 인식 결과 전달
     */
    private fun sendLoplatResponseToApplication(type: String, response: String) {
        val i = Intent()
        i.action = "com.loplat.sample.response"
        i.putExtra("type", type)
        i.putExtra("response", response)
        LocalBroadcastManager.getInstance(context).sendBroadcast(i)
    }
}