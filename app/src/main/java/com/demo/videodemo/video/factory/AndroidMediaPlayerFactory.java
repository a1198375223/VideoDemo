package com.demo.videodemo.video.factory;

import androidx.lifecycle.LifecycleOwner;

import com.demo.videodemo.video.AndroidMediaPlayer;
import com.demo.videodemo.video.abs.AbstractPlayer;

public class AndroidMediaPlayerFactory extends AbsPlayerFactory {


    public AndroidMediaPlayerFactory() { }

    public static AndroidMediaPlayerFactory create() {
        return new AndroidMediaPlayerFactory();
    }

    @Override
    public AbstractPlayer createPlayer(LifecycleOwner owner) {
        return new AndroidMediaPlayer(owner);
    }
}
