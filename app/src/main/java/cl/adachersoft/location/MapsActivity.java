package cl.adachersoft.location;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import cl.adachersoft.location.views.MainActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String lat;
    private String lon;
    private Double latdouble;
    private Double londouble;
    private LatLng location;
    private AlertDialog alert;
    private Double milat = 0.0;
    private Double milon = 0.0;
    private int detenido = 0;
    private int ingreso = 0;
    Location arg10;
    private GoogleMap mapInicialize;
    private int inicialize = 0;
    private String latlon;
    private ProgressDialog pd = null;
    private int gpsactivo = 0;
    private int reclamo = 0;
    private String clientebd;
    private TextView textoestado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        lat = intent.getStringExtra(MainActivity.LAT);
        lon = intent.getStringExtra(MainActivity.LNG);
        latdouble = Double.parseDouble(lat);
        londouble = Double.parseDouble(lon);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void AlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }

    private void setUpMap() {
        try {
            mMap.addMarker(new MarkerOptions().position(new LatLng(latdouble, londouble)).title("Mi Destino").snippet("Destino"));
            mMap.setMyLocationEnabled(true);
        } catch (Exception e) {
            e.getMessage();
        }
    }


    private void setUpMapIfNeeded() {
        Location loc = comenzarLocalizacion();

        double latloc = 0.0;
        double lonloc = 0.0;
        try {
            latloc = loc.getLatitude();
            lonloc = loc.getLongitude();
        } catch (Exception e) {
            latloc = 0.0;
            lonloc = 0.0;
        }

        if (latloc == 0.0 && lonloc == 0.0) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            try {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                latloc = latitude;
                lonloc = longitude;
            } catch (Exception e) {
                latloc = 0.0;
                lonloc = 0.0;
                try {
                    location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    lonloc = location.getLongitude();
                    latloc = location.getLatitude();
                } catch (Exception ex) {
                    latloc = 0.0;
                    lonloc = 0.0;
                    location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                }
            }
        }

        gpsactivo = 0;
        gpsactivo = ForzarActivacionGPS();
        if (gpsactivo == 0) {
            if (reclamo == 0) {
                reclamo = 1;
                AlertNoGps();
            }
        }

        mMap.clear();
        mMap = null;
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mMap = mapInicialize;
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                if (mMap != null) {
                    mMap.clear();
                    setUpMap();
                    milat = latloc;
                    milon = lonloc;
                    ingreso = 1;
                    LatLng latLng = new LatLng(milat, milon);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(milat, milon))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .title("Aquí me encuentro"));
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latdouble, londouble)).title("Mi Destino").snippet("Destino"));
                    mMap.setMyLocationEnabled(true);
                    midPoint(milat, milon, latdouble, londouble);
                }
            }
        }
    }


    public void midPoint(double lat1, double lon1, double lat2, double lon2) {
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(lat2, lon2))
                .include(new LatLng(lat1, lon1)).build();
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));
    }


    public int ForzarActivacionGPS() {
        int retorno = 0;
        double lat = 0.0;
        double lon = 0.0;

        try {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (!provider.contains("gps")) { //if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
        } catch (Exception e) {
        }
        try {
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", true);
            sendBroadcast(intent);
        } catch (Exception e) {

        }
        try {
            LocationListener locListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                }

                public void onProviderDisabled(String provider) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.i("", "Provider Status: " + status);
                }
            };

            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
            Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) {
                locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
                loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            double latloc = 0.0;
            double lonloc = 0.0;
            try {
                latloc = loc.getLatitude();
                lonloc = loc.getLongitude();
            } catch (Exception e) {
                latloc = 0.0;
                lonloc = 0.0;
            }
            if (latloc == 0.0 && lonloc == 0.0) {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                try {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    latloc = latitude;
                    lonloc = longitude;
                } catch (Exception e) {
                    latloc = 0.0;
                    lonloc = 0.0;
                    try {
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        lonloc = location.getLongitude();
                        latloc = location.getLatitude();
                    } catch (Exception ex) {
                        latloc = 0.0;
                        lonloc = 0.0;
                        location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    }
                }
            }
            lat = latloc;
            lon = lonloc;
        } catch (Exception e) {
            retorno = 0;
        }
        if (lat == 0.0 && lon == 0.0) {
            retorno = 0;
        } else {
            retorno = 1;
        }
        return retorno;
    }


    private Location comenzarLocalizacion() {
        LocationListener locListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i("", "Provider Status: " + status);
            }
        };

        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
        Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null) {
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
            loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return loc;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (inicialize == 0) {
            inicialize = 1;
            mMap = googleMap;
            mapInicialize = mMap;
            LatLng mylocation = new LatLng(latdouble, londouble);
            mMap.addMarker(new MarkerOptions().position(mylocation).title("Marker in my location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));

            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertNoGps();
            } else {
                setUpMapIfNeeded();
            }
        }
    }
}
