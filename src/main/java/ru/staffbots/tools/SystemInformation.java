package ru.staffbots.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class SystemInformation {

    public static final String osName = System.getProperty("os.name");
    public static final String osVersion = System.getProperty("os.version");
    public static final String osArchitecture = System.getProperty("os.arch");
    public static final String osRelease = getOSRelease();
    public static final boolean isRaspbian = isRaspbian();

    private static String getOSRelease() {
        if (osName.toLowerCase().contains("linux")) {
            try (FileInputStream fileInputStream = new FileInputStream("/etc/os-release")) {
                Properties properties = new Properties();
                properties.load(fileInputStream);
                return properties.getProperty("ID");
            } catch (Exception exception) {
            }
        }
        return "";
    }

    private static boolean isRaspbian() {
        return osRelease.equalsIgnoreCase("raspbian");
    }

    public static double getCPUTemperature(double defaultValue){
        if (!isRaspbian) return defaultValue;
        try (FileInputStream fstream = new FileInputStream("/sys/class/thermal/thermal_zone0/temp")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            return Integer.parseInt(br.readLine())/1000d;
        }
        catch(Exception exception){
            exception.printStackTrace();
            return defaultValue;
        }
    }

}
