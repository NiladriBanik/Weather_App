package com.example.weather_appnsb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout honeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV,temparatureTV,conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;
    public String bestProvider;
    public Criteria criteria;




    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        honeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temparatureTV = findViewById(R.id.idTVTemparature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRvWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        getWeatherInfo("Agartala");
//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED && !isLocationEnabled(MainActivity.this)){
//            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
//
//        }
//
//        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        criteria = new Criteria();
//        bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
//
//
//        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        if(location!=null) {
//            cityName = getCityName(location.getLongitude(),location.getLatitude());
//            getWeatherInfo(cityName);
//            searchIV.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String city = Objects.requireNonNull(cityEdt.getText()).toString();
//                    if(city.isEmpty()){
//                        Toast.makeText(MainActivity.this, "Please enter city Name", Toast.LENGTH_SHORT).show();
//                    }else{
//                        cityNameTV.setText(cityName);
//                        getWeatherInfo(city);
//                    }
//                }
//            });
//        }
//        else {
//            locationManager.requestLocationUpdates(bestProvider, 1000, 0, (LocationListener) this);
//            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
//
//        }
    }
//


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "permissions granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Please Provide the permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }



    private String getCityName(double longitude, double latitude){
        String cityName = "Agartala";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude,longitude, 10);

            for(Address adr : addresses){
                if(adr!=null){
                    String city = adr.getLocality();
                    if(city!=null && !city.equals("")){
                        cityName =city;
                    }else{
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this, "User city Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return cityName;
    }


    private void getWeatherInfo(String cityName){
        String url = "https://api.weatherapi.com/v1/current.json?key=7005bc2890a5401785244848232203&q=" + cityName;
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue( MainActivity.this);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                honeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try {
                    String temparature = response.getJSONObject("current").getString("temp_c");
                    temparatureTV.setText(temparature+"°c");
                    String isDay = response.getJSONObject("current").getString("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");

                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);

                    if(isDay.equals("1")){
                        //morning
                        Picasso.get().load("https://wallpaperaccess.com/full/4046896.jpg").into(backIV);
                    }else{
                        Picasso.get().load("https://images.unsplash.com/photo-1475274047050-1d0c0975c63e?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxleHBsb3JlLWZlZWR8MXx8fGVufDB8fHx8&w=1000&q=80").into(backIV);
                    }

                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }


}