/*
 * BaseVdpProvider
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 17/10/19 10:54
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

package com.fbdev.helios;

import com.fbdev.helios.util.VideoMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EventListener;
import java.util.List;

public interface BaseVdpProvider {

    Logger LOG = LogManager.getLogger(BaseVdpProvider.class.getSimpleName());

    void init();

    VideoMode getVideoMode();

    void renderScreenDataLinear(int[] render);

    //after loading a state
    default void reload() {
        //DO NOTHING
    }

    default void dumpScreenData() {
        throw new UnsupportedOperationException("Not supported");
    }

    default String getVdpStateString() {
        return "vdpState: unsupported";
    }

    default void resetVideoMode(boolean force) {
        throw new UnsupportedOperationException("Not supported");
    }

    default List<VdpEventListener> getVdpEventListenerList() {
        return Collections.emptyList();
    }

    default boolean addVdpEventListener(VdpEventListener l) {
        return getVdpEventListenerList().add(l);
    }

    default boolean removeVdpEventListener(VdpEventListener l) {
        return getVdpEventListenerList().remove(l);
    }

    enum VdpEvent {NEW_FRAME, VIDEO_MODE, H_LINE_COUNTER, INTERRUPT}

    interface VdpEventListener extends EventListener {

        default void onVdpEvent(VdpEvent event, Object value) {
        }

        default void onRegisterChange(int reg, int value) {
        }

        default void onNewFrame() {
            onVdpEvent(VdpEvent.NEW_FRAME, null);
        }
    }
}
