/*
 * SoundProvider
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 26/10/19 15:49
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fbdev.helios.sound;

import com.fbdev.helios.BaseVdpProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface SoundProvider extends BaseVdpProvider.VdpEventListener {
    Logger LOG = LogManager.getLogger(SoundProvider.class.getSimpleName());

    int SAMPLE_RATE_HZ = Integer.parseInt(System.getProperty("audio.sample.rate.hz", "44100"));

    int DEFAULT_BUFFER_SIZE_MS = 30;
    int AUDIO_BUFFER_LEN_MS = Integer.parseInt(System.getProperty("audio.buffer.length.ms",
            String.valueOf(DEFAULT_BUFFER_SIZE_MS)));

    boolean ENABLE_SOUND = Boolean.parseBoolean(System.getProperty("helios.enable.sound", "true"));

    boolean MD_NUKE_AUDIO = Boolean.parseBoolean(System.getProperty("md.nuke.audio", "true"));

    boolean JAL_SOUND_MGR = Boolean.parseBoolean(System.getProperty("helios.jal.sound.mgr", "false"));

//    int[] EMPTY_FM = new int[0];
//    byte[] EMPTY_PSG = new byte[0];


//    static int getPsgBufferByteSize(AudioFormat audioFormat) {
//        return getFmBufferIntSize(audioFormat) >> 1;
//    }
//
//    static int getFmBufferIntSize(AudioFormat audioFormat) {
//        return SoundUtil.getStereoSamplesBufferSize(audioFormat);
//    }


    SoundProvider NO_SOUND = new SoundProvider() {

        @Override
        public void onNewFrame() {
        }

        @Override
        public void close() {

        }

        @Override
        public boolean isMute() {
            return false;
        }

        @Override
        public void setEnabled(boolean mute) {
        }
    };

    void close();

    default boolean isRecording() {
        return false;
    }

    default void setRecording(boolean recording) {
        //NO OP
    }

    boolean isMute();

    void setEnabled(boolean mute);
}
