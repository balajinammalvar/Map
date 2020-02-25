package com.example.fusedlocationapi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertBox {

    protected Context context;
    public AlertBox(Context con) {
        // TODO Auto-generated constructor stub
        this.context = con;
    }


    public AlertDialog.Builder showAlertBox(String msg)
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                context);
        alertDialog.setMessage(msg);
        alertDialog.setTitle(context.getString(R.string.app_name));

        return alertDialog;


    }

    protected AlertDialog customView(){
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
//        View dialogView = inflater.inflate(R.layout.demo_launcher_screen, null);
//        alertDialog.setView(dialogView);
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
//                            ImageView sucess = (ImageView) dialogView.findViewById(R.id.success);


//        alertDialog.show();
        return alertDialog;
    }


    public void showAlertBoxWithBack(String msg)
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                context);
        alertDialog.setMessage(msg);
        alertDialog.setTitle(context.getResources().getString(R.string.app_name));
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ((Activity) context).onBackPressed();
            }
        });
        //alertDialog.setIcon(R.drawable.logo);
        alertDialog.show();

    }
}
