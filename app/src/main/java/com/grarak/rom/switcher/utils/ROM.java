/*
 * Copyright (C) 2015 Willi Ye
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

package com.grarak.rom.switcher.utils;

import com.grarak.rom.switcher.utils.root.RootUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 17.01.15.
 */
public class ROM {

    private final String path;
    private final List<String> roms = new ArrayList<>();

    public ROM(String path) {
        this.path = path;
        if (RootUtils.fileExist(path))
            roms.clear();
        for (String dir : RootUtils.listFiles(path))
            if (!dir.isEmpty() && RootUtils.isDirectory(path + "/" + dir))
                roms.add(dir);
    }

    public String getSize(int position) {
        return RootUtils.getSize(path + "/" + roms.get(position));
    }

    public String getName(int position) {
        return roms.get(position);
    }

    public int getLength() {
        return roms.size();
    }

}
