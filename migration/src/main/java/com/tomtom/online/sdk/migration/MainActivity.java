package com.tomtom.online.sdk.migration;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.tomtom.online.sdk.routing.RoutingException;
import com.tomtom.online.sdk.routing.route.RouteCalculationDescriptor;
import com.tomtom.online.sdk.routing.route.RouteCallback;
import com.tomtom.online.sdk.routing.route.RouteDescriptor;
import com.tomtom.online.sdk.routing.route.RoutePlan;
import com.tomtom.online.sdk.routing.route.RouteSpecification;
import com.tomtom.online.sdk.routing.route.information.FullRoute;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TomtomMap map;
    private Button btnTrafficOn;
    private Button btnTrafficOff;
    private Button btnRouteShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if(mapFragment != null) {
            mapFragment.getAsyncMap(this);
        }

        btnTrafficOn = findViewById(R.id.btnTrafficOn);
        btnTrafficOff = findViewById(R.id.btnTrafficOff);
        btnRouteShow = findViewById(R.id.btnRouteShow);
    }

    @Override
    public void onMapReady(@NonNull TomtomMap tomtomMap) {
        this.map = tomtomMap;

        btnTrafficOn.setOnClickListener(v -> {
            map.getTrafficSettings().turnOnTrafficIncidents();
            map.getTrafficSettings().turnOnTrafficFlowTiles();
        });

        btnTrafficOff.setOnClickListener(v -> {
            map.getTrafficSettings().turnOffTrafficFlowTiles();
            map.getTrafficSettings().turnOffTrafficIncidents();
        });

        btnRouteShow.setOnClickListener(showRouteListener);

        LatLng amsterdam = new LatLng(52.37, 4.90);
        SimpleMarkerBalloon balloon = new SimpleMarkerBalloon("Amsterdam");
        tomtomMap.addMarker(new MarkerBuilder(amsterdam).markerBalloon(balloon));
        tomtomMap.centerOn(CameraPosition.builder().focusPosition(amsterdam).zoom(7.0).build());
    }

    private View.OnClickListener showRouteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LatLng amsterdam = new LatLng(52.37, 4.90);
            LatLng hague = new LatLng(52.07, 4.30);
            RoutingApi routingApi = OnlineRoutingApi.create(getApplicationContext(), BuildConfig.ROUTING_API_KEY);

            RouteDescriptor routeDescriptor = new RouteDescriptor.Builder()
                    .routeType(com.tomtom.online.sdk.routing.route.description.RouteType.FASTEST)
                    .build();
            RouteCalculationDescriptor routeCalculationDescriptor = new RouteCalculationDescriptor.Builder()
                    .routeDescription(routeDescriptor)
                    .build();
            RouteSpecification routeSpecification = new RouteSpecification.Builder(amsterdam, hague)
                    .routeCalculationDescriptor(routeCalculationDescriptor)
                    .build();

            routingApi.planRoute(routeSpecification, new RouteCallback() {
                @Override
                public void onSuccess(@NotNull RoutePlan routePlan) {
                    for (FullRoute fullRoute : routePlan.getRoutes()) {
                        RouteBuilder routeBuilder = new RouteBuilder(
                                fullRoute.getCoordinates());
                        map.addRoute(routeBuilder);
                    }
                }

                @Override
                public void onError(@NotNull RoutingException e) {
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    };
}
