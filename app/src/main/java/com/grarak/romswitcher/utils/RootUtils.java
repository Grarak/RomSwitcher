package com.grarak.romswitcher.utils;

/*
 * Copyright (C) 2014 The RomSwitcher Project
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

/*
 * Created by grarak's kitten (meow) on 02.04.14.
 */

import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RootUtils implements Constants {

    public void run(String command) {

        // Let's take over the world with my cuteness (meow meow)
        try {
            RootTools.getShell(true).add(new CommandCapture(0, command))
                    .commandCompleted(0, 0);
        } catch (IOException e) {
            Log.e(TAG, "failed to run " + command);
        } catch (TimeoutException ignored) {
            Log.e(TAG, "Timeout: Cannot gain root access");
        } catch (RootDeniedException e) {
            Log.e(TAG, "Root access denied");
        }
    }

    public void reboot() {
        run("reboot");
    }

    public void writePartition(String image, String partition) {
        run("dd if=" + image + " of=" + partition);
    }

    public void readPartition(String partition, String image) {
        run("dd if=" + partition + " of=" + image);
    }

    public void createImage(String image, int size) {
        run("dd if=/dev/zero of=" + image + " bs=1024 count=" + String.valueOf(size * 1024));
        run("mke2fs -F -T ext4 " + image);
    }

}
