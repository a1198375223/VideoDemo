package com.demo.videodemo.codec;

public interface IDecoderStateListener {
    void decoderPrepare(BaseDecoder decoder);

    void decoderReady(BaseDecoder decoder);

    void decoderRunning(BaseDecoder decoder);

    void decoderPause(BaseDecoder decoder);

    void decodeOneFrame(BaseDecoder decoder, Frame frame);

    void decoderFinish(BaseDecoder decoder);

    void decoderDestroy(BaseDecoder decoder);

    void decoderError(BaseDecoder decoder, String msg);
}
