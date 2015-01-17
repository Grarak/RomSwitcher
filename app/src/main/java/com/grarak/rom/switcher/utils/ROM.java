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
