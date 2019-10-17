package com.dah.dah;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dah.dah.Common.Common;
import com.dah.dah.Model.DataMessage;
import com.dah.dah.Model.FCMResponse;
import com.dah.dah.Model.Token;
import com.dah.dah.Remote.IFCMService;
import com.dah.dah.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {

    TextView txtTime, txtDistance, txtAddress, txtCountdown;
    Button btnAccept, btnCancel;
    MediaPlayer mediaPlayer;
    IGoogleAPI mService;
    String customerId;
    IFCMService mFCMService;
    String lat, lng;
    CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custommer_call);

        mService = Common.getGoogleAPI();

        mFCMService = Common.getFCMService();

        if (getIntent() != null) {
            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customer");

            getDirection(lat, lng);
        }

        txtAddress = findViewById(R.id.txtAddress);
        txtDistance = findViewById(R.id.txtDistance);
        txtTime = findViewById(R.id.txtTime);
        txtCountdown= findViewById(R.id.txt_count_down);

        btnAccept = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnDecline);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                Intent intent = new Intent(CustomerCall.this, DriverTracking.class);
                intent.putExtra("late", lat);
                intent.putExtra("lnge", lng);
                intent.putExtra("customerId", customerId);
                startActivity(intent);
                finish();


            }
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        startTimer();

    }

    private void startTimer() {
     countDownTimer =   new CountDownTimer(31000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                txtCountdown.setText(String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                if (!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
                else
                    Toast.makeText(CustomerCall.this,"Customer id must not be null",Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);
//        Notification notification = new Notification("Cancel","Driver Has Cancelled Your Request");
//        Sender sender = new Sender(notification, token.getToken());
        Map<String, String> content = new HashMap<>();
        content.put("title", "Cancel");
        content.put("message", "Driver Has Cancelled Your Request");
        DataMessage dataMessage = new DataMessage(token.getToken(), content);


        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1) {
                            Toast.makeText(CustomerCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    private void getDirection(String lat, String lng) {


        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + Common.mLastLocation.getLatitude() + "," + Common.mLastLocation.getLongitude() + "&" +
                    "destination=" + lat + "," + lng + "&" + "key=" + getResources().getString(R.string.google_direction_api_CALL);

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                assert response.body() != null;
                                JSONObject jsonObject = new JSONObject(response.body());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                JSONObject object = routes.getJSONObject(0);
                                JSONArray legs = object.getJSONArray("legs");
                                JSONObject legsObject = legs.getJSONObject(0);


                                JSONObject distance = legsObject.getJSONObject("distance");
                                txtDistance.setText(distance.getString("text"));


                                JSONObject time = legsObject.getJSONObject("duration");
                                txtTime.setText(time.getString("text"));


                                String address = legsObject.getString("end_address");
                                txtAddress.setText(address);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustomerCall.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        if (mediaPlayer.isPlaying())
        mediaPlayer.release();
        countDownTimer.cancel();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
//        countDownTimer.p
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying())
            mediaPlayer.start();
    }
}
