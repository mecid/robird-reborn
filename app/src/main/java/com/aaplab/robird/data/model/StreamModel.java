package com.aaplab.robird.data.model;

import android.content.ContentValues;
import android.net.Uri;

import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Direct;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.provider.contract.DirectContract;
import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.inject.Inject;

import timber.log.Timber;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;
import twitter4j.UserStreamListener;

public final class StreamModel extends BaseTwitterModel {
    private final TwitterStream mTwitterStream;
    private final SqlBriteContentProvider mSqlBriteContentProvider;

    public StreamModel(Account account) {
        super(account);

        mTwitterStream = new TwitterStreamFactory().getInstance(mTwitter.getAuthorization());
        mSqlBriteContentProvider = SqlBriteContentProvider.create(Inject.contentResolver());
    }

    public void start() {
        if (mTwitterStream != null) {
            mTwitterStream.addListener(new UserStatusListener());
            mTwitterStream.user();
        }
    }

    public void stop() {
        if (mTwitterStream != null) {
            // remove reference to listener
            mTwitterStream.clearListeners();

            // shutdown stream consuming thread
            mTwitterStream.cleanUp();

            // shutdown all TwitterStream threads.
            mTwitterStream.shutdown();
        }
    }


    private class UserStatusListener implements UserStreamListener {

        private void saveStatus(Status status, long timelineId) {
            Tweet tweet = Tweet.from(status);

            ContentValues contentValues = tweet.toContentValues();
            contentValues.put(TweetContract.ACCOUNT_ID, mAccount.id());
            contentValues.put(TweetContract.TIMELINE_ID, timelineId);

            Uri uri = mSqlBriteContentProvider
                    .insert(TweetContract.CONTENT_URI, contentValues)
                    .toBlocking()
                    .first();

            Timber.d("Inserted new tweet with URI=%s", uri.toString());
        }

        private void deleteStatus(long statusId, long timelineId) {
            Integer tweetsDeleted = mSqlBriteContentProvider.delete(TweetContract.CONTENT_URI,
                    String.format("%s=%d AND %s=%d AND %s=%d",
                            TweetContract.TWEET_ID, statusId,
                            TweetContract.ACCOUNT_ID, mAccount.id(),
                            TweetContract.TIMELINE_ID, timelineId), null)
                    .toBlocking()
                    .first();

            Timber.d("Deleting tweet with id=%d; Removal status: %d",
                    statusId, tweetsDeleted);
        }

        private long getTimelineId(Status status) {
            if (isRetweeted(status)) {
                return TimelineModel.RETWEETS_ID;
            }

            if (isCurrentUserMentioned(status.getUserMentionEntities())) {
                return TimelineModel.MENTIONS_ID;
            }

            return TimelineModel.HOME_ID;
        }

        private boolean isRetweeted(Status status) {
            return status.isRetweet() && status.getRetweetedStatus()
                    .getUser().getId() == mAccount.userId();
        }

        private boolean isCurrentUserMentioned(UserMentionEntity[] mentions) {
            if (mentions == null || mentions.length <= 0) {
                return false;
            }

            for (UserMentionEntity mention : mentions) {
                if (mention.getId() == mAccount.userId()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public void onStatus(Status status) {
            Timber.d("onStatus @%s - %s", status.getUser().getScreenName(), status.getText());
            saveStatus(status, getTimelineId(status));
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            Timber.d("Got a status deletion notice id: %s", statusDeletionNotice.getStatusId());
            deleteStatus(statusDeletionNotice.getStatusId(), TimelineModel.HOME_ID);
        }

        @Override
        public void onException(Exception ex) {
            Timber.d(ex, "StreamModel exception");
        }

        @Override
        public void onFavorite(User source, User target, Status favoritedStatus) {
            if (source.getId() == mAccount.userId()) {
                Timber.d("onFavorite source:@%s target:@%s @%s - %s",
                        source.getScreenName(), target.getScreenName(),
                        favoritedStatus.getUser().getScreenName(), favoritedStatus.getText());

                saveStatus(favoritedStatus, TimelineModel.FAVORITES_ID);
            }
        }

        @Override
        public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
            if (source.getId() == mAccount.userId()) {
                Timber.d("onUnFavorite source:@%s target:@%s @%s - %s",
                        source.getScreenName(), target.getScreenName(),
                        unfavoritedStatus.getUser().getScreenName(), unfavoritedStatus.getText());

                deleteStatus(unfavoritedStatus.getId(), TimelineModel.FAVORITES_ID);
            }
        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) {
            Direct direct = Direct.from(directMessage);

            ContentValues contentValues = direct.toContentValues();
            contentValues.put(DirectContract.ACCOUNT_ID, mAccount.id());

            Uri uri = mSqlBriteContentProvider
                    .insert(DirectContract.CONTENT_URI, contentValues)
                    .toBlocking()
                    .first();

            Timber.d("Inserted new direct message with URI=%s", uri.toString());
        }

        @Override
        public void onDeletionNotice(long directMessageId, long userId) {
            System.out.println("Got a direct message deletion notice id:" + directMessageId);
            System.out.println("UserID and AccountId:" + userId + " | " + mAccount.id());

            Integer directMessagesDeleted = mSqlBriteContentProvider.delete(TweetContract.CONTENT_URI,
                    String.format("%s=%d AND %s=%d",
                            DirectContract.DIRECT_ID, directMessageId,
                            DirectContract.ACCOUNT_ID, mAccount.id()), null)
                    .toBlocking()
                    .first();

            Timber.d("Deleting direct message with id=%d; Removal status: %d",
                    directMessageId, directMessagesDeleted);
        }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Stubs
        ///////////////////////////////////////////////////////////////////////////////////////////

        @Override
        public final void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        }

        @Override
        public final void onScrubGeo(long userId, long upToStatusId) {
        }

        @Override
        public final void onStallWarning(StallWarning warning) {
        }

        @Override
        public void onFriendList(long[] friendIds) {
        }

        @Override
        public void onFollow(User source, User followedUser) {
        }

        @Override
        public void onUnfollow(User source, User followedUser) {
        }

        @Override
        public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
        }

        @Override
        public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
        }

        @Override
        public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
        }

        @Override
        public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
        }

        @Override
        public void onUserListCreation(User listOwner, UserList list) {
        }

        @Override
        public void onUserListUpdate(User listOwner, UserList list) {
        }

        @Override
        public void onUserListDeletion(User listOwner, UserList list) {
        }

        @Override
        public void onUserProfileUpdate(User updatedUser) {
        }

        @Override
        public void onUserDeletion(long deletedUser) {
        }

        @Override
        public void onUserSuspension(long suspendedUser) {
        }

        @Override
        public void onBlock(User source, User blockedUser) {
        }

        @Override
        public void onUnblock(User source, User unblockedUser) {
        }

        @Override
        public void onRetweetedRetweet(User source, User target, Status retweetedStatus) {
        }

        @Override
        public void onFavoritedRetweet(User source, User target, Status favoritedRetweet) {
        }

        @Override
        public void onQuotedTweet(User source, User target, Status quotingTweet) {
        }
    }
}
