package com.example.briantomasco.profile_fill;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by zacharyjohnson on 11/5/17.
 */

public class HistoryLayoutHelper {

    Context context;
    String picUrl;
    String name;
    double lat;
    double lng;
    boolean petted;


    public HistoryLayoutHelper(Context context, String picUrl, String name, double lat, double lng, boolean petted) {
        this.picUrl = picUrl;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.petted = petted;
        this.context = context;
    }

    public LinearLayout createLayout() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setWeightSum(5);
        layout.setPadding(10, 10, 10, 10);

        RelativeLayout imageEnclose = new RelativeLayout(context);
        imageEnclose.setLayoutParams(new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.MATCH_PARENT, 1.0f));
        imageEnclose.setHorizontalGravity(RelativeLayout.CENTER_HORIZONTAL);
        imageEnclose.setVerticalGravity(RelativeLayout.CENTER_VERTICAL);

        ImageView image = new ImageView(context);
        Picasso.with(context).load(picUrl).into(image);

        imageEnclose.addView(image);
        layout.addView(imageEnclose);

        LinearLayout catInfo = new LinearLayout(context);
        catInfo.setOrientation(LinearLayout.VERTICAL);
        catInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3.0f));

        catInfo.addView(createInfoTextView("Cat name", name));
        catInfo.addView(createInfoTextView("Lat", Double.toString(lat)));
        catInfo.addView(createInfoTextView("Lng", Double.toString(lng)));

        layout.addView(catInfo);

        Button petty = new Button(context);
        petty.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        petty.setTextSize(16.0f);
        petty.setClickable(false);
        if (!petted) {
            petty.setText("Not petted");
            petty.setBackgroundColor(Color.RED);
            petty.setTextColor(Color.WHITE);
        }
        else {
            petty.setText("Petted");
            petty.setTextColor(Color.WHITE);
            petty.setBackgroundColor(Color.GREEN);
        }


        layout.addView(petty);



        return layout;
    }

    protected TextView createInfoTextView(String type, String info) {
        TextView infoView = new TextView(context);
        infoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        infoView.setText(type + ": " + info);
        infoView.setTextSize(12.0f);
        return infoView;
    }
}
