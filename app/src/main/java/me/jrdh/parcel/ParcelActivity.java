package me.jrdh.parcel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.jrdh.parcel.api.HmacInterceptor;
import me.jrdh.parcel.api.ParcelService;
import me.jrdh.parcel.api.ParcelShipmentsDeserializer;
import me.jrdh.parcel.api.UserAgentInterceptor;
import me.jrdh.parcel.api.models.Shipment;
import me.jrdh.parcel.api.models.ShipmentUpdates;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ParcelActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.parcel_tablayout)
    TabLayout tabLayout;

    @BindView(R.id.parcel_viewpager)
    ViewPager viewPager;

    List<Shipment> shipments;

    ParcelService apiService;

    private RxBus _rxBus = null;

    public RxBus getRxBusSingleton() {
        if (_rxBus == null)
            _rxBus = new RxBus();
        return _rxBus;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcel);

        ButterKnife.bind(this);

        if (toolbar != null)
            setSupportActionBar(toolbar);

        initializeApiService();
        loadJson();

        getRxBusSingleton().toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(a -> updateShipmentData());
    }

    void updateShipmentData () {
        // FIXME: Duplicate code here, clean up and merge with loadJson method
        apiService.getShipments()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        shipments -> updateShipmentFragments(shipments.getShipments()),
                        e -> Log.e("Parcel", "Failed get shipments: " + e.getMessage())
                );
    }

    void updateShipmentFragments(List<Shipment> shipments) {
        this.shipments = shipments;
        // All
        ((PackagesFragment)((ViewPagerAdapter)viewPager.getAdapter()).getItem(0)).updateShipments(shipments);
        // Active
        ((PackagesFragment)((ViewPagerAdapter)viewPager.getAdapter()).getItem(1)).updateShipments(Stream.of(shipments).filter(s -> s.update).collect(Collectors.toCollection(ArrayList::new)));
        // Delivered
        ((PackagesFragment)((ViewPagerAdapter)viewPager.getAdapter()).getItem(2)).updateShipments(Stream.of(shipments).filter(s -> !s.update).collect(Collectors.toCollection(ArrayList::new)));
    }

    void initializeApiService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new UserAgentInterceptor())
                //.addInterceptor(new AuthCookieInterceptor(PreferenceManager.getDefaultSharedPreferences(this).getString(ParcelSettings.HASH, "")))
                .addInterceptor(new HmacInterceptor(PreferenceManager.getDefaultSharedPreferences(this).getString(ParcelSettings.USER_ID, "")))
                .addInterceptor(logging)
                .build();

        GsonBuilder gsonBuilder = new GsonBuilder();

        // Adding custom deserializers
        gsonBuilder.registerTypeAdapter(ShipmentUpdates.class, new ParcelShipmentsDeserializer());
        Gson gson = gsonBuilder.create();
        GsonConverterFactory gsonConverter = GsonConverterFactory.create(gson);

        Retrofit rf = new Retrofit.Builder()
                .baseUrl("https://data.parcelapp.net/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(gsonConverter)
                .client(httpClient)
                .build();

        apiService = rf.create(ParcelService.class);
    }

    private void loadJson () {
        apiService.getShipments()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                shipments -> processShipments(shipments),
                e -> Log.e("Parcel", "Failed get shipments: " + e.getMessage())
            );
    }

    void processShipments(ShipmentUpdates shipmentUpdates) {
        shipments = shipmentUpdates.getShipments();
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_parcel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            clearUserSettings();
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void clearUserSettings() {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefs.remove(ParcelSettings.USER_ID);
        prefs.remove(ParcelSettings.HASH);
        prefs.apply();
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        PackagesFragment allShipmentFragment;
        PackagesFragment activeShipmentFragment;
        PackagesFragment deliveredShipmentFragment;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return allShipmentFragment == null ? allShipmentFragment = PackagesFragment.newInstance(shipments) : allShipmentFragment;
                case 1:
                    return activeShipmentFragment == null ? activeShipmentFragment = PackagesFragment.newInstance(Stream.of(shipments).filter(s -> s.update).collect(Collectors.toCollection(ArrayList::new))) : activeShipmentFragment;
                case 2:
                    return deliveredShipmentFragment == null ? deliveredShipmentFragment = PackagesFragment.newInstance(Stream.of(shipments).filter(s -> !s.update).collect(Collectors.toCollection(ArrayList::new))) : deliveredShipmentFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle (int position) {
            switch (position) {
                case 0:
                    return "All";
                case 1:
                    return "Active";
                case 2:
                    return "Delivered";
                default:
                    return "Invalid";
            }

        }
    }
}
