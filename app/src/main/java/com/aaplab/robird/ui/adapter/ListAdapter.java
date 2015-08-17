package com.aaplab.robird.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.UserList;
import com.aaplab.robird.ui.activity.UserListActivity;

import java.util.List;

/**
 * Created by majid on 17.08.15.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListHolder> {

    private List<UserList> userLists;
    private Activity activity;
    private Account account;

    public ListAdapter(Activity activity, Account account, List<UserList> userLists) {
        this.userLists = userLists;
        this.activity = activity;
        this.account = account;
    }

    @Override
    public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ListHolder(LayoutInflater.from(activity).inflate(R.layout.single_line_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ListHolder holder, int position) {
        final UserList userList = userLists.get(position);
        ((TextView) holder.itemView).setText(userList.name());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserListActivity.start(activity, account, userList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userLists.size();
    }

    static final class ListHolder extends RecyclerView.ViewHolder {

        public ListHolder(View itemView) {
            super(itemView);
        }
    }
}
