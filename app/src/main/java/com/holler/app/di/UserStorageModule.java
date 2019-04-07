package com.holler.app.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class UserStorageModule {

    @Singleton
    @Provides
    public UserStorage provideUserStorage(SharedPreferencesModule.SharedPreferencesHelper storage) {

        return new UserStorage(storage);
    }


    public class UserStorage {
        private static final String FIELD_ID = "id";
        private static final String FIELD_FIRST_NAME = "first_name";
        private static final String FIELD_LAST_NAME = "last_name";
        private static final String FIELD_EMAIL = "email";
        private static final String FIELD_SOS = "sos";
        private static final String FIELD_PICTURE = "picture";
        private static final String FIELD_GENDER = "gender";
        private static final String FIELD_MOBILE = "mobile";
        private static final String FIELD_APPROVAL_STATUS = "approval_status";
        private static final String FIELD_CURRENCY = "currency";
        private static final String FIELD_SERVICE_NAME = "service";

        private static final String FIELD_LOGGED_IN = "loggedIn";

        private SharedPreferencesModule.SharedPreferencesHelper storage;

        public UserStorage(SharedPreferencesModule.SharedPreferencesHelper storage) {
            this.storage = storage;
        }

        public User getUser() {
            User user = new User();
            storage.put(FIELD_ID,user.id);
            storage.put(FIELD_FIRST_NAME,user.firstName);
            storage.put(FIELD_LAST_NAME,user.lastName);
            storage.put(FIELD_EMAIL,user.email);
            storage.put(FIELD_SOS,user.sos);
            storage.put(FIELD_PICTURE,user.avatar);
            storage.put(FIELD_GENDER,user.gender);
            storage.put(FIELD_MOBILE,user.mobile);
            storage.put(FIELD_APPROVAL_STATUS,user.status);
            storage.put(FIELD_CURRENCY,user.currency);
            storage.put(FIELD_SERVICE_NAME,user.service.name);

            return user;
        }

        public void putUser(User user) {

        }
    }

}
