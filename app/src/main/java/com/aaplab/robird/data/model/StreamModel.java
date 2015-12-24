package com.aaplab.robird.data.model;

import android.content.ContentValues;
import android.net.Uri;

import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
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
        }
    }

    private class UserStatusListener implements UserStreamListener {

        private boolean isCurrentUserMentioned(UserMentionEntity[] entities) {
            if (entities == null || entities.length <= 0) {
                return false;
            }

            for (UserMentionEntity mention : entities) {
                if (mention.getId() == mAccount.userId()) {
                    return true;
                }
            }

            return false;
        }

        private boolean isRetweeted(Status status) {
            return status.isRetweeted() && status.getRetweetedStatus()
                    .getUser().getId() == mAccount.userId();
        }

        private long getTimelineId(Status status) {
            if (isCurrentUserMentioned(status.getUserMentionEntities())) {
                System.out.println("Mentioned");
                return TimelineModel.MENTIONS_ID;
            }

            if (isRetweeted(status)) {
                System.out.println("Retweeted");
                return TimelineModel.RETWEETS_ID;
            }

            System.out.println("Home");
            return TimelineModel.HOME_ID;
        }

        private void saveStatus(Status status, long timelineId) {
            Tweet tweet = Tweet.from(status);

            ContentValues contentValues = tweet.toContentValues();
            contentValues.put(TweetContract.ACCOUNT_ID, mAccount.id());
            contentValues.put(TweetContract.TIMELINE_ID, timelineId);

            Uri uri = mSqlBriteContentProvider
                    .insert(TweetContract.CONTENT_URI, contentValues)
                    .toBlocking()
                    .first();

            Timber.d("Inserted new tweet with URI=" + uri.toString());
        }

        private void deleteStatus(long statusId, long timelineId) {
            Integer tweetsDeleted = mSqlBriteContentProvider.delete(TweetContract.CONTENT_URI,
                    String.format("%s=%d AND %s=%d AND %s=%d",
                            TweetContract.TWEET_ID, statusId,
                            TweetContract.ACCOUNT_ID, mAccount.id(),
                            TweetContract.TIMELINE_ID, timelineId), null)
                    .toBlocking()
                    .first();

            Timber.d(String.format("Deleting tweet with id=%d; Removal status: %d",
                    statusId, tweetsDeleted));
        }

        @Override
        public void onStatus(Status status) {
            Timber.d("onStatus @" + status.getUser().getScreenName() + " - " + status.getText());
            saveStatus(status, getTimelineId(status));
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            Timber.d("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            deleteStatus(statusDeletionNotice.getStatusId(), TimelineModel.HOME_ID);
        }

        @Override
        public void onException(Exception ex) {
            Timber.d(ex, "StreamModel exception");
        }

        @Override
        public void onFavorite(User source, User target, Status favoritedStatus) {
            if (source.getId() == mAccount.userId()) {
                Timber.d("onFavorite source:@"
                        + source.getScreenName() + " target:@"
                        + target.getScreenName() + " @"
                        + favoritedStatus.getUser().getScreenName() + " - "
                        + favoritedStatus.getText());

                saveStatus(favoritedStatus, TimelineModel.FAVORITES_ID);
            }
        }

        @Override
        public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
            if (source.getId() == mAccount.userId()) {
                Timber.d("onUnFavorite source:@"
                        + source.getScreenName() + " target:@"
                        + target.getScreenName() + " @"
                        + unfavoritedStatus.getUser().getScreenName()
                        + " - " + unfavoritedStatus.getText());

                deleteStatus(unfavoritedStatus.getId(), TimelineModel.FAVORITES_ID);
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // TODO Directs
        ///////////////////////////////////////////////////////////////////////////////////////////

        @Override
        public void onDirectMessage(DirectMessage directMessage) {
            System.out.println("onDirectMessage text:"
                    + directMessage.getText());
        }

        @Override
        public void onDeletionNotice(long directMessageId, long userId) {
            System.out.println("Got a direct message deletion notice id:" + directMessageId);
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
