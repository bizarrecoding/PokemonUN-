package com.example.herik21.pokemongo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<LocationSettingsResult>,OnMapReadyCallback {

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500 ;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 123;
    private GoogleApiClient mApiClient;
    private LocationRequest mLocationRequest;
    private GoogleMap gmap;
    private ArrayList<Marker> markers = new ArrayList<>();
    private Marker trainer;
    private ProgressDialog pDialog;
    private ArrayList<WildMarker> wildMarkers = new ArrayList<>();
    private TextView log;
    private boolean moveCam;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        moveCam = true;
        log = (TextView)findViewById(R.id.log);
        FlowManager.init(new FlowConfig.Builder(this).openDatabasesOnInit(true).build());
        mApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        onGO();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(MapsActivity.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connMgr.getActiveNetworkInfo();
        if(!ni.isConnected()){
            Toast.makeText(this,"Not Connected to Internet",Toast.LENGTH_LONG).show();
        }
    }

    public void append(String entry){
        String logText = log.getText().toString();
        log.setText(logText+"\n"+entry);
    }

    public void onGO(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest nLocationSettingsRequest = builder.build();
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mApiClient,
                        nLocationSettingsRequest);
        result.setResultCallback(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        append("Welcome trainer");
        gmap = googleMap;
        gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(!marker.getTitle().equals("Trainer")){
                    Intent i = new Intent(MapsActivity.this,BattleActivity.class);
                    TeamPokemon starter = new Select().from(TeamPokemon.class).where(TeamPokemon_Table.id.is(1)).querySingle();
                    Pokemon wild = new Select().from(Pokemon.class).where(Pokemon_Table.name.is(marker.getTitle())).querySingle();
                    i.putExtra("pokemon",starter);
                    i.putExtra("wild",wild);
                    startActivityForResult(i,1);
                    return true;
                }else{
                    Intent i = new Intent(MapsActivity.this,TeamActivity.class);
                    startActivity(i);
                }
                return false;
            }
        });
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }else{
            gmap.setMyLocationEnabled(true);
            gmap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MapsActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                        return false;
                    }
                    Location loc = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
                    LatLng myLocation = new LatLng(loc.getLatitude(),loc.getLongitude());
                    updateUI(myLocation,moveCam);
                    return false;
                }
            });
            try{
                LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient,mLocationRequest,this);
                Location loc = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
                LatLng myLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
                new HttpAsyncTask(loc.getLatitude(),loc.getLongitude()).execute();
                updateUI(myLocation,moveCam);
            }catch (Exception ex){
                Log.d("GMAPS","no location, api restricted");
            }
        }
    }

    public static String getStops(double lat, double lng){
        String response = "";
        try{
            URL url = new URL("http://190.144.171.172/function3.php?lat="+lat+"&lng="+lng);
            URLConnection uc = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String result;
            while ((result = in.readLine())!=null){
                Log.d("HTTPGET","result = "+result);
                response = result;
            }
            in.close();
            return response;
        }catch (Exception ex){
            Log.d("HTTPGET",ex.getLocalizedMessage());
            return null;
        }
    }

    public void updateUI(LatLng location, boolean move) {
        //gmap.clear();
        gmap.setMaxZoomPreference(19.5f);
        gmap.setMinZoomPreference(16.5f);
        if(move){
           moveCam = false;
           gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
        }
        if(trainer!=null){
            trainer.setPosition(location);
        }else{
            trainer = gmap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pkmn_loc))
                    .position(location)
                    .title("Trainer"));
        }
        setWildPokemons();
    }

    public void setWildPokemons(){
        for(WildMarker wMarker : wildMarkers) {
            Marker mk = wMarker.mk;
            if(mk==null) {
                mk = gmap.addMarker(new MarkerOptions()
                        .position(new LatLng(wMarker.loc[0],wMarker.loc[1]))
                        .visible(true));
            }
            if (wMarker.visible) {
                mk.setVisible(true);
                Pokemon pk = new Select().from(Pokemon.class).where(Pokemon_Table.id.is(wMarker.pkid)).querySingle();
                mk.setPosition(new LatLng(wMarker.loc[0], wMarker.loc[1]));
                mk.setTitle(pk.name);
                new MarkerIconAsyncTask(mk).execute(pk.imgFront);
            } else {
                mk.setVisible(false);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mApiClient != null) {
            mApiClient.connect();
            Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mApiClient.connect();
    }
    @Override
    protected void onPause() {
        mApiClient.disconnect();
        super.onPause();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {}
    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}
    @Override
    public void onLocationChanged(Location location) {
        updateUI(new LatLng(location.getLatitude(),location.getLongitude()),moveCam);
        new DistanceAsyncTask(location).execute();
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        Log.d("GPS", "onResult" + status.getStatusCode());
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                if (mApiClient.isConnected()) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                        return;
                    }
                    Log.d("fuck permissions","line247");
                    gmap.setMyLocationEnabled(true);
                    gmap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                        @Override
                        public boolean onMyLocationButtonClick() {
                            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                                return false;
                            }
                            Log.d("fuck permissions","line259");
                            Location loc = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
                            LatLng myLocation = new LatLng(loc.getLatitude(),loc.getLongitude());
                            updateUI(myLocation,moveCam);
                            return false;
                        }
                    });
                    Log.d("fuck permissions","line266");
                    LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
                    Location loc = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
                    new HttpAsyncTask(loc.getLatitude(),loc.getLongitude()).execute();
                    LatLng myLocation = new LatLng(loc.getLatitude(),loc.getLongitude());
                    updateUI(myLocation,moveCam);
                }
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Toast.makeText(this, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 1){
                Pokemon newPk = (Pokemon) data.getSerializableExtra("captured");
                if(newPk!=null){
                    long size = new Select().from(TeamPokemon.class).count();
                    Log.d("SIZE","before: "+size);
                    int newHP = data.getIntExtra("hp",20);
                    int newAtk = data.getIntExtra("atk",20);
                    int newDef = data.getIntExtra("def",20);
                    Pokemon wild = (Pokemon) data.getSerializableExtra("bwild");
                    TeamPokemon newTeam = new TeamPokemon(wild,newHP,newAtk,newDef);
                    newTeam.save();

                    Log.d("SIZE","after: "+size);
                    size = new Select().from(TeamPokemon.class).count();
                    append(wild.name+" Captured! (Team size "+size+")");
                }
            }
        }
    }

    @Override
    public void onBackPressed(){
        if (doubleBackToExitPressedOnce){
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce=true;
        Toast.makeText(this,"Please click BACK again to exit",Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        },2000);
    }

    private class DistanceAsyncTask extends AsyncTask<Void,Void,Boolean>{
        private Location myLocation;

        public DistanceAsyncTask(Location loc){
            this.myLocation = loc;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean near =false;
            double lat = myLocation.getLatitude();
            double lon = myLocation.getLongitude();
            int count=0;
            for (WildMarker wildpkmn : wildMarkers){
                double distance = Math.sqrt(Math.pow(lat - wildpkmn.loc[0], 2) + Math.pow(lon - wildpkmn.loc[1], 2));
                Log.d("DISTANCE","distance "+10000*distance);
                if(distance*10000<=5){
                    count++;
                    Log.d("DISTANCE","Marker with id "+wildpkmn.pkid+" visible, distance "+10000*distance+" visible "+count+" out of "+wildMarkers.size());
                    wildpkmn.visible = true;
                    near = true;
                }
            }
            return near;
        }

        @Override
        protected void onPostExecute(Boolean near) {
            super.onPostExecute(near);
            if(near){
                append("wild pokemons near you");
            }
        }
    }

    private class HttpAsyncTask extends AsyncTask<Void, Void, Void> {

        private final double lng;
        private final double lat;

        public HttpAsyncTask(double lat, double lng){
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Please Wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... Voids) {
            String obj = MapsActivity.getStops(lat,lng);
            if(obj != null){
                try {
                    JSONArray jsonArray = new JSONArray(obj);
                    Random r = new Random();
                    for (int i = 0 ; i<jsonArray.length();i++){
                        JSONObject stop = jsonArray.getJSONObject(i);
                        String lt = stop.getString("lt");
                        String lng = stop.getString("lng");
                        double[] loc = {Double.parseDouble(lt), Double.parseDouble(lng)};
                        wildMarkers.add(new WildMarker(r.nextInt(10),loc,false));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Log.d("HTTPGET","null response");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
        }
    }

    private class MarkerIconAsyncTask extends AsyncTask<String,Void,Bitmap>{
        public Marker mk;
        public MarkerIconAsyncTask(Marker marker){
            this.mk = marker;
        }
        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new URL(imageURL).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Log.d("ICONS","bitmap setted on marker");
            mk.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }
}