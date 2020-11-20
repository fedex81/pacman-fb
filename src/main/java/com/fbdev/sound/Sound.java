package com.fbdev.sound;

/*
 * Copyright 2015 Mark Longstaff-Tyrrell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fbdev.helios.model.IoProvider;
import com.fbdev.helios.sound.SoundProvider;
import com.fbdev.helios.util.Util;
import com.fbdev.util.RomHelper;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sound.java
 *
 * @author Mark Longstaff-Tyrrell.
 * <p>
 * - 2020-11
 * .refactor and adapt
 * .reduce latency
 * .remove FIR resampling, use quick resampling
 * @author Federico Berti
 */

/*
Register
￼￼￼￼￼
Voice 1 Waveform
Address
5045h
Notes
￼￼low 3 bits used – selects waveform 0-7 from ROM
￼
￼￼
￼
5050h-5054h 20 bits in low nibbles
5055h ￼ ￼ ￼ ￼ low nibble – 0 off to 15 loudest
5040h-5044h low nibbles, used by H/W only
504Ah low 3 bits used – selects waveform 0-7 from ROM
5056h-5059h ￼ ￼ ￼ ￼ 16 bits in low nibbles
505Ah low nibble – 0 off to 15 loudest
5046h-5049h ￼ ￼ ￼ ￼ low nibbles, used by H/W only
504Fh ￼ ￼ ￼ ￼ low 3 bits used – selects waveform 0-7 from ROM
505Bh-505Eh 16 bits in low nibbles
505Fh ￼ ￼ ￼ ￼ low nibble – 0 off to 15 loudest
504Bh-504Eh low nibbles, used by H/W only
*/
public class Sound implements SoundProvider {

    private static final int FRAMES_PER_SECOND = 60;
    private static final int SOURCE_SAMPLE_RATE = 96000;
    private static final int OUTPUT_SAMPLE_RATE = 48000;
    final int srcFrameSize = SOURCE_SAMPLE_RATE / FRAMES_PER_SECOND;// pacman sound runs at 96kHz
    final int dstFrameSize = OUTPUT_SAMPLE_RATE / FRAMES_PER_SECOND;// emulator sound
    final byte[] outputBuffer = new byte[dstFrameSize];
    final int[] output96 = new int[srcFrameSize];
    IoProvider io;
    byte[] rom;
    VoiceParameters voiceParameters;
    boolean mute;
    SourceDataLine line;
    byte[] frame1 = new byte[srcFrameSize];
    byte[] frame2 = new byte[srcFrameSize];
    byte[] frame3 = new byte[srcFrameSize];

    public Sound(RomHelper r, IoProvider io) {
        this.io = io;
        rom = r.getSoundRom();
        voiceParameters = new VoiceParameters();
        openSound();
    }

    @Override
    public void close() {
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

    @Override
    public boolean isMute() {
        return mute;
    }

    @Override
    public void setEnabled(boolean mute) {
        this.mute = mute;
    }

    private void openSound() {
        AudioFormat pcm = new AudioFormat(OUTPUT_SAMPLE_RATE, 8, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcm);
        int bufferMono8bitMs = OUTPUT_SAMPLE_RATE / 1000 * 25; //25ms
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(pcm, bufferMono8bitMs);
            line.start();
            LOG.info("Buffer size: {}", line.getBufferSize());
        } catch (Exception ex) {
            Logger.getLogger(Sound.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onNewFrame() {
        playSound();
    }

    // generate and play next frame (1/60th of a second) of sound data
    private void playSound() {
        if (!io.isSoundEnabled() || mute)
            return;

        int[] buffer = voiceParameters.generateSoundFrame(srcFrameSize);
        // quick resample 96kHz buffer -> 48khz
        for (int i = 0, offset = 0; i < dstFrameSize; i++, offset += 2) {
            int val = (buffer[offset] + buffer[offset + 1]) >> 1;
            outputBuffer[i] = (byte) (val & 0xFF);
        }

        // write data to sound output
        line.write(outputBuffer, 0, outputBuffer.length);
    }

    private class VoiceParameters {
        private static final int VOICE1 = 0;
        private static final int VOICE2 = 1;
        private static final int VOICE3 = 2;

        // 16 32 sample waveforms in the sound ROMs
        private static final int WAVEFORMS = 16;
        private static final int SAMPLES_PER_WAVEFORMS = 32;

        int[] frequency;
        int[] volume;
        int[] waveform;
        int[] accumulator;

        // sound samples preloaded from sound ROM
        int[][] samples;

        public VoiceParameters() {
            frequency = new int[3];
            volume = new int[3];
            waveform = new int[3];
            accumulator = new int[3];
            samples = new int[WAVEFORMS][];

            // preload waveforms from ROMs
            for (int i = 0; i < WAVEFORMS; i++) {
                samples[i] = getSample(i);
            }
        }

        public void UpdateVoiceParameters() {
            UpdateVoiceParameters(VOICE1);
            UpdateVoiceParameters(VOICE2);
            UpdateVoiceParameters(VOICE3);
        }

        public void UpdateVoiceParameters(int voice) {
            // get voice parameters out of the IO component
            switch (voice) {
                case VOICE1:
                    waveform[0] = getWaveformIndex(0x5045);
                    frequency[0] = getFrequency(0x5050, 5);
                    volume[0] = getVolume(0x5055);

                    //    if(frequency[0]>0)
                    //       System.out.println(String.format("Voice 1 volume 0x%05x",volume[0]));

                    break;
                case VOICE2:
                    waveform[1] = getWaveformIndex(0x504a);
                    frequency[1] = getFrequency(0x5056, 4) << 4;
                    volume[1] = getVolume(0x505a);
                    //   if(frequency[1]>0)
                    //       System.out.println(String.format("Voice 2 volume 0x%05x",volume[1]));

                    break;
                case VOICE3:
                    waveform[2] = getWaveformIndex(0x504f);
                    frequency[2] = getFrequency(0x505b, 4) << 4;
                    volume[2] = getVolume(0x505f);

                    // if(frequency[2]>0)
                    //   System.out.println(String.format("Voice 3 volume 0x%05x",volume[2]));

                    //  System.out.println(String.format("volumes: %d, %d, %d", volume[0],volume[1],volume[2]));


                    break;
                default:
                    break;
            }
        }

        // return a single sample, adjusted for frequency and volume
        private int getNextSample(int voice) {
            // get selected sample waveform
            int[] wave = samples[waveform[voice]];

            // get next sample byte
            // index into wave is is top 5 bits in accumulator
            int offset = (accumulator[voice] >> 15) & 0x1f;
            int sample = wave[offset];

            // update accumulator
            accumulator[voice] += frequency[voice];

            // looking at the schematic, the 4-bit volume parameter is applied to the enable lines of 4 analogue switches
            // (the input of each switch is from a weighted resistor network) so it's an & rather than a multiply
            int output = sample & volume[voice];

            // maximise volume of 4-bit samples for output
            return output <<= 3;
        }

        private int getWaveformIndex(int address) {
            return io.readSoundData(address) & 0x07;
        }

        // get low nibbles from frequency bytes
        // store them as a single 20-bit value

        // 4444 3333 2222 1111 0000

        private int getFrequency(int address, int length) {
            int frequency = 0;

            for (int i = length - 1; i >= 0; i--) {

                frequency <<= 4;

// two voices have 4-nibble frequencies
                // if(i<=length)
                frequency |= (io.readSoundData(address + i) & 0x0f);

            }

            frequency &= 0xfffff;


            return frequency;
        }

        private int getVolume(int address) {
            return (io.readSoundData(address) & 0x0f);//>>1;
        }

        /*
            4-bit samples, so each byte entry has the top nibble set to 0.
            This gives in total 512 four-bit sound samples.
            These are organized into 16 waveforms, each 32 samples long, and each sample value from 0-15.
        */
        // get waveform from sound ROM
        private int[] getSample(int sampleNumber) {
            int[] sample = new int[SAMPLES_PER_WAVEFORMS];
            int offset = sampleNumber * SAMPLES_PER_WAVEFORMS;

            for (int i = 0; i < WAVEFORMS; i++) {
                sample[i] = rom[i + offset];
            }

            return sample;
        }

        // return PCM data of currently playing sound on voice 'voice'
        public byte[] generateVoiceWaveform(int voice, int frameSize, byte[] frame) {
            for (int i = 0; i < frameSize; i++) {
                frame[i] = (byte) voiceParameters.getNextSample(voice);
            }
            return frame;
        }


        public int[] generateSoundFrame(int frameSize) {
            voiceParameters.UpdateVoiceParameters();

            byte[] voice1 = voiceParameters.generateVoiceWaveform(VoiceParameters.VOICE1, frameSize, frame1);
            byte[] voice2 = voiceParameters.generateVoiceWaveform(VoiceParameters.VOICE2, frameSize, frame2);
            byte[] voice3 = voiceParameters.generateVoiceWaveform(VoiceParameters.VOICE3, frameSize, frame3);

            // mix the three channels together
            for (int i = 0; i < frameSize; i++) {
                int mix = voice1[i] + voice2[i] + voice3[i];
                output96[i] = mix >> 1;
            }
            return output96;
        }

    }
}
