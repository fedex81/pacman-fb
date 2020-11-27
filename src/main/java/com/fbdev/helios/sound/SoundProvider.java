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
import com.fbdev.helios.model.Device;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface SoundProvider extends BaseVdpProvider.VdpEventListener, Device {
    Logger LOG = LogManager.getLogger(SoundProvider.class.getSimpleName());

    //ignored, we only support 48khz
    int SAMPLE_RATE_HZ = 48000;

    int DEFAULT_BUFFER_SIZE_MS = 25;
    int AUDIO_BUFFER_LEN_MS = Integer.parseInt(System.getProperty("audio.buffer.length.ms",
            String.valueOf(DEFAULT_BUFFER_SIZE_MS)));

    boolean ENABLE_SOUND = Boolean.parseBoolean(System.getProperty("helios.enable.sound", "true"));
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

    boolean isMute();

    void setEnabled(boolean mute);

}
