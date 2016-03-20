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
package org.messic.android.messiccore.util;

import android.os.AsyncTask;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class UtilNetwork {

    private static UtilNetwork instance;

    @Inject
    Configuration config;

    private UtilNetwork() {
        MessicCoreApp.getInstance().getComponent().inject(this);
    }

    public static UtilNetwork get() {
        if (instance == null) {
            instance = new UtilNetwork();
        }
        return instance;
    }

    public void checkMessicServerUpAndRunning(final MDMMessicServerInstance instance, Scheduler scheduler, final Action1<UtilNetwork.MessicServerConnectionStatus> onNext, final Action1<Throwable> onError, final Action0 onComplete) {
        Observable.create(
                new Observable.OnSubscribe<MessicServerConnectionStatus>() {
                    @Override
                    public void call(Subscriber<? super MessicServerConnectionStatus> subscriber) {
                        boolean reachable = isServerReachable(instance.ip);
                        if (reachable) {
                            boolean running = isMessicServerInstanceRunning(instance);
                            subscriber.onNext(new MessicServerConnectionStatus(reachable, running));
                        } else {
                            subscriber.onNext(new MessicServerConnectionStatus(false, false));
                        }
                        subscriber.onCompleted();
                    }
                }).subscribeOn(Schedulers.io()).observeOn(scheduler).subscribe(onNext, onError, onComplete);
    }

    public void checkMessicServerUpAndRunning(final MDMMessicServerInstance instance,
                                              final MessicServerStatusListener msl) {
        if (instance == null || msl == null) {
            return;
        }

        AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                boolean reachable = isServerReachable(instance.ip);
                if (reachable) {
                    boolean running = isMessicServerInstanceRunning(instance);
                    msl.setResponse(true, running);
                } else {
                    msl.setResponse(false, false);
                }
                return null;
            }

        };
        at.execute();
    }

    /**
     * Check if messic is running in the current configuration connection WARNING! this must be done in another thread
     *
     * @param instance MDMMessicServerInstance
     * @return
     */
    public boolean isMessicServerInstanceRunning(MDMMessicServerInstance instance) {
        try {
            nukeNetwork();
            String surl = config.getBaseUrl(instance) + "/services/check";
            URL url = new URL(surl);
            URLConnection urlConnection = url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            in.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Function to check if an ip is reachable from the device WARNING! this must be done in another thread
     *
     * @param ip String ip in the way (xxx.xxx.xxx.xxx)
     * @return boolean true->if it is reachable
     */
    public boolean isServerReachable(String ip) {
        try {
            boolean result = InetAddress.getByName(ip).isReachable(1000);
            return result;
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        }
        return false;
    }

    /**
     * Function to trust all certificates for https connections
     */
    public void nukeNetwork() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                    return myTrustedAnchors;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }

    public boolean check() {
        String surl =
                this.config.getBaseUrl() + "/services/check?messic_token=" + this.config.getLastToken();
        try {
            URL url = new URL(surl);
            url.openConnection();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public interface MessicServerStatusListener {
        void setResponse(boolean reachable, boolean running);
    }

    public static class MessicServerConnectionStatus {
        public boolean reachable;
        public boolean running;

        public MessicServerConnectionStatus(boolean reachable, boolean running) {
            this.reachable = reachable;
            this.running = running;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MessicServerConnectionStatus) {
                MessicServerConnectionStatus om = (MessicServerConnectionStatus) o;
                return (reachable == om.reachable && running == om.running);
            } else {
                return false;
            }
        }
    }


}
