/*
 * Copyright (C) 2016 Tony Guyot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.tonyguyot.acronym;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    // tag for logging information
    private static final String TAG = "AcronymMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set custom font for the app title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Lobster-Regular.ttf");
        title.setTypeface(tf);

        // set the tabs and view pager
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) toolbar.findViewById(R.id.toolbar_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // set the callback for the FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // select the first tab
                viewPager.setCurrentItem(0);

                // set focus on edit text, clear it and display keyboard
                EditText ed = (EditText) viewPager.findViewById(R.id.query_entry);
                ed.requestFocus();
                ed.setText("");
                Utils.showKeyboard(MainActivity.this);
            }
        });
    }

    // helper method to setup the view pager
    private void setupViewPager(final ViewPager vp) {

        // create the adapater
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // set the view pager
        vp.setOffscreenPageLimit(2);
        vp.setAdapter(adapter);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int newPosition) {
                // a new page has been displayed...
                Log.d(TAG, "switching to page position #" + newPosition);
            }

            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
                // do nothing
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (vp.getCurrentItem() != 0) {
                        Utils.hideKeyboard(MainActivity.this, vp.getWindowToken());
                    }
                }
            }
        });
    }

    // adapter for the view pager
    private class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int pos) {
            switch (pos) {
                case 0: return new QueryFragment();
                case 1: return new HistoryFragment();
                case 2: return new InfoFragment();
                default: return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int pos) {
            switch (pos) {
                case 0: return getResources().getString(R.string.tab_query);
                case 1: return getResources().getString(R.string.tab_history);
                case 2: return getResources().getString(R.string.tab_info);
                default: return null;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // uncomment to add menu items
        /*
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }
}
