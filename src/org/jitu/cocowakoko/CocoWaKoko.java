package org.jitu.cocowakoko;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new LocationClient(this, this, this);
        setContentView(R.layout.activity_coco_wa_koko);
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
        if (servicesConnected()) {
            location = client.getLastLocation();
            String msg = (location == null) ? "location is null" : location.toString();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    public void onClickSendLocation(View view) {
        Uri uri = getLocationUri();
        if (uri == null) {
            Toast.makeText(this, "location is null", Toast.LENGTH_LONG).show();
            return;
        }
        startMap(uri);
    }

    private Uri getLocationUri() {
        if (location == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder("geo:0,0?q=");
        buf.append(location.getLatitude())
            .append(",")
            .append(location.getLongitude());
        return Uri.parse(buf.toString());
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
