package com.example.pullcity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 *  プログレスダイアログ用のDialogFragment
 */
public class ProgressDialogFragment extends DialogFragment {	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getArguments().getString("message"));
        return dialog;
    }
}