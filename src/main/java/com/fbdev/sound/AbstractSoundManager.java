/*
 * AbstractSoundManager
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 27/10/19 13:12
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

package com.fbdev.sound;

import com.fbdev.helios.sound.SoundProvider;
import com.fbdev.helios.util.PriorityThreadFactory;
import com.fbdev.helios.util.SoundUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractSoundManager implements SoundProvider {
    private static final Logger LOG = LogManager.getLogger(AbstractSoundManager.class.getSimpleName());
    private static final int OUTPUT_SAMPLE_SIZE = 8;
    private static final int OUTPUT_CHANNELS = 2;
    public static AudioFormat audioFormat = new AudioFormat(SoundProvider.SAMPLE_RATE_HZ, OUTPUT_SAMPLE_SIZE, OUTPUT_CHANNELS, true, false);
    public volatile boolean close;
    protected ExecutorService executorService;
    protected SourceDataLine dataLine;
    protected Sound pmSound;
    private boolean mute = false;

    public static SoundProvider createSoundProvider(Sound pmSound) {
        if (!ENABLE_SOUND) {
            LOG.warn("Sound disabled");
            return NO_SOUND;
        }
        AbstractSoundManager jsm = new JalSoundManager();
        jsm.pmSound = pmSound;
        jsm.init();
        return jsm;
    }


    public void init() {
        executorService = Executors.newSingleThreadExecutor
                (new PriorityThreadFactory(Thread.MAX_PRIORITY, AbstractSoundManager.class.getSimpleName()));
        LOG.info("Output audioFormat: " + audioFormat);
    }


    @Override
    public void reset() {
        LOG.info("Resetting sound");
        close = true;
        List<Runnable> list = executorService.shutdownNow();
        SoundUtil.close(dataLine);
        LOG.info("Closing sound, stopping background tasks: #{}", list.size());
    }

    @Override
    public void close() {
        reset();
    }

    @Override
    public boolean isMute() {
        return mute;
    }

    @Override
    public void setEnabled(boolean mute) {
        this.mute = mute;
        LOG.info("Set mute: {}", mute);
    }
}
