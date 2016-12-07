package com.acer.run_gps.gui.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.acer.run_gps.R;
import com.acer.run_gps.gui.adapter.ActivityAdapter;
import com.acer.run_gps.gui.message.ToastFactory;
import com.acer.run_gps.model.DBAccessHelper;
import com.acer.run_gps.model.Track;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fabian on 29.12.2015.
 */
public class ActivitiesActivity extends Activity {

    // UI Widgets
    private ExpandableListView expListView = null;
    private TextView empty = null;

    // Used for the ExpandableListView
    private List<String> parentStrings = null;
    private List<com.acer.run_gps.model.Activity> childActivities = null;
    private Map<String, ArrayList<com.acer.run_gps.model.Activity>> groupCollection = null;

    // CAB
    private ArrayList<com.acer.run_gps.model.Activity> selection = null;

    // Used to refresh data in ExpandableListView
    private ActivityAdapter activityAdapter = null;

    // Actual Track
    private Track track = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activities_activity);

        expListView = (ExpandableListView) findViewById(R.id.listViewActivities);
        empty = (TextView) findViewById(R.id.activitiesEmpty);
        // registerForContextMenu(expListView);
        selection = new ArrayList();

        Intent intent = getIntent();
        track = (Track) intent.getSerializableExtra("track");
        getActionBar().setTitle(track.getName());

        childActivities = track.getActivities();
        if (childActivities == null) {
            childActivities = new ArrayList<>();
            empty.setVisibility(View.VISIBLE);
            empty.setText(getResources().getString(R.string.activities_empty));
        } else {
            empty.setVisibility(View.GONE);
        }

        setUpParentsAndChilds();

        activityAdapter = new ActivityAdapter(this, parentStrings,
                groupCollection);
        expListView.setAdapter(activityAdapter);
        expListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        expListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean
                    checked) {
                if (expListView.getItemAtPosition(position) instanceof com
                        .acer.run_gps.model.Activity) {
                    mode.getMenuInflater().inflate(R.menu.contextual_action_bar_menu_delete, mode
                            .getMenu());
                    int aid = ((com.acer.run_gps.model.Activity) (expListView.getItemAtPosition
                            (position))).getId();
                    com.acer.run_gps.model.Activity activity = null;
                    for (com.acer.run_gps.model.Activity a : childActivities) {
                        if (a.getId() == aid) {
                            activity = a;
                        }
                    }
                    if (checked) {
                        selection.add(activity);

                    } else {
                        selection.remove(activity);
                    }
                    mode.setTitle(expListView.getCheckedItemCount() + getResources().getString(R
                            .string.select));
                } else {
                    if (position == childActivities.size()) {
                        position--;
                    }
                    mode.getMenuInflater().inflate(R.menu.contextual_action_bar_empty, mode
                            .getMenu());
                    int idType = getResources().getIdentifier(childActivities.get(position)
                                    .getType
                                            ().toString().toLowerCase(),
                            "string", getPackageName());
                    mode.setTitle(getResources().getString(idType));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete) {
                    final ActionMode modeFinal = mode;
                    final MenuItem itemFinal = item;

                    new AlertDialog.Builder(ActivitiesActivity.this)
                            .setTitle(getResources().getString(R.string.dialog_back_pressed_title))
                            .setMessage(getResources().getString(R.string
                                    .dialog_delete_activity_message))
                            .setPositiveButton(android.R.string.yes, new DialogInterface
                                    .OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    for (com.acer.run_gps.model.Activity Item : selection) {
                                        if (DBAccessHelper.getInstance(ActivitiesActivity.this)
                                                .deleteActivity
                                                        (Item) != 0) {
                                            ToastFactory.makeToast(ActivitiesActivity.this,
                                                    getResources()
                                                            .getString(R.string
                                                                    .toast_delete_activity_error));
                                        } else {
                                            int indexToExpand = -1;
                                            childActivities.remove(Item);
                                            // Finish Activity if Track has no Activities
                                            if (childActivities.size() == 0) {
                                                finish();
                                            } else {
                                                // Check which group must be expanded
                                                childActivities.add(Item);
                                                int id1 = getResources().getIdentifier(Item.getType
                                                                ().toString().toLowerCase(),
                                                        "string", getPackageName());
                                                String type = getResources().getString(id1);
                                                Object[] types = groupCollection.keySet().toArray();
                                                for (int i = 0; i < types.length;
                                                     i++) {
                                                    if (types[i].equals(type) &&
                                                            groupCollection.get(types[i]).size()
                                                                    != 1) {
                                                        indexToExpand = i;
                                                    }
                                                }
                                            }
                                            childActivities.remove(Item);
                                            itemFinal.setVisible(false);
                                            // Refresh data
                                            onCreate(null);
                                            activityAdapter.notifyDataSetChanged();

                                            // Expand the actual group in ExpandableListView
                                            if (indexToExpand != -1) {
                                                expListView.expandGroup(indexToExpand);
                                            }
                                            // Close the dialog
                                            dialog.dismiss();
                                            // Close CAB
                                            modeFinal.finish();
                                        }
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface
                                    .OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Close the dialog
                                    dialog.dismiss();
                                    // Close CAB
                                    modeFinal.finish();
                                }
                            })
                            .create().show();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selection.clear();
            }
        });

        // If there is only one group in the ListView, then expand it
        if (parentStrings.size() == 1) {
            expListView.expandGroup(0);
        }

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(ActivitiesActivity.this, MapsActivity.class);
                intent.putExtra("activity", groupCollection.get(parentStrings.get(groupPosition))
                        .get(childPosition));
                startActivity(intent);
                return true;
            }
        });

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != previousGroup) {
                    expListView.collapseGroup(previousGroup);
                }
                previousGroup = groupPosition;
            }
        });
    }

    private void setUpParentsAndChilds() {
        parentStrings = new ArrayList<>();
        groupCollection = new LinkedHashMap<>();

        // Iterate all Activities
        for (com.acer.run_gps.model.Activity a1 : childActivities) {
            int id1 = getResources().getIdentifier(a1.getType().toString().toLowerCase(),
                    "string", getPackageName());
            String typeAsString = getResources().getString(id1);
            // Add every Type of Activity once
            if (!parentStrings.contains(typeAsString)) {
                parentStrings.add(getResources().getString(id1));
                ArrayList<com.acer.run_gps.model.Activity> temp = new ArrayList<>();
                // Iterate Activities again and link every Type with the right Activities
                for (com.acer.run_gps.model.Activity a2 : childActivities) {
                    int id2 = getResources().getIdentifier(a2.getType().toString().toLowerCase(),
                            "string", getPackageName());
                    if (getResources().getString(id2).equals(typeAsString)) {
                        temp.add(a2);
                    }
                }
                // Set the ranking in the group
                DBAccessHelper.getInstance(this).setRankingForActivitiesInTrack(temp);
                // Set the coordinates for each Activity
                for (com.acer.run_gps.model.Activity activity : temp) {
                    activity.setCoordinates(DBAccessHelper.getInstance(this).getCoordinates
                            (activity));
                }
                groupCollection.put(typeAsString, temp);
            }
        }
    }
}