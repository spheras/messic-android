package org.messic.android.smarttv.activities.main.fragments;

import org.messic.android.messiccore.datamodel.MDMSong;

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
