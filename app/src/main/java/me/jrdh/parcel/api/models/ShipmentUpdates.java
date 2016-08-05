package me.jrdh.parcel.api.models;

import java.util.LinkedList;
import java.util.List;

public class ShipmentUpdates {

    List<Shipment> shipments;

    public List<Shipment> getShipments () {
        return shipments;
    }

    public void setShipments (List<Shipment> shipments) {
        this.shipments = shipments;
    }

    public void add(Shipment shipment) {
        if (shipments == null) {
            shipments = new LinkedList<>();
        }
        shipments.add(shipment);
    }

}
