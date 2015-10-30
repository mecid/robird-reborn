package com.aaplab.robird.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.ui.activity.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import twitter4j.User;

/**
 * Created by majid on 20.07.15.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private Activity mActivity;
    private List<User> mUsers;
    private Account mAccount;

    public UserAdapter(Activity activity, Account account, List<User> users) {
        mActivity = activity;
        mAccount = account;
        mUsers = users;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserHolder(LayoutInflater.from(mActivity).inflate(R.layout.tweet_item, parent, false));
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        final User user = mUsers.get(position);

        holder.textView.setText(user.getDescription());
        holder.usernameTextView.setText(user.getScreenName());
        holder.fullNameTextView.setText(user.getName());
        holder.infoTextView.setText(user.getLocation());

        Picasso.with(mActivity)
                .load(user.getOriginalProfileImageURL())
                .into(holder.avatarImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileActivity.start(mActivity, mAccount, user.getScreenName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static final class UserHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.avatar)
        ImageView avatarImageView;

        @Bind(R.id.screen_name)
        TextView usernameTextView;

        @Bind(R.id.full_name)
        TextView fullNameTextView;

        @Bind(R.id.info)
        TextView infoTextView;

        @Bind(R.id.text)
        TextView textView;

        public UserHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
