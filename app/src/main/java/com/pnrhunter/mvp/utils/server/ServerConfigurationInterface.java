package com.pnrhunter.mvp.utils.server;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface ServerConfigurationInterface {
    public Single<Boolean> checkSocialLoginIsEnabled();
}
