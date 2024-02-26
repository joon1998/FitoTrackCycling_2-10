package de.tadris.fitness.util.charts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.data.BarData;

import java.util.ArrayList;

import de.tadris.fitness.R;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.Icon;

public class BitmapHelper {
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static ArrayList<Bitmap> getBitmaps(Context ctx, ArrayList<WorkoutType> types)
    {
        ArrayList<Bitmap> imageList = new ArrayList<>();
        for (WorkoutType type:types) {
            Drawable icon = ctx.getResources().getDrawable(Icon.getIcon(type.icon));
            icon.mutate().setColorFilter(ctx.getResources().getColor(type.color), PorterDuff.Mode.SRC_IN);
            Bitmap bitmap = drawableToBitmap(icon);
            imageList.add(bitmap);
        }
        return imageList;
    }
}
