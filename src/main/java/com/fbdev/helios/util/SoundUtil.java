/*
 * SoundUtil
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 05/10/19 14:15
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

package com.fbdev.helios.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.lang.reflect.Field;

public class SoundUtil {

    private static final Logger LOG = LogManager.getLogger(SoundUtil.class.getSimpleName());

    /*
     * gets the number of bytes needed to play the specified number of milliseconds
     * @see com.sun.media.sound.Toolkit
     */
    private static long millis2bytes(AudioFormat format, long millis) {
        long result = (long) (millis * format.getFrameRate() / 1000.0f * format.getFrameSize());
        return align(result, format.getFrameSize());
    }

    /*
     * returns bytes aligned to a multiple of blocksize
     * the return value will be in the range of (bytes-blocksize+1) ... bytes
     */
    static long align(long bytes, int blockSize) {
        // prevent null pointers
        if (blockSize <= 1) {
            return bytes;
        }
        return bytes - (bytes % blockSize);
    }

    //as in bytes for the underlying dataLine
    public static int getAudioLineBufferSize(AudioFormat audioFormat, int bufferLenMs) {
        return (int) millis2bytes(audioFormat, bufferLenMs);
    }

    //as in bytes for the underlying dataLine
    public static int getAudioLineBufferSizeSingleChannel(AudioFormat audioFormat, int bufferLenMs) {
        return (int) millis2bytes(audioFormat, bufferLenMs) >> 1;
    }

    private static void lowerLatencyHack(SourceDataLine line) {
        String sname = line.getClass().getSuperclass().getCanonicalName();
        if ("com.sun.media.sound.DirectAudioDevice.DirectDL".equalsIgnoreCase(sname)) {
            try {
                Field f = line.getClass().getSuperclass().getDeclaredField("waitTime");
                f.setAccessible(true);
                f.set(line, 1);
                LOG.info("Setting waitTime to 1ms for SourceDataLine: {}", line.getClass().getCanonicalName());
            } catch (Exception e) {
                LOG.warn("Unable to set waitTime for SourceDataLine: {}", line.getClass().getCanonicalName());
            }
        }
    }

    public static void close(DataLine line) {
        if (line != null) {
            line.stop();
            synchronized (line) {
                line.flush();
            }
            Util.sleep(150); //avoid pulse-audio crashes on linux
            line.close();
            Util.sleep(100);
        }
    }
}