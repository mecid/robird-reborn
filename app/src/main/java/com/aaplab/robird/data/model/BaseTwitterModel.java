package com.aaplab.robird.data.model;

import com.aaplab.robird.Config;
import com.aaplab.robird.data.entity.Account;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by majid on 10.05.15.
 */
public abstract class BaseTwitterModel {
    protected Twitter mTwitter;
    protected Account mAccount;

    public BaseTwitterModel(Account account) {
        mAccount = account;
        mTwitter = configure(account);
    }

    private static Twitter configure(Account account) {
        final ConfigurationBuilder builder = new ConfigurationBuilder();

        builder.setOAuthConsumerKey(Config.TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(Config.TWITTER_CONSUMER_SECRET);
        builder.setOAuthAccessTokenSecret(account.tokenSecret());
        builder.setOAuthAccessToken(account.token());

        return new TwitterFactory(builder.build()).getInstance();
    }
}
