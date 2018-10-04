package com.tomtom.online.sdk.migration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.CameraPosition;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteType;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TomtomMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getAsyncMap(this);

        Button btnTrafficOn = findViewById(R.id.btnTrafficOn);
        btnTrafficOn.setOnClickListener(v -> {
            map.getUiSettings().turnOnVectorTrafficFlowTiles();
            map.getUiSettings().turnOnRasterTrafficIncidents();
        });

        Button btnRouteShow = findViewById(R.id.btnRouteShow);
        btnRouteShow.setOnClickListener(v -> {

            LatLng hague = new LatLng(52.07, 4.30);
            LatLng amsterdam = new LatLng(52.37, 4.90);

            RoutingApi routingApi = OnlineRoutingApi.create(getApplicationContext());
            RouteQuery routeQuery = new RouteQueryBuilder(amsterdam, hague).withRouteType(RouteType.FASTEST).build();

            routingApi.planRoute(routeQuery)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(routeResult -> {
                        for (FullRoute fullRoute : routeResult.getRoutes()) {
                            RouteBuilder routeBuilder = new RouteBuilder(
                                    fullRoute.getCoordinates()).isActive(true);
                            map.addRoute(routeBuilder);
                        }
                    });
        });

        Button btnTrafficOff = findViewById(R.id.btnTrafficOff);
        btnTrafficOff.setOnClickListener(v -> map.getUiSettings().turnOffTraffic());
    }

    @Override
    public void onMapReady(@NonNull TomtomMap tomtomMap) {
        this.map = tomtomMap;

        LatLng amsterdam = new LatLng(52.37, 4.90);
        SimpleMarkerBalloon balloon = new SimpleMarkerBalloon("Amsterdam");
        tomtomMap.addMarker(new MarkerBuilder(amsterdam).markerBalloon(balloon));
        tomtomMap.centerOn(CameraPosition.builder(amsterdam).zoom(7.0).build());
    }
}
