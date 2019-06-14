package com.example.activity_map;

import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;

public class GeoCoding {
    //　緯度、経度を表示する
    private static final String TAG = "GeoCoding";

    public static String geoCoding(Geocoder geocoder, String givenAddress){
        String addressToReturn=null;

        try{
            List<Address> addresses = geocoder.getFromLocationName(givenAddress,1);
            Address address = addresses.get(0);
            addressToReturn = address.getLatitude() + " " +address.getLongitude();
            return addressToReturn;
        }catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public static String reverseGeoCoding(Geocoder geocoder, double latitude, double longitude){
        String addressToReturn;
        try{
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            Address address = addresses.get(0);
            addressToReturn = address.getCountryName() + address.getAddressLine(0);

            return addressToReturn;
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}

