package me.jrdh.parcel;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.jrdh.parcel.api.models.Shipment;

public class PackageDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @BindView(R.id.packageDetails)
    private RecyclerView detailsRecyclerView;

    private Shipment shipment;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_package_details);

        ButterKnife.bind(this);

        if (toolbar != null)
            setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        detailsRecyclerView.setLayoutManager(layoutManager);

        shipment = Parcels.unwrap(getIntent().getExtras().getParcelable(Shipment.class.getName()));

        getSupportActionBar().setTitle(shipment.label);

        detailsRecyclerView.setAdapter(new PackageDetailsAdapter(shipment.locations));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_package_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete_delivery:
                break;
            case R.id.action_edit_delivery:
                break;
            case R.id.action_track_on_website:
                break;
            case R.id.action_mark_delivered:
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.getUiSettings().setAllGesturesEnabled(false);

        Geocoder gcoder = new Geocoder(this);

        List<Address> addresses = null;

        try {
            addresses = gcoder.getFromLocationName(shipment.locations.get(0).address,1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses == null || addresses.isEmpty())
            return;


        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng latlng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
        //mMap.addMarker(new MarkerOptions().position(latlng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));

    }
}
