package me.jrdh.parcel;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.jrdh.parcel.api.models.Shipment;

public class PackagesAdapter extends RecyclerView.Adapter<PackagesAdapter.ViewHolder> {

    private List<Shipment> shipments = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.packageStatusImageView)
        ImageView packageStatusImageView;

        @BindView(R.id.packageNameTextView)
        TextView packageNameTextView;

        @BindView(R.id.packageStatusTextView)
        TextView packageStatusTextView;

        public void setShipment(Shipment shipment) {
            this.shipment = shipment;
        }

        Shipment shipment;

        public ViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

            v.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Log.d("Parcel", "Package selected: " + packageNameTextView.getText());
            Intent intent = new Intent(v.getContext(), PackageDetailsActivity.class);
            intent.putExtra(Shipment.class.getName(), Parcels.wrap(shipment));
            v.getContext().startActivity(intent);
        }
    }

    public PackagesAdapter(List<Shipment> shipments) {
        this.shipments = shipments;
    }

    @Override
    public PackagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.package_layout, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setShipment(shipments.get(position));

        holder.packageNameTextView.setText(shipments.get(position).label);
        holder.packageStatusTextView.setText(shipments.get(position).locations.get(shipments.get(position).locations.size() - 1).additional);

        if (shipments.get(position).update)
            holder.packageStatusImageView.setImageResource(R.drawable.ic_local_shipping_black_24dp);
        else
            holder.packageStatusImageView.setImageResource(R.drawable.delivered);
    }

    @Override
    public int getItemCount() {
        return shipments.size();
    }
}
