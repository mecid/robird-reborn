package com.aaplab.robird.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.TrendsModel;
import com.aaplab.robird.ui.adapter.TrendAdapter;
import com.aaplab.robird.util.DefaultObserver;

import java.util.ArrayList;
import java.util.List;

import icepick.Icicle;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.GeoLocation;
import twitter4j.Trend;

/**
 * Created by majid on 25.08.15.
 */
public class TrendsFragment extends BaseSwipeToRefreshRecyclerFragment implements LocationListener {
    private static final int PERMISSION_REQUEST_CODE = 118;

    public static TrendsFragment create(Account account) {
        final Bundle args = new Bundle();
        args.putParcelable("account", account);

        TrendsFragment fragment = new TrendsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Icicle
    ArrayList<Trend> mTrends;

    private LocationManager mLocationManager;
    private TrendsModel mTrendsModel;
    private Account mAccount;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mTrends = mTrends == null ? new ArrayList<Trend>() : mTrends;
        mAccount = getArguments().getParcelable("account");
        mTrendsModel = new TrendsModel(mAccount);

        if (savedInstanceState == null || mTrends.isEmpty()) {
            updateTrends();
        } else {
            setupTrends(mTrends);
        }
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        updateTrends();
    }

    private void setupTrends(ArrayList<Trend> trends) {
        mTrends = trends;
        mRecyclerView.setAdapter(new TrendAdapter(getActivity(), mAccount, mTrends));
    }

    private void updateTrends() {
        final int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            setRefreshing(true);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null)
                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
            else
                onLocationChanged(location);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        final int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(this);
            mSubscriptions.add(
                    mTrendsModel
                            .local(new GeoLocation(location.getLatitude(), location.getLongitude()))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultObserver<List<Trend>>() {
                                @Override
                                public void onNext(List<Trend> trends) {
                                    super.onNext(trends);
                                    setRefreshing(false);
                                    setupTrends(new ArrayList<>(trends));
                                }
                            })
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateTrends();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
