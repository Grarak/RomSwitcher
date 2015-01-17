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
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by willi on 13.01.15.
 */
public class RootUtils implements Constants {

    public static SU su;

    public static void runCommand(String command) {
        if (su == null) su = new SU();
        su.run(command);
    }

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
        getOutput("dd if=" + image + " of=" + partition);
    }

    public static void moveFile(String oldFile, String newFile) {
        getOutput("mv -f " + oldFile + " " + newFile);
    }

    public static String getSize(String file) {
        return getOutput("du -sm " + file).split(file)[0].replace(" ", "").trim();
    }

    public static List<String> listFiles(String directory) {
        return new ArrayList<>(Arrays.asList(getOutput("ls " + directory).split("\\r?\\n")));
    }

    public static String readFile(String file) {
        return getOutput("cat " + file);
    }

    public static boolean fileExist(String file) {
        String output = getOutput("[ -e " + file + " ] && echo true");
        return output != null && output.contains("true");
    }

    public static boolean isDirectory(String directory) {
        String output = getOutput("[ -d " + directory + " ] && echo true");
        return output != null && output.contains("true");
    }

    public static String getOutput(String command) {
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

        public synchronized void run(final String command) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bufferedWriter.write(command + "\n");
                        bufferedWriter.flush();

                        Log.i(TAG, "run: " + command);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to run " + command);
                    }
                }
            }).start();
        }

        public synchronized String runCommand(final String command) {
            Future<String> value = Executors.newFixedThreadPool(3).submit(new Callable<String>() {
                @Override
                public String call() {
                    StringBuilder sb = new StringBuilder();

                    try {
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
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to run " + command);
                    }

                    return sb.toString().trim();
                }
            });

            try {
                return value.get();
            } catch (Exception e) {
                return "";
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
