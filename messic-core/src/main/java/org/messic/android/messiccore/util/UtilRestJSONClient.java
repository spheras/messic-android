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
import android.util.Log;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.util.UtilNetwork.MessicServerStatusListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import javax.inject.Inject;

public class UtilRestJSONClient {
    public static final int READ_TIME_OUT = 60 * 1000;

    public static final int CONNECTION_TIME_OUT = 10 * 1000;
    private static UtilRestJSONClient instance;
    @Inject
    Configuration config;

    private UtilRestJSONClient() {
        MessicCoreApp.getInstance().getComponent().inject(this);
    }

    public static UtilRestJSONClient get() {
        if (instance == null) {
            instance = new UtilRestJSONClient();
        }
        return instance;
    }

    /**
     * Synchronous Mode
     * Rest POST petition to the server at the url param, sending all the post parameters defiend at formData. This post
     * return an object (json marshalling) of class defined at clazz parameter. You should register a
     * {@link RestListener} in order to obtain the returned object, this is because the petition is done in an async
     * process.
     *
     * @param url      {@link String} URL to attack
     * @param formData {@link MultiValueMap}<?,?/> map of parameters to send
     * @param clazz    Class<T/> class that you will marshall to a json object
     */
    public <T> T post(final String url, MultiValueMap<?, ?> formData,
                      final Class<T> clazz) throws Exception {
        return _post(url, formData, clazz, null);
    }


    /**
     * Rest POST petition to the server at the url param, sending all the post parameters defiend at formData. This post
     * return an object (json marshalling) of class defined at clazz parameter. You should register a
     * {@link RestListener} in order to obtain the returned object, this is because the petition is done in an async
     * process.
     *
     * @param url      {@link String} URL to attack
     * @param formData {@link MultiValueMap}<?,?/> map of parameters to send
     * @param clazz    Class<T/> class that you will marshall to a json object
     * @param rl       {@link RestListener} listener to push the object returned
     */
    private <T> void post(final String url, MultiValueMap<?, ?> formData,
                          final Class<T> clazz, final RestListener<T> rl) throws Exception {
        _post(url, formData, clazz, rl);
    }

    /**
     * Rest POST petition to the server at the url param, sending all the post parameters defiend at formData. This post
     * return an object (json marshalling) of class defined at clazz parameter. You should register a
     * {@link RestListener} in order to obtain the returned object, this is because the petition is done in an async
     * process.
     *
     * @param url      {@link String} URL to attack
     * @param formData {@link MultiValueMap}<?,?/> map of parameters to send
     * @param clazz    Class<T/> class that you will marshall to a json object
     * @param rl       {@link RestListener} listener to push the object returned (if null, synchronous mode)
     */
    private <T> T _post(final String url, MultiValueMap<?, ?> formData,
                        final Class<T> clazz, final RestListener<T> rl) throws Exception {

        final RestTemplate restTemplate = new RestTemplate();
        // we set timeout
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        rf.setReadTimeout(READ_TIME_OUT);
        rf.setConnectTimeout(CONNECTION_TIME_OUT);

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        // Sending multipart/form-data
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // Populate the MultiValueMap being serialized and headers in an HttpEntity object to use for the request
        final HttpEntity<MultiValueMap<?, ?>> requestEntity =
                new HttpEntity<MultiValueMap<?, ?>>(formData, requestHeaders);


        if (rl != null) {
            AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        rl.response(postEntity(restTemplate, url, requestEntity, clazz));
                    } catch (Exception e) {
                        Log.e("UtilRestJSONClient", e.getMessage(), e);
                        e.printStackTrace();
                        // maybe a timeout!???
                        // is the server online yet??

                        UtilNetwork.get().checkMessicServerUpAndRunning(config.getCurrentMessicService(),
                                new MessicServerStatusListener() {
                                    public void setResponse(boolean reachable,
                                                            boolean running) {
                                        if (!running) {
                                            // @TODO remove comment
                                            config.logout();
                                        }
                                    }
                                });

                        rl.fail(e);
                    }
                    return null;
                }

            };

            at.execute();
            return null;
        } else {
            return postEntity(restTemplate, url, requestEntity, clazz);
        }
    }


    /**
     * Syncrhonous mode
     * Rest GET petition to the server at the url param, sending all the post parameters defiend at formData. This post
     * return an object (json marshalling) of class defined at clazz parameter. You should register a
     * {@link RestListener} in order to obtain the returned object, this is because the petition is done in an async
     * process.
     *
     * @param url   {@link String} URL to attack
     * @param clazz Class<T/> class that you will marshall to a json object
     */
    public <T> T get(final String url, final Class<T> clazz) {
        return _get(url, clazz, null);
    }

    /**
     * Rest GET petition to the server at the url param, sending all the post parameters defiend at formData. This post
     * return an object (json marshalling) of class defined at clazz parameter. You should register a
     * {@link RestListener} in order to obtain the returned object, this is because the petition is done in an async
     * process.
     *
     * @param url   {@link String} URL to attack
     * @param clazz Class<T/> class that you will marshall to a json object
     * @param rl    {@link RestListener} listener to push the object returned
     */
    public <T> void get(final String url, final Class<T> clazz, final RestListener<T> rl) {
        _get(url, clazz, rl);

    }

    /**
     * Rest GET petition to the server at the url param, sending all the post parameters defiend at formData. This post
     * return an object (json marshalling) of class defined at clazz parameter. You should register a
     * {@link RestListener} in order to obtain the returned object, this is because the petition is done in an async
     * process.
     *
     * @param url   {@link String} URL to attack
     * @param clazz Class<T/> class that you will marshall to a json object
     * @param rl    {@link RestListener} listener to push the object returned
     */
    private <T> T _get(final String url, final Class<T> clazz, final RestListener<T> rl) {
        final RestTemplate restTemplate = new RestTemplate();

        // we set timeout
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        rf.setReadTimeout(READ_TIME_OUT);
        rf.setConnectTimeout(CONNECTION_TIME_OUT);

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        // Populate the MultiValueMap being serialized and headers in an HttpEntity object to use for the request
        final HttpEntity<MultiValueMap<?, ?>> requestEntity =
                new HttpEntity<MultiValueMap<?, ?>>(new LinkedMultiValueMap<String, Object>(), requestHeaders);

        if (rl != null) {
            AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, clazz);
                        rl.response(response.getBody());
                    } catch (RestClientException e) {
                        Log.e("UtilRestJSONClient", e.getMessage(), e);
                        e.printStackTrace();

                        // maybe a timeout!???
                        // is the server online yet??

                        UtilNetwork.get().checkMessicServerUpAndRunning(config.getCurrentMessicService(),
                                new MessicServerStatusListener() {
                                    public void setResponse(boolean reachable,
                                                            boolean running) {
                                        if (!running) {

                                            // server is no online anymore
                                            config.logout();
                                        }
                                    }
                                });

                        rl.fail(e);
                    }
                    return null;
                }

            };

            at.execute();
            return null;
        } else {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, clazz);
            return response.getBody();
        }
    }

    private <T> T postEntity(RestTemplate restTemplate, String url, HttpEntity requestEntity, Class<T> clazz) {
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, clazz);
        return response.getBody();
    }

    public interface RestListener<T> {
        void response(T response);

        void fail(Exception e);
    }
}
