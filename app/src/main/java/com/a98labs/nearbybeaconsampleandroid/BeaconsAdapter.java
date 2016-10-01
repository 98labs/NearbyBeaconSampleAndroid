package com.a98labs.nearbybeaconsampleandroid;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static java.lang.Double.NaN;

public class BeaconsAdapter extends RecyclerView.Adapter<BeaconsAdapter.ViewHolder> {
    private static final String TAG = BeaconsAdapter.class.getSimpleName();

    private Context mContext;
    private List<Beacon> mNearbyBeacons;
    private OnBeaconItemClickListener mOnBeaconItemClickListener;

    public interface OnBeaconItemClickListener {
        void onBeaconItemClick(Beacon beacon);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView advertisedId, advertisedType, distance;
        PulsatorLayout distancePulsator;

        public ViewHolder(View view) {
            super(view);

            cardView = (CardView) view.findViewById(R.id.beacon_card_view);
            advertisedId = (TextView) view.findViewById(R.id.advertised_id);
            advertisedType = (TextView) view.findViewById(R.id.advertised_type);
            distance = (TextView) view.findViewById(R.id.distance);
            distancePulsator = (PulsatorLayout) view.findViewById(R.id.signalPulse);
        }
    }

    public BeaconsAdapter(Context context,
                          List<Beacon> nearbyBeacons,
                          OnBeaconItemClickListener onBeaconItemClickListener) {
        mContext = context;
        mNearbyBeacons = nearbyBeacons;
        mOnBeaconItemClickListener = onBeaconItemClickListener;
    }

    @Override
    public BeaconsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.beacon_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Beacon beacon = mNearbyBeacons.get(position);
        holder.advertisedId.setText(beacon.getAdvertisedId());
        holder.advertisedType.setText(beacon.getAdvertisedType());
        if (beacon.getDistance() != NaN) {
            holder.distance.setText(String.valueOf(beacon.getDistance() + "m"));
            holder.distancePulsator.setCount(getPulseCountFromDistance(beacon.getDistance()));
            holder.distancePulsator.start();
        } else {
            holder.distance.setText("unknown");
            holder.distancePulsator.stop();
        }
        if(mOnBeaconItemClickListener != null) {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnBeaconItemClickListener.onBeaconItemClick(mNearbyBeacons.get(position));
                }
            });
        }
    }

    private int getPulseCountFromDistance(double distance) {
        int pulseCount = 0;


        if(distance != NaN) {
            double diff = 7 - (distance * 100);
            if(diff < 0) {
                diff = 1;
            }
            pulseCount = new Double(diff).intValue();
        }

        Log.i(TAG, "distance: " + distance + ", pulseCount: " + pulseCount);

        return pulseCount;
    }

    @Override
    public int getItemCount() {
        return mNearbyBeacons.size();
    }
}