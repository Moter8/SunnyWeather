package pw.moter8.sunnyweather;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;

public class AlertDialogFragment extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .title(context.getString(R.string.error_title))
                .content(context.getString(R.string.error_message))
                .positiveText(context.getString(R.string.error_ok_button));
        AlertDialog dialog = builder.show();
        return dialog;
    }
}
