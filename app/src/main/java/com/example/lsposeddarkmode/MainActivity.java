package com.example.lsposeddarkmode;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "dark_mode_prefs";

    private ChipGroup chipGroup;
    private AppAdapter adapter;
    private List<AppInfo> allApps = new ArrayList<>();

    private enum FilterMode { ALL, SYSTEM, USER }
    private FilterMode currentFilter = FilterMode.ALL;

    private SharedPreferences getPrefs() {
        try {
            return getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
        } catch (SecurityException e) {
            return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null) adapter.filter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filter(newText);
                return true;
            }
        });

        chipGroup = findViewById(R.id.chip_group);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipGroup.check(R.id.chip_all);
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chip_all) currentFilter = FilterMode.ALL;
            else if (id == R.id.chip_system) currentFilter = FilterMode.SYSTEM;
            else if (id == R.id.chip_user) currentFilter = FilterMode.USER;
            String text = searchView.getQuery().toString();
            adapter.filter(text);
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppAdapter();
        recyclerView.setAdapter(adapter);

        loadApps();
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(0);

        SharedPreferences prefs = getPrefs();

        allApps.clear();
        for (ApplicationInfo app : installedApps) {
            if (app.packageName.equals("com.example.lsposeddarkmode")) continue;
            boolean enabled = prefs.getBoolean(app.packageName, false);
            allApps.add(new AppInfo(app, pm, enabled));
        }

        allApps.sort((a, b) -> {
            if (a.enabled != b.enabled) return a.enabled ? -1 : 1;
            return a.label.compareToIgnoreCase(b.label);
        });

        adapter.setApps(allApps);
    }

    private static class AppInfo {
        final ApplicationInfo applicationInfo;
        final String packageName;
        final String label;
        final Drawable icon;
        final boolean isSystem;
        boolean enabled;

        AppInfo(ApplicationInfo ai, PackageManager pm, boolean enabled) {
            this.applicationInfo = ai;
            this.packageName = ai.packageName;
            this.label = pm.getApplicationLabel(ai).toString();
            this.icon = pm.getApplicationIcon(ai);
            this.isSystem = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            this.enabled = enabled;
        }
    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> implements Filterable {
        private List<AppInfo> originalApps = new ArrayList<>();
        private List<AppInfo> filteredApps = new ArrayList<>();
        private String currentQuery = "";

        void setApps(List<AppInfo> apps) {
            originalApps = new ArrayList<>(apps);
            refilter();
        }

        void filter(String query) {
            currentQuery = query != null ? query.toLowerCase() : "";
            refilter();
        }

        private void refilter() {
            filteredApps = new ArrayList<>();
            for (AppInfo app : originalApps) {
                if (currentFilter == FilterMode.SYSTEM && !app.isSystem) continue;
                if (currentFilter == FilterMode.USER && app.isSystem) continue;
                if (!currentQuery.isEmpty()
                        && !app.label.toLowerCase().contains(currentQuery)
                        && !app.packageName.toLowerCase().contains(currentQuery)) {
                    continue;
                }
                filteredApps.add(app);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppInfo app = filteredApps.get(position);
            holder.icon.setImageDrawable(app.icon);
            holder.label.setText(app.label);
            holder.packageName.setText(app.packageName);
            holder.checkBox.setChecked(app.enabled);

            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                app.enabled = isChecked;
                int indexInAll = originalApps.indexOf(app);
                if (indexInAll >= 0) {
                    originalApps.remove(indexInAll);
                    originalApps.add(0, app);
                }
                getPrefs().edit().putBoolean(app.packageName, isChecked).apply();
                refilter();
            });

            holder.itemView.setOnClickListener(v -> {
                holder.checkBox.setChecked(!app.enabled);
            });
        }

        @Override
        public int getItemCount() {
            return filteredApps.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    filter(constraint != null ? constraint.toString() : null);
                    FilterResults results = new FilterResults();
                    results.values = filteredApps;
                    results.count = filteredApps.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView label;
            TextView packageName;
            CheckBox checkBox;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.app_icon);
                label = itemView.findViewById(R.id.app_label);
                packageName = itemView.findViewById(R.id.app_package);
                checkBox = itemView.findViewById(R.id.app_checkbox);
            }
        }
    }
}
