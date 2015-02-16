package pw.moter8.sunnyweather;

import android.graphics.Color;

import java.util.Random;

public class ColorWheel {

    // Member variable: properties about the object
    // Method: abilities, things the object can do


    public int getColor() {

        String[] mColors = {
                "#39add1", // light blue
                "#3079ab", // dark blue
                "#c25975", // mauve
                "#e15258", // red
                "#f9845b", // orange
                "#838cc7", // lavender
                "#7d669e", // purple
                "#53bbb4", // aqua
                "#51b46d", // green
                "#e0ab18", // mustard
                "#637a91", // dark gray
                "#f092b0", // pink
                "#b7c0c7"  // light gray
        };

        Random randomGen = new Random();
        int randomNumber = randomGen.nextInt(mColors.length);

        String randomColor = mColors[randomNumber];
        int rndColorAsInt = Color.parseColor(randomColor);
        return rndColorAsInt;


    }

}