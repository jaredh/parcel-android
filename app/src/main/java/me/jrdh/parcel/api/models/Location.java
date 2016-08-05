package me.jrdh.parcel.api.models;


import org.parceler.Parcel;

@Parcel
public class Location {
    public String additional;
    public String address;
    public String date;
    public String operation;
    public String postcode;

    public Location () { }

    public Location (String additional, String address, String date, String operation, String postcode) {
        this.additional = additional;
        this.address = address;
        this.date = date;
        this.operation = operation;
        this.postcode = postcode;
    }
}
