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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
    private void setupViewPager(ViewPager vp) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addItem(new QueryFragment(), getResources().getString(R.string.tab_query));
        adapter.addItem(new HistoryFragment(), getResources().getString(R.string.tab_history));
        adapter.addItem(new InfoFragment(), getResources().getString(R.string.tab_info));
        vp.setAdapter(adapter);
        vp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // a new page has been displayed...

                // if this page is the history page, then refresh the history list
                if (position == 1) {
                    HistoryFragment frag = (HistoryFragment) adapter.getItem(1);
                    frag.refresh();
                }

                // if the new page is not the query page, then hide keyboard
                if (position != 0) {
                    QueryFragment frag = (QueryFragment) adapter.getItem(0);
                    frag.hideKeyboard();
                }
            }
        });
    }

    // adapter for the view pager
    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<String> mTitles = new ArrayList<>();
        private final List<Fragment> mFragments = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public Fragment getItem(int pos) {
            return mFragments.get(pos);
        }

        @Override
        public CharSequence getPageTitle(int pos) {
            return mTitles.get(pos);
        }

        public void addItem(Fragment fragment, String title) {
            mFragments.add(fragment);
            mTitles.add(title);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
