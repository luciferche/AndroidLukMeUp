package com.luciferche.lukmeup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by luciferche on 2/22/18.
 */

public class ImageUtils {

    public static Bitmap createUserMarker(Context context, String text) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View markerLayout = inflater.inflate(R.layout.user_marker_layout, null);

//        ImageView markerImage = (ImageView) markerLayout.findViewById(R.id.marker_image);
        TextView userTitle = (TextView) markerLayout.findViewById(R.id.tv_marker_title);
//        markerImage.setImageResource(R.drawable.ic_home_marker);
        userTitle.setText(text);

        markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);
        return bitmap;
    }
}
