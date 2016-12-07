package com.acer.run_gps.gui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.acer.run_gps.R;
import com.acer.run_gps.model.Track;

import java.util.ArrayList;

/**
 * Created by Fabian on 29.12.2015.
 */
public class TrackAdapter extends ArrayAdapter<Track> {

    public TrackAdapter(Activity context, ArrayList<Track> tracks) {
        super(context, R.layout.track_item, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View ret = convertView;
        TrackHolder trackHolder;
        if (ret == null) {
            LayoutInflater layoutInflater = ((Activity) getContext())
                    .getLayoutInflater();
            ret = layoutInflater.inflate(R.layout.track_item, parent, false);
            trackHolder = new TrackHolder();
            trackHolder.name = (TextView) ret.findViewById(R.id.item_track_name);
            trackHolder.count = (TextView) ret.findViewById(R.id.item_track_count);
            ret.setTag(trackHolder);
        } else {
            trackHolder = (TrackHolder) ret.getTag();
        }
        Track t = getItem(position);
        trackHolder.name.setText(t.getName());
        ArrayList<com.acer.run_gps.model.Activity> activities = t.getActivities();
        if (activities == null) {
            trackHolder.count.setText("0" + getContext().getString(R.string.track_adapter_plural));
        } else {
            int count = activities.size();
            // Because it is one Activity set for this Track we don't need the 's' at the end of
            // the word 'activites'
            if (count == 1) {
                trackHolder.count.setText(String.valueOf(count) + getContext().getString(R.string
                        .track_adapter_singular));
            } else {
                trackHolder.count.setText(String.valueOf(count) + getContext().getString(R.string
                        .track_adapter_plural));
            }
        }
        return ret;
    }

    // Class holds all UI Widgets
    private class TrackHolder {
        TextView name = null;
        TextView count = null;
    }
}