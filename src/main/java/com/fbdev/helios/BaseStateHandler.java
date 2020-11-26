/*
 * BaseStateHandler
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 18/06/19 17:15
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

import com.fbdev.helios.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

public interface BaseStateHandler {

    Logger LOG = LogManager.getLogger(BaseStateHandler.class.getSimpleName());

    Type getType();

    String getFileName();

    byte[] getData();

    default void storeData() {
        LOG.info("Persisting savestate to: {}", getFileName());
        FileUtil.writeFileSafe(Paths.get(getFileName()), getData());
    }

    enum Type {SAVE, LOAD}
}
