package com.aaplab.robird;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.ContactModel;
import com.aaplab.robird.data.model.UserModel;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.TaskParams;

import java.util.List;

import twitter4j.User;

/**
 * Created by majid on 17.10.15.
 */
public final class AccountUpdateService extends GcmTaskService {

    @Override
    public int onRunTask(TaskParams taskParams) {
        final AccountModel accountModel = new AccountModel();
        final List<Account> accounts = accountModel.accounts().toBlocking().first();

        for (Account account : accounts) {
            final UserModel userModel = new UserModel(account, account.screenName());
            final User user = userModel.user().toBlocking().first();

            final Account updatedAccount = account.withMeta(
                    user.getName(),
                    user.getScreenName(),
                    user.getOriginalProfileImageURL(),
                    user.getProfileBannerMobileRetinaURL()
            );

            accountModel.update(updatedAccount).toBlocking().first();
            new ContactModel(account).update().toBlocking().first();
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    public static PeriodicTask create() {
        return new PeriodicTask.Builder()
                .setService(AccountUpdateService.class)
                .setTag("account_update")
                .setUpdateCurrent(true)
                .setPeriod(24 * 3600)
                .setPersisted(true)
                .build();
    }
}
