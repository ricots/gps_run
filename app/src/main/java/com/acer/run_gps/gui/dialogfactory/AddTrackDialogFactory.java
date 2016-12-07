package com.acer.run_gps.gui.dialogfactory;

import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.acer.run_gps.R;
import com.acer.run_gps.gui.activities.SaveActivityActivity;
import com.acer.run_gps.model.DBAccessHelper;
import com.acer.run_gps.model.Track;

import java.util.Hashtable;

/**
 * Created by Fabian on 31.12.2015.
 */
public class AddTrackDialogFactory {

    private SaveActivityActivity saveActivityActivity = null;

    public AddTrackDialogFactory(SaveActivityActivity saveActivityActivity) {
        this.saveActivityActivity = saveActivityActivity;
    }

    public void makeCustomInputDialog() {
        LayoutInflater layoutInflater = saveActivityActivity.getLayoutInflater();
        final View promptView = layoutInflater.inflate(R.layout.add_change_track_dialog, null);

        // Set visibility of the dialog to final because it's used in an anonymous method
        final AlertDialog d = new AlertDialog.Builder(saveActivityActivity)
                .setPositiveButton(android.R.string.yes, null)
                .setNegativeButton(android.R.string.cancel, null)
                .setTitle(R.string.dialog_add_track_title)
                .setView(promptView)
                .create();

        // Important: Show the dialog here an not somewhere else
        d.show();

        // Set an OnClickListener to the positive button of the dialog.
        // This is needed because the dialog can not be closed until the user input is valid
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                // UI Widgets
                EditText name = (EditText) promptView.findViewById(R.id.dialog_name);
                TextView error = (TextView) promptView.findViewById(R.id.dialog_error);

                Track track = new Track();
                String input = name.getText().toString();
                if (input != null) {
                    track.setName(name.getText().toString());
                }
                int result = DBAccessHelper.getInstance(null).insertTrack(track);
                if (result == 0) {
                    // Inserting track was successful now the Spinner has to update
                    saveActivityActivity.setUpSpinners();
                    // Set the actual value in the spinner to the inserted Track
                    saveActivityActivity.spinnerTrack.setSelection(saveActivityActivity
                            .spinnerTrack.getAdapter().getCount() - 1);
                    // Finally close the dialog
                    d.dismiss();

                } else {
                    // An error occured and the error is set into the TextView
                    Hashtable<String, Integer> errors = track.getError();
                    if (error != null) {
                        if (errors.get("name") == Track.NAME_IS_NOT_SET) {
                            error.setVisibility(View.VISIBLE);
                            error.setText(saveActivityActivity.getResources().getString(R.string
                                    .dialog_error_no_name));
                        }
                        if (errors.get("name") == Track.NAME_ALREADY_EXISTS) {
                            error.setVisibility(View.VISIBLE);
                            error.setText(saveActivityActivity.getResources().getString(R.string
                                    .dialog_error_already_exists));
                        }
                    }
                }
            }
        });
    }
}