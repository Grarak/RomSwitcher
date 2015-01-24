package com.grarak.rom.switcher.utils.root;

import android.util.Log;

import com.grarak.rom.switcher.utils.Constants;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by willi on 13.01.15.
 */
public class RootUtils implements Constants {

    public static SU su;

    public static boolean rooted() {
        return RootTools.isRootAvailable();
    }

    public static boolean rootAccess() {
        return RootTools.isAccessGiven();
    }

    public static boolean busyboxInstalled() {
        return RootTools.isBusyboxAvailable();
    }

    public static void writePartition(String image, String partition) {
        runCommand("dd if=" + image + " of=" + partition);
    }

    public static void moveFile(String oldFile, String newFile) {
        runCommand("mv -f " + oldFile + " " + newFile);
    }

    public static String getSize(String file) {
        return runCommand("du -sm " + file).split(file)[0].replace(" ", "").trim();
    }

    public static List<String> listFiles(String directory) {
        return new ArrayList<>(Arrays.asList(runCommand("ls " + directory).split("\\r?\\n")));
    }

    public static String readFile(String file) {
        return runCommand("cat " + file);
    }

    public static boolean fileExist(String file) {
        String output = runCommand("[ -e " + file + " ] && echo true");
        return output != null && output.contains("true");
    }

    public static boolean isDirectory(String directory) {
        String output = runCommand("[ -d " + directory + " ] && echo true");
        return output != null && output.contains("true");
    }

    public static String runCommand(String command) {
        if (su == null) su = new SU();
        return su.runCommand(command);
    }

    /**
     * Based on AndreiLux's SU code in Synapse
     * https://github.com/AndreiLux/Synapse/blob/master/src/main/java/com/af/synapse/utils/Utils.java#L238
     */
    public static class SU {

        private Process process;
        private BufferedWriter bufferedWriter;
        private BufferedReader bufferedReader;

        public SU() {
            try {
                Log.i(TAG, "SU initialized");
                process = Runtime.getRuntime().exec("su");
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            } catch (IOException e) {
                Log.e(TAG, "Failed to run shell as su");
            }
        }

        public synchronized String runCommand(final String command) {
            try {
                StringBuilder sb = new StringBuilder();
                String callback = "/shellCallback/";
                bufferedWriter.write(command + "\necho " + callback + "\n");
                bufferedWriter.flush();

                int i;
                char[] buffer = new char[256];
                while (true) {
                    i = bufferedReader.read(buffer);
                    sb.append(buffer, 0, i);
                    if ((i = sb.indexOf(callback)) > -1) {
                        sb.delete(i, i + callback.length());
                        break;
                    }
                }

                Log.i(TAG, "Output of: " + command + " : " + sb.toString().trim());
                return sb.toString().trim();
            } catch (IOException e) {
                Log.e(TAG, "Failed to run " + command);
                return null;
            }
        }

        public void close() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bufferedWriter.write("exit\n");
                        bufferedWriter.flush();

                        process.waitFor();
                        Log.i(TAG, "SU closed");
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close BufferWriter");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

}
