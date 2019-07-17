package com.pnrhunter.utils;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class ReactiveServiceBindingFactory {

    public <B extends Binder> Observable<B> bind(final Context context, final Intent launch) {
        return bind(context, launch, Service.BIND_AUTO_CREATE);
    }

    private <B extends Binder> Observable<B> bind(final Context context, final Intent launch, final int flags) {
        return Observable.using(
                Connection::new,
                (final Connection<B> connection) -> {
                    context.bindService(launch, connection, flags);
                    return Observable.create(connection);
                },
                context::unbindService);
    }

    private static class Connection<B extends Binder> implements ServiceConnection, ObservableOnSubscribe<B> {

        private ObservableEmitter<? super B> subscriber;
        private B service;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (this.subscriber==null){
                this.service = (B)service;
            }else{
                subscriber.onNext((B) service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (subscriber != null) {
                subscriber.onComplete();
            }
        }

        @Override
        public void subscribe(ObservableEmitter<B> emitter) throws Exception {
            this.subscriber = emitter;
            if(this.service!=null){
                subscriber.onNext(this.service);
            }
        }
    }

}
