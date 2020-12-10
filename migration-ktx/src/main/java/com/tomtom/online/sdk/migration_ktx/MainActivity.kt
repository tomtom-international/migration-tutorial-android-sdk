package com.tomtom.online.sdk.migration_ktx

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.map.*
import com.tomtom.online.sdk.routing.OnlineRoutingApi
import com.tomtom.online.sdk.routing.RoutingException
import com.tomtom.online.sdk.routing.route.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: TomtomMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (mapFragment as MapFragment).getAsyncMap(this)
    }

    override fun onMapReady(tomtomMap: TomtomMap) {
        map = tomtomMap
        btnTrafficOn.setOnClickListener {
            map.trafficSettings.turnOnTrafficIncidents()
            map.trafficSettings.turnOnTrafficFlowTiles()
        }
        btnTrafficOff.setOnClickListener {
            map.trafficSettings.turnOffTrafficIncidents()
            map.trafficSettings.turnOffTrafficFlowTiles()
        }
        btnRouteShow.setOnClickListener(showRouteListener)

        val amsterdam = LatLng(52.37, 4.90)
        val balloon = SimpleMarkerBalloon("Amsterdam")
        tomtomMap.addMarker(MarkerBuilder(amsterdam).markerBalloon(balloon))
        tomtomMap.centerOn(CameraPosition.builder().focusPosition(amsterdam).zoom(7.0).build())
    }

    private val showRouteListener = View.OnClickListener {
        val amsterdam = LatLng(52.37, 4.90)
        val hague = LatLng(52.07, 4.30)
        val routingApi = OnlineRoutingApi.create(applicationContext, BuildConfig.ROUTING_API_KEY)
        val routeDescriptor = RouteDescriptor.Builder()
                .routeType(com.tomtom.online.sdk.routing.route.description.RouteType.FASTEST)
                .build()
        val routeCalculationDescriptor = RouteCalculationDescriptor.Builder()
                .routeDescription(routeDescriptor).build()
        val routeSpecification = RouteSpecification.Builder(amsterdam, hague)
                .routeCalculationDescriptor(routeCalculationDescriptor)
                .build()
        routingApi.planRoute(routeSpecification, object : RouteCallback {
            override fun onSuccess(routePlan: RoutePlan) {
                for (fullRoute in routePlan.routes) {
                    val routeBuilder = RouteBuilder(
                            fullRoute.getCoordinates())
                    map.addRoute(routeBuilder)
                }
            }

            override fun onError(error: RoutingException) {
                Toast.makeText(this@MainActivity, error.localizedMessage, Toast.LENGTH_LONG).show()
            }
        })
    }
}
