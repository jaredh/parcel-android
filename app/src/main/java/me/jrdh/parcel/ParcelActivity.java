package me.jrdh.parcel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
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

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
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

    @BindView(R.id.fabBtn)
    FloatingActionButton fab;

    List<Shipment> shipments = new ArrayList<>();

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

        setSupportActionBar(toolbar);

        initializeApiService();

        getRxBusSingleton().toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(a -> fetchShipmentData());

        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        fab.setOnClickListener(v ->
                v.getContext().startActivity(
                        new Intent(v.getContext(), AddPackageActivity.class)
                )
        );

    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchShipmentData();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("activity_shipments", Parcels.wrap(shipments));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        shipments = Parcels.unwrap(savedInstanceState.getParcelable("activity_shipments"));
    }

    void fetchShipmentData() {
        apiService.getShipments()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        shipments -> updateShipmentFragments(shipments.getShipments()),
                        e -> Log.e("Parcel", "Failed get shipments: " + e.getMessage())
                );
    }

    void updateShipmentFragments(List<Shipment> shipments) {
        sortAndUpdateShipments(shipments);

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

    void sortAndUpdateShipments(List<Shipment> shipments) {
        Collections.sort(shipments, (lhs, rhs) -> {
            DateTime d1 = parseDateTime(lhs.dateExpected);
            DateTime d2 = parseDateTime(rhs.dateExpected);
            return DateTimeComparator.getInstance().compare(d1,d2);
        });

        Collections.reverse(shipments);
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

    DateTime parseDateTime (String dateTime) {
        DateTimeParser[] parsers = {
                DateTimeFormat.forPattern( "yyyy-MM-dd" ).getParser(),
                DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss" ).getParser(),
                DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss.SSS" ).getParser(),
                DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss.SSSSSS" ).getParser()};

        DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

        return formatter.parseDateTime(dateTime);
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
                    return getString(R.string.pager_all);
                case 1:
                    return getString(R.string.pager_active);
                case 2:
                    return getString(R.string.pager_delivered);
                default:
                    return getString(R.string.pager_invalid);
            }

        }
    }
}
