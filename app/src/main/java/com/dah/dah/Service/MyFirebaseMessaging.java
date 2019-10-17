package com.dah.dah.Service;

import android.content.Intent;
import android.util.Log;

import com.dah.dah.Common.Common;
import com.dah.dah.CustomerCall;
import com.dah.dah.Model.Token;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        updateTokenToServer(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData()!=null) {
            Map<String,String> data = remoteMessage.getData();
            String customer = data.get("customer");
            String lat = data.get("lat");
            String lng = data.get("lng");

            Intent intent = new Intent(getBaseContext(), CustomerCall.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            intent.putExtra("customer", customer);

            startActivity(intent);
        }
    }

    private void updateTokenToServer(String refreshedToken) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference tokens = db.getReference(Common.token_tb1);

        final Token token = new Token(refreshedToken);

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                tokens.child(account.getId())
                        .setValue(token);

            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.d("ERROR_ACCOUNT_KIT",accountKitError.getUserFacingMessage());
            }
        });

    }
}