package com.aaplab.robird.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Direct;
import com.aaplab.robird.ui.activity.DirectChatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by majid on 18.08.15.
 */
public class DirectsAdapter extends RecyclerView.Adapter<DirectsAdapter.DirectHolder> {

    private List<Direct> directs;
    private Activity activity;
    private Account account;
    private boolean clickable;

    public DirectsAdapter(Activity activity, Account account, List<Direct> directs, boolean clickable) {
        this.activity = activity;
        this.directs = directs;
        this.account = account;
        this.clickable = clickable;
    }

    @Override
    public DirectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DirectHolder(LayoutInflater.from(activity).inflate(R.layout.tweet_item, parent, false));
    }

    @Override
    public void onBindViewHolder(DirectHolder holder, int position) {
        final Direct direct = directs.get(position);

        String avatar = direct.avatar();
        String username = direct.userName();
        String fullName = direct.fullName();

        if (clickable) {
            avatar = TextUtils.equals(direct.recipient(), account.screenName()) ?
                    direct.avatar() : direct.recipientAvatar();

            username = TextUtils.equals(direct.recipient(), account.screenName()) ?
                    direct.userName() : direct.recipient();

            fullName = TextUtils.equals(direct.recipient(), account.screenName()) ?
                    direct.fullName() : direct.recipientFullName();
        }

        Glide.with(activity)
                .load(avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.avatarImageView);

        holder.infoTextView.setText(DateUtils.getRelativeTimeSpanString(direct.createdAt()));
        holder.screenNameTextView.setText("@" + username);
        holder.fullNameTextView.setText(fullName);
        holder.textView.setText(direct.text());

        if (clickable)
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String username = TextUtils.equals(direct.recipient(), account.screenName()) ?
                            direct.userName() : direct.recipient();
                    DirectChatActivity.start(activity, account, username);
                }
            });
    }

    @Override
    public int getItemCount() {
        return directs.size();
    }

    static final class DirectHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.avatar)
        ImageView avatarImageView;

        @Bind(R.id.text)
        TextView textView;

        @Bind(R.id.screen_name)
        TextView screenNameTextView;

        @Bind(R.id.full_name)
        TextView fullNameTextView;

        @Bind(R.id.info)
        TextView infoTextView;

        public DirectHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
