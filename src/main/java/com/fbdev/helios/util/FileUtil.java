/*
 * FileUtil
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 22/11/2020, 12:57
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {

    private static final Logger LOG = LogManager.getLogger(FileUtil.class.getSimpleName());

    public static String readFileContentAsString(String fileName) {
        return readFileContent(fileName).stream().collect(Collectors.joining("\n"));
    }

    public static List<String> readFileContent(String fileName) {
        Path pathObj = Paths.get(".", fileName);
        return readFileContent(pathObj);
    }

    public static List<String> readFileContent(Path pathObj) {
        List<String> lines = Collections.emptyList();
        String fileName = pathObj.getFileName().toString();
        if (pathObj.toFile().exists()) {
            try {
                lines = Files.readAllLines(pathObj);
            } catch (IOException e) {
                LOG.error("Unable to load {}, from path: {}", fileName, pathObj);
            }
            return lines;
        }
        String classPath = getCurrentClasspath();
        if (isRunningInJar(classPath)) {
            return loadFileContentFromJar(fileName);
        }
        LOG.warn("Unable to load: {}", fileName);
        return lines;
    }

    private static List<String> loadFileContentFromJar(String fileName) {
        List<String> lines = Collections.emptyList();
        try (
                InputStream inputStream = FileUtil.class.getResourceAsStream("/" + fileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            lines = reader.lines().collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Unable to load {}, from path: {}", fileName, fileName);
        }
        return lines;
    }

    private static String getCurrentClasspath() {
        Class<?> clazz = FileUtil.class;
        String className = clazz.getSimpleName() + ".class";
        return clazz.getResource(className).toString();
    }

    private static boolean isRunningInJar(String classPath) {
        return classPath.startsWith("jar");
    }
}