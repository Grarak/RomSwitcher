package com.grarak.romswitcher.utils;

import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by grarak's kitten (meow) on 02.04.14.
 */

public class RootUtils implements Constants {

    public void run(String command) {
        // Let's take over the world with my cuteness (meow meow)
        try {
            RootTools.getShell(true).add(new CommandCapture(0, command))
                    .commandCompleted(0, 0);
        } catch (IOException e) {
            Log.e(TAG, "failed to run " + command);
        } catch (TimeoutException ignored) {
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
