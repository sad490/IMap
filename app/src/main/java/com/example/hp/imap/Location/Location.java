package com.example.hp.imap.Location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapsInitializer;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.Circle;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.LatLng;
import com.example.hp.imap.MainActivity;
import com.example.hp.imap.R;

/**
 * Created by hp on 2017/7/11.
 */

public class Location extends Activity {
    private AMap aMap;
    private MapView mapView;
    private ImageButton Location;
    private ImageButton Circle_Gen;
    private ImageButton Poly_Gen;
    private ImageButton Map_3D;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private enum MAP_MODE { LOCATION, CIRCLE, RCTANGLE };
    MAP_MODE map_mode = MAP_MODE.LOCATION;
    private Circle circle;

    @Override
    public void onCreate(Bundle savedInstace)
    {
        super.onCreate(savedInstace);
        setContentView(R.layout.location_main);
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.onCreate(savedInstace);
        init();
        MapsInitializer.loadWorldGridMap(true);
    }

    public void init()
    {
        Location = (ImageButton)findViewById(R.id.Location);
        Circle_Gen = (ImageButton)findViewById(R.id.circle_gen);
        Poly_Gen = (ImageButton)findViewById(R.id.squ_gen);
        Map_3D = (ImageButton)findViewById(R.id.map_3d);

        locationOption = new AMapLocationClientOption();
        locationOption.setGpsFirst(true);
        locationOption.setSensorEnable(true);
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setOnceLocation(true);

        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationClient.setLocationOption(locationOption);
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if(aMapLocation != null)
                {
                    LatLng Loc = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                    Log.d("Loc",  aMapLocation.getLatitude() + ", " + aMapLocation.getLongitude());
                    Log.d("Attuitle = " + aMapLocation.getAltitude()," m.");
                    moveTo(Loc);
                }
            }
        });

        Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location();
            }
        });

        Circle_Gen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_mode = MAP_MODE.CIRCLE;
            }
        });
        Poly_Gen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Map_3D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if(aMap == null)
        {
            aMap = mapView.getMap();
            setUpMap();
        }
    }

    public void setUpMap()
    {
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {

            }
        });
        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(map_mode == MAP_MODE.CIRCLE)
                {
                    circle = aMap.addCircle(new CircleOptions().center(latLng).fillColor(255).strokeColor(0).visible(true));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        locationClient.stopLocation();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        locationClient.stopLocation();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        intent.setClass(Location.this, MainActivity.class);
        super.onBackPressed();
        return;
    }

    public void Location()
    {
        locationClient.startLocation();
    }

    public void moveTo(LatLng pos)
    {
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
        aMap.invalidate();
    }

}
