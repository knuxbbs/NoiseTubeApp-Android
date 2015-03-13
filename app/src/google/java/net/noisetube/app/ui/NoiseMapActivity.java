package net.noisetube.app.ui;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import net.noisetube.R;
import net.noisetube.api.model.NTLocation;
import net.noisetube.api.model.NTMeasurement;
import net.noisetube.api.model.SoundLevelScale;
import net.noisetube.api.model.Track;
import net.noisetube.api.ui.TrackUIAdapter;
import net.noisetube.api.util.Measurement;
import net.noisetube.app.location.AndroidNTCoordinates;
import net.noisetube.app.ui.model.TrackData;
import net.noisetube.app.util.DialogUtils;
import net.noisetube.app.util.NTUtils;

import java.util.List;

public class NoiseMapActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, GoogleMap.OnCameraChangeListener {


    MapFragment mapFragment;
    float zIndex = 0;
    private GoogleMap mMap;

    TrackUIAdapter adapter = new TrackUIAdapter() {
        @Override
        public void newMeasurement(final Track track, final NTMeasurement newMeasurement, NTMeasurement savedMeasurement) {
            super.newMeasurement(track, newMeasurement, savedMeasurement);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMap != null) {
                        NTLocation location = newMeasurement.getLocation();

                        if (location != null && location.getCoordinates() != null && ((AndroidNTCoordinates) location.getCoordinates()).getLocation() != null) {
                            final AndroidNTCoordinates coordinates = (AndroidNTCoordinates) location.getCoordinates();
                            onMyLocationChange(coordinates.getLocation());
                            Measurement item = new Measurement(newMeasurement.getTimeStamp(), coordinates.getLatitude(), coordinates.getLongitude(), newMeasurement.getLeqDBA());
                            drawNoiseMeasurement(item);

                        }
                    }
                }
            });


        }
    };
    private float zoomLevel = -1;
    private float tiltLevel;
    private float bearing;
    private List<Measurement> measurements;
    private boolean viewingTraces = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noise_map);

        Track track = androidNTService.getTrack();
        Object obj = getIntent().getSerializableExtra("TRACK");
        if (obj != null) {

            if (track != null && track.isRunning()) {
                track.pause();
                Toaster.displayShortToast(String.valueOf(getResources().getText(R.string.action_paused_measure)));
            }

            TrackData item = (TrackData) obj;
            measurements = item.getMeasurements();

            findViewById(R.id.details_container).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.track_id)).setText(item.getId());
            ((TextView) findViewById(R.id.total_measurements)).setText(String.valueOf(item.getTotalMeasurements()));

        } else if (track != null) {
            track.addTrackUIListener(adapter);
            findViewById(R.id.status_container).setVisibility(View.VISIBLE);
        }


        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.noise_map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (androidNTService != null) {
            Track track = androidNTService.getTrack();
            if (track != null)
                track.removeTrackUIListener(adapter);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NTUtils.supportsInternetAccess()) {
            DialogUtils.showMapInternetDialog(this);
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        // we only have a nav drawer if we are in top-level Explore mode.
        return NAVDRAWER_ITEM_MAP;
    }

    public void onMyLocationChange(Location lastKnownLocation) {
        if (lastKnownLocation != null) {
            LatLng latlng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            CameraUpdate myLoc = CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(latlng)
                            .zoom(zoomLevel)
                            .tilt(tiltLevel)
                            .bearing(bearing)
                            .build());
            mMap.animateCamera(myLoc);
        }
    }

    public void onCameraChange(CameraPosition position) {
        tiltLevel = position.tilt;
        bearing = position.bearing;
        if (zoomLevel == -1)
            zoomLevel = 22;
        else
            zoomLevel = position.zoom;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setIndoorEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraChangeListener(NoiseMapActivity.this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (measurements != null && !measurements.isEmpty()) {
                    Measurement item = measurements.get(0);
                    if (item.getLatitude() > -1 && item.getLongitude() > -1) {
                        LatLng latlng = new LatLng(item.getLatitude(), item.getLongitude());

                        CameraUpdate center =
                                CameraUpdateFactory.newLatLng(latlng);
                        CameraUpdate zoom = CameraUpdateFactory.zoomTo(25);

                        mMap.moveCamera(center);
                        mMap.animateCamera(zoom);
                    }
                } else {


                    measurements = androidNTService.getNoiseMapMeasureBuffer();

                    if (measurements.isEmpty()) {
                        mMap.setMyLocationEnabled(true);
                        mMap.setOnMyLocationChangeListener(NoiseMapActivity.this);

                        onMyLocationChange(mMap.getMyLocation());
                    }

                }
                for (Measurement item : measurements) {
                    drawNoiseMeasurement(item);
                }
            }
        });
    }

    private void drawNoiseMeasurement(Measurement item) {
        if (item.getLatitude() > -1 && item.getLongitude() > -1) {
            int color = SoundLevelScale.getNoiseMapColor(item.getLoudness()).getARGBValue();

            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(item.getLatitude(), item.getLongitude()))
                    .strokeWidth(1)
                    .strokeColor(color)
                    .fillColor(color)
                    .radius(1); // In meters

            circleOptions.zIndex(zIndex++);

            mMap.addCircle(circleOptions);
        }

    }
}
