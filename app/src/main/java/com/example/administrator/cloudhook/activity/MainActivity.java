package com.example.administrator.cloudhook.activity;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.administrator.cloudhook.R;
import com.example.administrator.cloudhook.util.FileUtil;
import com.example.administrator.cloudhook.util.JSONObjectUtil;
import com.example.administrator.cloudhook.util.ReflectUtil;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        final TextView textView = (TextView)findViewById(R.id.textView);
        textView.setText("Locating ...");
        final TextView textView2 = (TextView)findViewById(R.id.textView2);
        textView2.setText("Listening ...");

        // Location
        try {
            final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2 * 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    String timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    String string = "latitude: " + lat + " longitude: " + lng;
                    String log = timeString + " - " + string + "\r\n";

                    textView.setText(string);
                    Log.d(TAG, "----->>>>> onLocationChanged lat:" + lat + " lng:" + lng);

                    String filename = MainActivity.this.getFilesDir().getAbsolutePath() + "/locations.log";
                    FileUtil.appendTextToFile(log, filename);

                    File f = new File(filename);
                    if (f.length() >= 5 * 1024 * 1024) { // 10MB
                        FileUtil.devideFile(f);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {  }
                @Override
                public void onProviderEnabled(String provider) { }
                @Override
                public void onProviderDisabled(String provider) { }
            });

            locationManager.addGpsStatusListener(new GpsStatus.Listener(){
                @Override
                public void onGpsStatusChanged(int event) {
                    textView2.setText("GPS Status:" + event);
                    Log.d(TAG, "----->>>>> onGpsStatusChanged event:" + event);
                }
            });

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // LocationManager.NETWORK_PROVIDER
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                String string = "lat:" + lat + " lng:" + lng;

                textView.setText(string);
                Log.d(TAG, "----->>>>> getLastKnownLocation " + string);
            }

            // WIFI
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> scanResults = wifiManager.getScanResults();
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            String dhcpInfoString = JSONObjectUtil.objToJson(dhcpInfo);
            JSONObject jsonObject = new JSONObject(dhcpInfoString);
            List<WifiConfiguration> cons = wifiManager.getConfiguredNetworks();
            Log.d(TAG, dhcpInfo.toString());

            // ApplicationLoaders
            Class<?> clz = Class.forName("android.app.ApplicationLoaders");
            String fieldSig = ReflectUtil.getFieldSignature(clz, "mLoaders");
            String methodSig = ReflectUtil.getMethodSignature(Class.class, "forName");

            Log.d(TAG, "----------");

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
