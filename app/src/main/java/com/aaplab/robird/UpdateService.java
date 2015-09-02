package com.aaplab.robird;

import android.app.IntentService;
import android.content.Intent;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.UserList;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.DirectsModel;
import com.aaplab.robird.data.model.TimelineModel;
import com.aaplab.robird.data.model.UserListsModel;

import java.util.List;

/**
 * Created by majid on 02.09.15.
 */
public final class UpdateService extends IntentService {
    public static final String NAME = "UpdateService";

    public UpdateService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        for (Account account : new AccountModel().accounts().toBlocking().first()) {
            new TimelineModel(account, TimelineModel.HOME_ID).update().toBlocking().first();
            new TimelineModel(account, TimelineModel.MENTIONS_ID).update().toBlocking().first();
            new TimelineModel(account, TimelineModel.RETWEETS_ID).update().toBlocking().first();
            new TimelineModel(account, TimelineModel.FAVORITES_ID).update().toBlocking().first();
            new UserListsModel(account).update().toBlocking().first();
            new DirectsModel(account).update().toBlocking().first();

            List<UserList> userLists = new UserListsModel(account).lists().toBlocking().first();
            for (UserList userList : userLists)
                new TimelineModel(account, userList.listId()).update().toBlocking().first();
        }
    }
}
