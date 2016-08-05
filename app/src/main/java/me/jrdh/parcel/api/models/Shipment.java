package me.jrdh.parcel.api.models;


import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Shipment {
    public List<Location> locations;
    public String label;
    public String trackingNumber;
    public String type;
    public boolean update;
    public String changeToken;
    public String dateAdded;
    public String dateExpected;
    public String dateUpdated;
    public int idNumber;

    public Shipment () {
        locations = new ArrayList<Location>();
    }

    public Shipment (List<Location> locations, String label, String trackingNumber, String type, boolean update, String changeToken, String dateAdded, String dateExpected, String dateUpdated, int idNumber) {
        this.locations = locations;
        this.label = label;
        this.trackingNumber= trackingNumber;
        this.type = type;
        this.update = update;
        this.changeToken = changeToken;
        this.dateAdded = dateAdded;
        this.dateExpected = dateExpected;
        this.dateUpdated = dateUpdated;
        this.idNumber = idNumber;
    }
}
