package com.a98labs.nearbybeaconsampleandroid;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    private GoogleApiClient mGoogleApiClient;
    private MessageListener mMessageListener;

    private List<Beacon> mNearbyBeacons;

    private FABProgressCircle scanCircle;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean isSubscribingToNearby = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_activity_main);

        scanCircle = (FABProgressCircle) findViewById(R.id.scanCircle);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.scan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSubscribingToNearby) {
                    unsubscribe();
                } else {
                    subscribe();
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.beacon_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mNearbyBeacons = new ArrayList<>();
        mAdapter = new BeaconsAdapter(this, mNearbyBeacons, null);
        mRecyclerView.setAdapter(mAdapter);

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.i(TAG, "Found Message: " + message.toString());

                if ("deviceInfo".equals(message.getType())) {
                    Beacon beacon = Beacon.fromDeviceInfoAttachmentType(message.getContent());

                    if (!mNearbyBeacons.contains(beacon)) {
                        mNearbyBeacons.add(beacon);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onBleSignalChanged(final Message message, final BleSignal bleSignal) {
                Log.i(TAG, "Message: " + message + " has new BLE signal information: " + bleSignal);

                if ("deviceInfo".equals(message.getType())) {
                    Beacon beacon = Beacon.fromDeviceInfoAttachmentType(message.getContent());

                    if (mNearbyBeacons.contains(beacon)) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onDistanceChanged(final Message message, final Distance distance) {
                Log.i(TAG, "Distance changed, message: " + message + ", new distance: " + distance);

                if ("deviceInfo".equals(message.getType())) {
                    Beacon beacon = Beacon.fromDeviceInfoAttachmentType(message.getContent());

                    if (mNearbyBeacons.contains(beacon)) {
                        beacon = mNearbyBeacons.get(mNearbyBeacons.indexOf(beacon));
                        beacon.setDistance(distance.getMeters());
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onLost(Message message) {
                Log.i(TAG, "onLost BLE device");

                if ("deviceInfo".equals(message.getType())) {
                    Beacon beacon = Beacon.fromDeviceInfoAttachmentType(message.getContent());

                    if (mNearbyBeacons.contains(beacon)) {
                        mNearbyBeacons.remove(beacon);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        };

        buildGoogleApiClient();
    }

    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API, new MessagesOptions.Builder()
                        .setPermissions(NearbyPermissions.BLE)
                        .build())
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.i(TAG, "onConnected GoogleApiClient");
        //subscribe();
    }

    private void subscribe() {
        Log.i(TAG, "subscribing to beacon messages");
        SubscribeCallback subscribeCallback = new SubscribeCallback() {
            @Override
            public void onExpired() {
                Log.i(TAG, "onExpired subscription");
            }
        };
        MessageFilter messageFilter = new MessageFilter.Builder()
                .includeAllMyTypes()
                .includeEddystoneUids("bdf519733e283ef0681b", null)
                .build();
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(Strategy.BLE_ONLY)
                .setFilter(messageFilter)
                .setCallback(subscribeCallback)
                .build();
        Nearby.Messages
                .subscribe(mGoogleApiClient, mMessageListener, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.i(TAG, "onResult subscription: " + status.toString());
                    }
                });

        Snackbar.make(findViewById(R.id.root),
                "Starting to look for beacons nearby...",
                Snackbar.LENGTH_SHORT).show();
        scanCircle.show();
        isSubscribingToNearby = true;
    }

    private void unsubscribe() {
        Log.i(TAG, "unsubscribing from beacon messages");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);

        Snackbar.make(findViewById(R.id.root),
                "Stopped looking for beacons nearby...",
                Snackbar.LENGTH_SHORT).show();
        scanCircle.hide();
        isSubscribingToNearby = false;
        mNearbyBeacons.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(TAG, "onConnectionSuspended GoogleApiClient with cause: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            Log.e(TAG, "onConnectionFailed GoogleApiClient");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                Log.e(TAG, "onActivityResult GoogleApiClient connection failed to resolve.");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStop() {
        unsubscribe();
        super.onStop();
    }

}
