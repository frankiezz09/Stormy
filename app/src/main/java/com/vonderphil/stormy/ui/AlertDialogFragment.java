package com.vonderphil.stormy.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.vonderphil.stormy.R;

/**
 * Created by Administrator on 6/2/2015.
 */
public class AlertDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //get context of the activity where it was called.
        Context context = getActivity();
        //build the alert dialog on the activity where it was called.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //build the content of the alert dialog
        builder.setTitle(context.getString(R.string.error_title))
                .setMessage(context.getString(R.string.error_message))
                .setPositiveButton(context.getString(R.string.error_ok_button_message), null);
        //setNegativeButton optional

        //then finally create the dialog using the .create() method.
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
