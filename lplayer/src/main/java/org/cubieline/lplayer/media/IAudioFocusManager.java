package org.cubieline.lplayer.media;

import android.media.AudioManager;

/**
 * Created by LeiGuoting on 14/11/12.
 */
/*package*/ interface IAudioFocusManager extends AudioManager.OnAudioFocusChangeListener {

    /*package*/ int AUDIO_FOCUS_NO_FOCUS_NO_DUCK = 0x01;

    /*package*/ int AUDIO_FOCUS_NO_FOCUS_CAN_DUCK = 0x02;

    /*package*/ int AUDIO_FOCUS_FOCUSED = 0x04;

    /*package*/ void pauseFromAudioFocus();

    /*package*/ void startFromAudioFocus();

    /*package*/ void tryToGetAudioFocus();

    /*package*/ void releaseAudioFocus();
}
