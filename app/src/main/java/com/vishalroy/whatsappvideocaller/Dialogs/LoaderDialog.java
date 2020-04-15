package com.vishalroy.whatsappvideocaller.Dialogs;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.vishalroy.whatsappvideocaller.Helpers.Typefaces;
import com.vishalroy.whatsappvideocaller.R;

import java.text.DecimalFormat;

public class LoaderDialog {

    public Dialog dialog;
    private CircularProgressView progressBar;
    private TextView progress_text;
    private Context context;

    public LoaderDialog(Context context){
        this.context = context;

        //Initializing dialog
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        //Finding view by ID
        progressBar = dialog.findViewById(R.id.progress);
        progress_text = dialog.findViewById(R.id.progress_text);

        //Changing typefaces
        progress_text.setTypeface(new Typefaces(context).ralewayMedium());

        //Making dialog non-cancellable
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    public void makeDeterminate(){
        progressBar.setIndeterminate(false);
        progressBar.setMaxProgress(100);
        progress_text.setVisibility(View.VISIBLE);
    }

    public void setProgress(final double progress){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                DecimalFormat decimalFormat = new DecimalFormat("#");
                String progressText = String.valueOf(decimalFormat.format(progress))+"%";
                progressBar.setProgress((float) progress);
                progress_text.setText(progressText);
            }
        });
    }

    public void show(){
        if (!dialog.isShowing()){
            dialog.show();
        }
    }

    public void dismiss(){
        if (dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
