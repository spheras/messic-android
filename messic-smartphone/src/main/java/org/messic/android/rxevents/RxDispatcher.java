/*
 * Copyright (C) 2013
 *
 *  This file is part of Messic.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.messic.android.rxevents;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

public class RxDispatcher {

    private static RxDispatcher instance;
    private RxBus _rxBus = null;

    private RxDispatcher(RxBus bus) {
        this._rxBus = bus;
    }

    public static RxDispatcher get() {
        if (instance == null) {
            instance = new RxDispatcher(new RxBus());
        }
        return instance;
    }

    public Subscription subscribe(final RxSubscriber subscriber) {
        // note that it is important to subscribe to the exact same _rxBus instance that was used to post the events
        return _rxBus.toObservable().observeOn(AndroidSchedulers.mainThread()).
                subscribe(
                        new Action1<Object>() {
                            @Override
                            public void call(Object event) {
                                if (event instanceof RxAction) {
                                    subscriber.call((RxAction) event);
                                } else {
                                    Timber.w("Received Event !RxAction... discarding");
                                }
                            }
                        }
                );
    }

    public void unsubscribe(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public void send(RxAction action) {
        this._rxBus.send(action);
    }

    public interface RxSubscriber {
        void call(RxAction event);
    }

}
