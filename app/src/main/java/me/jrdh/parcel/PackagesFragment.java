package me.jrdh.parcel;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.jrdh.parcel.api.models.Shipment;

public class PackagesFragment extends Fragment {

    @BindView(R.id.packages_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.packages_recycler_view_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView.LayoutManager layoutManager;

    private List<Shipment> shipments;

    public void setShipments(List<Shipment> shipments) {
        this.shipments = shipments;
    }

    public static PackagesFragment newInstance(List<Shipment> shipments) {
        PackagesFragment fragment = new PackagesFragment();

        fragment.setShipments(shipments);

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public PackagesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_packages, container, false);

        ButterKnife.bind(this, view);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new PackagesAdapter(shipments));

        registerForContextMenu(recyclerView);

        swipeRefreshLayout.setOnRefreshListener(this::sendRefreshEvent);

        return view;
    }

    void sendRefreshEvent() {
        swipeRefreshLayout.setRefreshing(false);
        ((ParcelActivity)getActivity()).getRxBusSingleton().send(new RefreshShipmentsEvent());
    }

    void updateShipments(List<Shipment> updatedShipments) {
        shipments = updatedShipments;

        PackagesAdapter newAdapter = new PackagesAdapter(updatedShipments);

        if (recyclerView == null)
            Log.e("Parcel", "RECYCLER VIEW IS NULL!");

        recyclerView.swapAdapter(newAdapter, false);
        recyclerView.invalidate();
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    public class RefreshShipmentsEvent { }

}
