package com.dah.dah.Common;

import android.location.Location;

import com.dah.dah.Model.UberDriver;
import com.dah.dah.Remote.FCMClient;
import com.dah.dah.Remote.IFCMService;
import com.dah.dah.Remote.IGoogleAPI;
import com.dah.dah.Remote.RetrofitClient;

public class Common {

    public static final String driver_tb1 = "Drivers";
    public static final String user_driver_tb1 = "UsersInformation";
    public static final String token_tb1 = "Tokens";


    public static final int PICK_IMAGE_REQUEST = 999 ;

    public static UberDriver currentUser;

    public static Location mLastLocation=null;


    public static final String baseURL = "https://maps.googleapis.com/";
    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static final String user_field = "usr";

    public static double base_fare = 2.55;
    private static  double time_rate = 0.35;
    private static double distance_rate = 1.75;

    public static double formulaPrice(double km,double min)
    {
        return base_fare+(distance_rate*km)+(time_rate*min);
    }


    public static IGoogleAPI getGoogleAPI()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
