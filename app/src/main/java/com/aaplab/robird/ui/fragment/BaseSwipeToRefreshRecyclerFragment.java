package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaplab.robird.R;
import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by majid on 19.01.15.
 */
public abstract class BaseSwipeToRefreshRecyclerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, AppBarLayout.OnOffsetChangedListener {

    @Bind(R.id.refresh)
    SwipeRefreshLayout mRefreshLayout;

    @Bind(R.id.recycler)
    RecyclerView mRecyclerView;

    protected LinearLayoutManager mLayoutManager;
    private AppBarLayout mAppBar;

    private boolean mKeepOnAppending = true;
    private boolean mBottomLoading = false;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new ScrollListener());
        mRefreshLayout.setColorSchemeResources(R.color.primary);
        mRefreshLayout.setOnRefreshListener(this);
        mAppBar = ButterKnife.findById(getActivity(), R.id.app_bar);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAppBar != null)
            mAppBar.addOnOffsetChangedListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAppBar != null)
            mAppBar.removeOnOffsetChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_swipe_to_refresh_recycler, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        mRefreshLayout.setEnabled(i == 0);
    }

    private final class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE)
                Glide.with(getActivity()).resumeRequestsRecursive();
            else
                Glide.with(getActivity()).pauseRequestsRecursive();
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
            int visibleItemCount = mLayoutManager.findLastVisibleItemPosition() - firstVisibleItem;

            if (totalItemCount > 0 && mKeepOnAppending) {
                if (!mBottomLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 10)) {
                    mBottomLoading = true;
                    startBottomLoading();
                }
            }
        }
    }

    protected void setRefreshing(final boolean refreshing) {
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(refreshing);
            }
        });
    }

    protected void stopBottoLoading(boolean keepOnAppending) {
        mBottomLoading = false;
        mKeepOnAppending = keepOnAppending;
    }

    public void startBottomLoading() {
        stopBottoLoading(false);
    }

    @Override
    public void onRefresh() {

    }
}
