package com.dah.dah;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dah.dah.Common.Common;
import com.dah.dah.Model.Token;
import com.dah.dah.Model.UberDriver;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    Button btnContinue;
    RelativeLayout rootLayout;
    FirebaseDatabase db;
    DatabaseReference users;
    private int REQUEST_CODE = 1000;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);

        Paper.init(this);

        rootLayout = findViewById(R.id.rootLayout);
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_driver_tb1);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithPhone();
            }
        });

        //check login session
        if (AccountKit.getCurrentAccessToken() != null) {

            final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(this).build();
            ;
            waitingDialog.setCancelable(false);
            waitingDialog.setMessage("Please waiting...");
            waitingDialog.show();

            AccountKit.getCurrentAccount(new AccountKitCallback<com.facebook.accountkit.Account>() {
                @Override
                public void onSuccess(com.facebook.accountkit.Account account) {
                    users.child(account.getId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    Common.currentUser = dataSnapshot.getValue(UberDriver.class);
                                    updateTokenToServer();
                                    Intent homeIntent = new Intent(MainActivity.this, DriverHome.class);
                                    startActivity(homeIntent);
                                    waitingDialog.dismiss();
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("ERROR", databaseError.getMessage());
                                }
                            });

                }

                @Override
                public void onError(AccountKitError accountKitError) {
                    Log.d("ERROR", accountKitError.getUserFacingMessage());

                }
            });
        }

    }

    private void signInWithPhone() {

        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result.getError() != null) {
                Toast.makeText(this, "" + result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            } else if (result.getError() != null) {
                Toast.makeText(this, "Cancel login", Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (result.getAccessToken() != null) {
                    final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(this).build();
                    waitingDialog.setMessage("Please waiting.....");
                    waitingDialog.setCancelable(false);
                    waitingDialog.show();

                    AccountKit.getCurrentAccount(new AccountKitCallback<com.facebook.accountkit.Account>() {
                        @Override
                        public void onSuccess(final com.facebook.accountkit.Account account) {
                            users.orderByKey().equalTo(account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (!dataSnapshot.child(account.getId()).exists()) {
                                        final UberDriver user = new UberDriver();
                                        user.setPhone(account.getPhoneNumber().toString());
                                        user.setName(account.getPhoneNumber().toString());
                                        user.setAvatarUrl("");
                                        user.setRates("0.0");
                                        user.setCarType("x");

                                        users.child(account.getId())
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        users.child(account.getId())
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        Common.currentUser = dataSnapshot.getValue(UberDriver.class);
                                                                        updateTokenToServer();
                                                                        Intent homeIntent = new Intent(MainActivity.this, DriverHome.class);
                                                                        startActivity(homeIntent);

                                                                        waitingDialog.dismiss();
                                                                        finish();
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                        Log.d("ERROR", databaseError.getMessage());
                                                                    }
                                                                });


                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    } else {
                                        users.child(account.getId())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Common.currentUser = dataSnapshot.getValue(UberDriver.class);
                                                        updateTokenToServer();
                                                        Intent homeIntent = new Intent(MainActivity.this, DriverHome.class);
                                                        startActivity(homeIntent);

                                                        waitingDialog.dismiss();
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        Log.d("ERROR", databaseError.getMessage());
                                                    }
                                                });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("ERROR", databaseError.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Log.d("ERROR", accountKitError.getUserFacingMessage());
                            Toast.makeText(MainActivity.this, "" + accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    private void updateTokenToServer() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference tokens = db.getReference(Common.token_tb1);


        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Token token = new Token(instanceIdResult.getToken());
                        tokens.child(account.getId())
                                .setValue(token);
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("ERROR_TOKEN", e.getMessage());
                                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.d("ERROR_ACCOUNT_KIT", accountKitError.getUserFacingMessage());
            }
        });

    }


//    private void printKeyHash() {
//        try{
//            PackageInfo info = getPackageManager().getPackageInfo("com.dah.dah", PackageManager.GET_SIGNATURES);
//            for (Signature signature:info.signatures)
//            {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KEYHASH", Base64.encodeToString(md.digest(),Base64.DEFAULT));
//
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }
}


