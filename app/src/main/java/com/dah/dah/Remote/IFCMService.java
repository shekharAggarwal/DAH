package com.dah.dah.Remote;

import com.dah.dah.Model.DataMessage;
import com.dah.dah.Model.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAVpDUlPI:APA91bGex5XZbEeGNS_Itxmbs8j4skhtTt_ZAW7XiR1OQsNCxF6GIEjGOM_sDEswWVqKEApezk122U1lDf9MwnJtEc7XYFC9XL5ZNRMWMaK3Lqy7Eop3sa9BM_x0t6sYa3OWDhwRXJCV"
    })
    @POST("fcm/send")
    Call<FCMResponse>sendMessage(@Body DataMessage body);
}
