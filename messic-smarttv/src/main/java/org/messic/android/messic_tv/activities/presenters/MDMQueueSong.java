package org.messic.android.messic_tv.activities.presenters;

import org.messic.android.messiccore.datamodel.MDMSong;

/**
 * Created by spheras on 23/08/15.
 */
public class MDMQueueSong extends MDMSong {

    public int indexAtList;

    public MDMQueueSong(MDMSong song) {
        setAlbum(song.getAlbum());
        setCode(song.getCode());
        setFileName(song.getFileName());
        setFlagFromLocalDatabase(song.isFlagFromLocalDatabase());
        setLfileName(song.getLfileName());
        setLsid(song.getLsid());
        setName(song.getName());
        setRate(song.getRate());
        setSid(song.getSid());
        setSize(song.getSize());
        setTrack(song.getTrack());

    }

}
