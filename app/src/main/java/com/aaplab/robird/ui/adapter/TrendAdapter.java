package com.aaplab.robird.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.ui.activity.SearchActivity;

import java.util.List;

import twitter4j.Trend;

/**
 * Created by majid on 25.08.15.
 */
public class TrendAdapter extends RecyclerView.Adapter {

    private Activity activity;
    private Account account;
    private List<Trend> trends;

    public TrendAdapter(Activity activity, Account account, List<Trend> trends) {
        this.activity = activity;
        this.account = account;
        this.trends = trends;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(activity).inflate(R.layout.single_line_item, parent, false)) {
        };
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((TextView) holder.itemView).setText(trends.get(position).getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchActivity.start(activity, account, trends.get(position).getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return trends.size();
    }
}
