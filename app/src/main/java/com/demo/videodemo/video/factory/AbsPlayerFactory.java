package com.demo.videodemo.video.factory;

import androidx.lifecycle.LifecycleOwner;

import com.demo.videodemo.video.abs.AbstractPlayer;

public abstract class AbsPlayerFactory {
    public abstract AbstractPlayer createPlayer(LifecycleOwner owner);
}
