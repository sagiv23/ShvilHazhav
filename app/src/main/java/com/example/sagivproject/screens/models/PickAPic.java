package com.example.sagivproject.screens.models;

import android.content.Context;
import android.widget.ImageView;

import java.util.Random;

public class PickAPic {
    private int[] imageResIds;
    private Random rnd;

    public PickAPic(Context context, int totalImages) {
        imageResIds = new int[totalImages];
        rnd = new Random();

        //לולאה שמכניסה את כל ההתמונות למערך
        for (int i = 0; i < totalImages; i++) {
            //שם הקובץ כפי שהוא ב-drawable
            String name = "pics_for_game_" + (i + 1);

            int resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
            imageResIds[i] = resId;
        }
    }

    //מחזיר ID של תמונה רנדומלית
    public int getRandomImageResId() {
        int idx = rnd.nextInt(imageResIds.length);
        return imageResIds[idx];
    }

    // שם תמונה רנדומלית ב-ImageView
    public void setRandomImage(ImageView imageView) {
        int resId = getRandomImageResId();
        imageView.setImageResource(resId);
    }
}