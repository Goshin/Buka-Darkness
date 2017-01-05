package io.goshin.bukadarkness;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import io.goshin.bukadarkness.database.DatabaseBase;
import io.goshin.bukadarkness.sited.MangaSource;

public class MainActivity extends AppCompatActivity {

    private SourceListFragment sourceFragment;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseBase.init(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupTab();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) {
            tab.select();
        }
        sourceFragment.tryInstallSource(intent);
    }

    private void setupToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getResources().getString(R.string.app_name));
        }
        setSupportActionBar(mToolbar);
    }

    private void setupTab() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        sourceFragment = new SourceListFragment();
        adapter.addFragment(sourceFragment, getString(R.string.Sources));

        SettingsFragment settingsFragment = new SettingsFragment();
        adapter.addFragment(settingsFragment, getString(R.string.settings));

        if (viewPager != null) {
            viewPager.setAdapter(adapter);
        }

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName("pref");
            //noinspection deprecation
            preferenceManager.setSharedPreferencesMode(MODE_WORLD_READABLE);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);
            try {
                getPreferenceManager().findPreference("version_name").setTitle(getString(
                        R.string.version_detail,
                        getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0)
                                .versionName
                ));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            getPreferenceManager().findPreference("sited_version").setTitle(getString(
                    R.string.sited_detail,
                    MangaSource.getEngineVersionName()
            ));
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {

        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
