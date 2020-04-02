package com.tomtom.online.sdk.migration_ktx

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.map.*
import com.tomtom.online.sdk.routing.OnlineRoutingApi
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder
import com.tomtom.online.sdk.routing.data.RouteResponse
import com.tomtom.online.sdk.routing.data.RouteType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: TomtomMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (mapFragment as MapFragment).getAsyncMap(this)
        btnTrafficOn.setOnClickListener {
            map.trafficSettings.turnOnVectorTrafficIncidents()
            map.trafficSettings.turnOnVectorTrafficFlowTiles()
        }
        btnTrafficOff.setOnClickListener { map.trafficSettings.turnOffTraffic() }
        btnRouteShow.setOnClickListener {
            val amsterdam = LatLng(52.37, 4.90)
            val hague = LatLng(52.07, 4.30)
            val routingApi = OnlineRoutingApi.create(applicationContext)
            val routeQuery = RouteQueryBuilder(amsterdam, hague).withRouteType(RouteType.FASTEST).build()
            routingApi.planRoute(routeQuery)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableSingleObserver<RouteResponse?>() {
                        override fun onSuccess(routeResponse: RouteResponse) {
                            for (fullRoute in routeResponse.routes) {
                                val routeBuilder = RouteBuilder(
                                        fullRoute.coordinates)
                                map.addRoute(routeBuilder)
                            }
                        }

                        override fun onError(e: Throwable) {
                            Toast.makeText(this@MainActivity, e.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    })
        }
    }

    override fun onMapReady(tomtomMap: TomtomMap) {
        map = tomtomMap
        val amsterdam = LatLng(52.37, 4.90)
        val balloon = SimpleMarkerBalloon("Amsterdam")
        tomtomMap.addMarker(MarkerBuilder(amsterdam).markerBalloon(balloon))
        tomtomMap.centerOn(CameraPosition.builder().focusPosition(amsterdam).zoom(7.0).build())
    }
}
