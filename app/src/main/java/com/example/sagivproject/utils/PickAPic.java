package com.example.sagivproject.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PickAPic {
    private final List<Integer> availableImages;

    public PickAPic(Context context, int totalImages) {
        availableImages = new ArrayList<>();

        for (int i = 0; i < totalImages; i++) {
            //שם הקובץ כפי שהוא ב-drawable
            String name = "pics_for_game_" + (i + 1);
            int resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());

            if (resId != 0) {
                availableImages.add(resId);
            }
        }

        Collections.shuffle(availableImages);
    }

    public List<Integer> getUniqueImages(int count) {
        if (count > availableImages.size()) {
            throw new IllegalArgumentException("אין מספיק תמונות ייחודיות");
        }
        return new ArrayList<>(availableImages.subList(0, count));
    }
}
