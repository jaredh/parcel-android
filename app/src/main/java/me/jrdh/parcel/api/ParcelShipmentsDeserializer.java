package me.jrdh.parcel.api;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import me.jrdh.parcel.api.models.Location;
import me.jrdh.parcel.api.models.Shipment;
import me.jrdh.parcel.api.models.ShipmentUpdates;

public class ParcelShipmentsDeserializer implements JsonDeserializer<ShipmentUpdates> {
    List<Shipment> shipments;

    @Override
    public ShipmentUpdates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ShipmentUpdates updates = new ShipmentUpdates();
        shipments = new ArrayList<>();
        JsonArray jsonArray = json.getAsJsonArray();
        traverseJson(jsonArray);
        updates.setShipments(shipments);

        return updates;
    }

    private void traverseJson (JsonArray jArray) {
        for (JsonElement element : jArray.get(0).getAsJsonArray()) {
            if (element instanceof JsonArray) {
                shipments.add(parseShipment((JsonArray)element));
            }
        }
    }

    private Shipment parseShipment (JsonArray jArray) {
        Shipment shipment = new Shipment();

        shipment.trackingNumber = jArray.get(0).getAsString();
        shipment.label = jArray.get(1).getAsString();
        shipment.type = jArray.get(2).getAsString();
        if (jArray.get(3).getAsString().equalsIgnoreCase("no"))
            shipment.update = false;
        else
            shipment.update = true;

        //Log.d("Parcel", "Package: " + shipment.label + " Delivered: " + jArray.get(3).getAsString());

        parseStatusUpdates(shipment, jArray.get(4).getAsJsonArray());

        return shipment;
    }

    private void parseStatusUpdates (Shipment shipment, JsonArray jArray) {
        for (JsonElement statusElement : jArray) {
            if (!(statusElement instanceof JsonArray))
                continue;

            JsonArray updateList = statusElement.getAsJsonArray();

            Location location = new Location();

            location.additional = getStringOrNullAsString(updateList.get(0));
            location.date = getStringOrNullAsString(updateList.get(1));
            location.postcode = getStringOrNullAsString(updateList.get(2));
            location.address = getStringOrNullAsString(updateList.get(3));
            location.operation = getStringOrNullAsString(updateList.get(4));
            shipment.locations.add(location);
        }
    }

    private static String getStringOrNullAsString (JsonElement element) {
        if (element instanceof JsonPrimitive)
            return element.getAsString();
        else
            return "";
    }
}
