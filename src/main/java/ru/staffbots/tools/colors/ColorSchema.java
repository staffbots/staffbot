package ru.staffbots.tools.colors;

import java.awt.*;

public class ColorSchema {

    private float mainRatio = 0.5f;

    private HSLColor mainColor = new HSLColor(Color.lightGray);

    public ColorSchema() {
    }

    public ColorSchema(String mainColor) {
        if (mainColor == null)
            return;
        try {
            Color color = Color.decode(mainColor);
            this.mainColor = new HSLColor(color);
        } catch (Exception e) {}
    }

    public ColorSchema(Color mainColor) {
        if (mainColor == null)
            return;
        this.mainColor = new HSLColor(mainColor);
    }

    public String getMainColor() {
        return mainColor.toHex();
    }

    public String getSiteColor() {
        return adjustMainLuminance(mainRatio / 10).toHex();
    }

    public String getDeepColor() {
        return adjustMainLuminance(mainRatio).toHex();
    }

    private HSLColor adjustMainLuminance(float ratio) {
        float mainLuminance = mainColor.getLuminance();
        float luminance = mainLuminance < 50f ? mainLuminance * ratio : 100 - (100 - mainLuminance) * ratio;
        float minLuminance = 0;
        float maxLuminance = 100;
        if (luminance < minLuminance) luminance = minLuminance;
        if (luminance > maxLuminance) luminance = maxLuminance;
        return new HSLColor(mainColor.adjustLuminance(luminance));
    }

    public String getTextColor() {
        float luminance = 100 - new HSLColor(Color.decode(getSiteColor())).getLuminance();
        return new HSLColor(mainColor.adjustLuminance(luminance)).toHex();
    }

    public String getHalfColor() {
        return new HSLColor(mainColor.adjustLuminance(50f)).toHex();
    }

}
