package com.vishalroy.whatsappvideocaller.Helpers;

import android.content.Context;
import android.graphics.Typeface;

public class Typefaces {

    private Context context;

    public Typefaces(Context context){
        this.context = context;
    }

    public Typeface ralewayMedium(){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-Medium.ttf");
    }

    public Typeface ralewaySemiBold(){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-SemiBold.ttf");
    }

    public Typeface latoBold(){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Bold.ttf");
    }
}
