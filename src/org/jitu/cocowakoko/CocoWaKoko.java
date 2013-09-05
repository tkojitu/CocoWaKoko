package org.jitu.cocowakoko;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class CocoWaKoko extends Activity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    private LocationClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!makeAppHome()) {
            finish();
        }
        client = new LocationClient(this, this, this);
        setContentView(R.layout.activity_coco_wa_koko);
    }

    private boolean makeAppHome() {
        File home = getAppHome();
        if (!home.exists()) {
            if (!home.mkdir()) {
                Toast.makeText(this, "cannot make app home", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        if (!home.isDirectory()) {
            Toast.makeText(this, "app home is not a directory", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private File getAppHome() {
        File storage = Environment.getExternalStorageDirectory();
        return new File(storage, getAppName());
    }

    private String getAppName() {
        return getResources().getString(R.string.app_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.coco_wa_koko, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }
    
    @Override
    protected void onStop() {
        client.disconnect();
        super.onStop();
    }

    public void onClickCocoWaDoko(View view) {
        if (!servicesConnected()) {
            Toast.makeText(this, "not connected to GPS", Toast.LENGTH_LONG).show();
            return;
        }
        Location loc = client.getLastLocation();
        if (loc == null) {
            Toast.makeText(this, "location is not available", Toast.LENGTH_LONG).show();
            return;
        }
        String uriStr = getLocationUriString(loc);
        if (!storeUriString(uriStr)) {
            return;
        }
        startMap(Uri.parse(uriStr));
    }

    private boolean storeUriString(String uriStr) {
        File file = getUriFile();
        return saveFile(file, uriStr);
    }

    public boolean saveFile(File file, String text) {
        try {
            FileWriter wf = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(wf);
            bw.write(text, 0, text.length());
            bw.newLine();
            bw.close();
            return true;
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private File getUriFile() {
        return new File(getAppHome(), "uri.txt");
    }

    private String getLocationUriString(Location loc) {
        StringBuilder buf = new StringBuilder("geo:0,0?q=");
        buf.append(loc.getLatitude())
            .append(",")
            .append(loc.getLongitude());
        return buf.toString();
    }

    public void onClickShowMap(View view) {
        File file = getUriFile();
        String uriStr = readUriStr(file);
        if (uriStr == null) {
            return;
        }
        startMap(Uri.parse(uriStr));
    }

    private String readUriStr(File file) {
        String uriStr = null;
        try {
            FileReader fr = new FileReader(file);
            BufferedReader bf = new BufferedReader(fr);
            try {
                String line;
                while ((line = bf.readLine()) != null) {
                    uriStr = line;
                }
                return uriStr;
            } finally {
                bf.close();
            }
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return uriStr;
        }
    }

    private void startMap(Uri uri) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 9000);
            } catch (IntentSender.SendIntentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
            }
        } else {
            Toast.makeText(this, "no resolution: " + connectionResult.getErrorCode(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        Toast.makeText(this, "connected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "disconnected", Toast.LENGTH_LONG).show();
    }

    private boolean servicesConnected() {
        int ret = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == ret) {
            return true;
        }
        Toast.makeText(this, "GooglePlayServices are disable: " + ret, Toast.LENGTH_LONG).show();
        return false;
    }
}
