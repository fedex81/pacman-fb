/*
 * JavaSoundManager
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 26/10/19 17:40
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
import com.fbdev.helios.util.SoundUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.audioservers.AudioServer;
import org.jaudiolibs.audioservers.AudioServerProvider;
import org.jaudiolibs.audioservers.ext.ClientID;
import org.jaudiolibs.audioservers.ext.Connections;
import org.jaudiolibs.audioservers.javasound.JSTimingMode;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.ServiceLoader;

public class JalSoundManager extends AbstractSoundManager implements AudioClient {

    //Output audioFormat: PCM_SIGNED 48000.0 Hz, 8 bit, stereo, 2 bytes/frame,
    public static final int bufferSize = SoundUtil.getAudioLineBufferSizeSingleChannel(audioFormat,
            SoundProvider.AUDIO_BUFFER_LEN_MS);
    private static final Logger LOG = LogManager.getLogger(JalSoundManager.class.getSimpleName());
    private static final String lib = "JavaSound"; // or "JACK";

    private static final int JAL_TIMING_MODE = Integer.parseInt(System.getProperty("helios.jal.timing.mode", "1"));

    private JSTimingMode timingMode = JSTimingMode.FramePosition;

    @Override
    public void init() {
        super.init();
        for (JSTimingMode mode : JSTimingMode.values()) {
            if (mode.ordinal() == JAL_TIMING_MODE) {
                timingMode = mode;
                break;
            }
        }
        LOG.info("Using timing mode: {}", timingMode);
        startAudio();
    }

    private void startAudio() {
        AudioServerProvider provider = null;
        for (AudioServerProvider p : ServiceLoader.load(AudioServerProvider.class)) {
            if (lib.equals(p.getLibraryName())) {
                provider = p;
                break;
            }
        }
        if (provider == null) {
            throw new NullPointerException("No AudioServer found that matches : " + lib);
        }
        AudioClient client = this;
        AudioConfiguration config = new AudioConfiguration(
                audioFormat.getSampleRate(), //sample rate
                0, // input channels
                audioFormat.getChannels(), // output channels
                bufferSize, //buffer size
                false,
                // extensions
                new Object[]{
                        new ClientID("JalSoundManager"),
                        timingMode,
                        Connections.OUTPUT
                });
        try {
            AudioServer server = provider.createServer(config, client);
            executorService.submit(getServerRunnable(server));
            LOG.info("Audio Max buffer size (in samples per channel): {}", bufferSize);
        } catch (Throwable t) {
            LOG.error(t);
            t.printStackTrace();
        }
    }

    private Runnable getServerRunnable(AudioServer server) {
        return () -> {
            try {
                server.run();
            } catch (InterruptedException ie) {
                LOG.info("interrupted");
                return;
            } catch (Exception | Error ex) {
                LOG.error(ex);
                ex.printStackTrace();
            }
        };
    }

    @Override
    public void onNewFrame() {
        pmSound.onNewFrame();
    }

    @Override
    public void configure(AudioConfiguration context) throws Exception {
        LOG.info("configure");
    }

    @Override
    public boolean process(long time, List<FloatBuffer> inputs, List<FloatBuffer> outputs, int nframes) {
        // get left and right channels from array list
        FloatBuffer left = outputs.get(0);
        FloatBuffer right = outputs.get(1);
        final int[] buffer96 = pmSound.generateSoundMonoFloat48khz(nframes);
        int k = 0;
        for (int i = 0; i < nframes; i++, k += 2) {
            int val = (buffer96[k] + buffer96[k + 1]) >> 1; //96khz -> 48khz
            float val1 = ((float) (val & 0xFF)) / 256f; //toFloat
            left.put(val1);
            right.put(val1);
            if (k == buffer96.length) {
                k = 0;
                LOG.warn("wrap");
            }
        }
        return !close;
    }

    @Override
    public void shutdown() {
        LOG.info("shutdown");
    }

    @Override
    public void reset() {
        super.reset();
        shutdown();
    }
}

