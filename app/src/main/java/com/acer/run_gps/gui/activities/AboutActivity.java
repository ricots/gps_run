package com.acer.run_gps.gui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.acer.run_gps.R;
import com.acer.run_gps.gui.message.ToastFactory;

import java.util.ArrayList;

/**
 * Created by Fabian on 28.12.2015.
 */
public class AboutActivity extends Activity {

    private ListView infos = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        infos = (ListView) findViewById(R.id.infos);

        ArrayList<String> titles = new ArrayList();
        titles.add(getResources().getString(R.string.libraries));
        titles.add(getResources().getString(R.string.source_code));
        titles.add(getResources().getString(R.string.privacy_policy));
        titles.add(getResources().getString(R.string.credits));
        titles.add(getResources().getString(R.string.report_bug));
        titles.add(getResources().getString(R.string.rate_this_app));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, titles);
        infos.setAdapter(adapter);

        infos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent libraries = new Intent(AboutActivity.this, LibrariesActivity.class);
                        startActivity(libraries);
                        break;
                    case 1:
                        Intent gitHub = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getResources().getString(R.string.github)));
                        startActivity(gitHub);
                        break;
                    case 2:
                        Intent privacy = new Intent(AboutActivity.this, PrivacyPolicy.class);
                        startActivity(privacy);
                        break;
                    case 3:
                        Intent credits = new Intent(AboutActivity.this, CreditsActivity.class);
                        startActivity(credits);
                        break;
                    case 4:
                        try {
                            Intent mail = new Intent(Intent.ACTION_SENDTO);
                            mail.setType("text/plain");
                            mail.setData(Uri
                                    .parse(getResources().getString(R.string.mailto)));
                            mail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mail.putExtra(Intent.EXTRA_SUBJECT,
                                    getResources().getString(R.string.app_name) + " " +
                                            getResources().getString(R.string.version));
                            startActivity(mail);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            ToastFactory.makeToast(AboutActivity.this, getResources().getString(R
                                    .string.email));
                        }
                        break;
                    case 5:
                        try {
                            Intent googlePlayDirect = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(getResources().getString(R.string.market)));
                            startActivity(googlePlayDirect);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            // Play Store is not installed, open the link to the app in the Browser
                            Intent googlePlayBrowser = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(getResources().getString(R.string.play_store)));
                            startActivity(googlePlayBrowser);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }
}