package org.hcilab.projects.nlogx.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.service.AppDatabase;
import org.hcilab.projects.nlogx.Entity.AppFilterEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppFilterActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppFilterAdapter adapter;
    List<AppFilter> appList = new ArrayList<>();
    Context context;
    AppDatabase db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_notification);
        db = AppDatabase.getInstance(this);

        context = this.getApplicationContext();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.filter_list);
        recyclerView.setLayoutManager(layoutManager);

        // Load apps in background thread
        executor.execute(() -> {
            getCurrentInstalledPackage();
            Log.d("[DEBUG]", "Total installed app: " + appList.size());
            
            // Update UI on main thread
            mainHandler.post(this::updateAppList);
        });
    }

    private void getCurrentInstalledPackage(){
        appList.clear();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> packages = getPackageManager().queryIntentActivities(mainIntent, 0);

        final PackageManager pm = getPackageManager();
        for (ResolveInfo packageInfo : packages) {
            Drawable icon = getBaseContext().getPackageManager()
                    .getApplicationIcon(packageInfo.activityInfo.applicationInfo);
            String strAppName = packageInfo.activityInfo.applicationInfo.loadLabel(pm).toString();
            String strPackageName = packageInfo.activityInfo.applicationInfo.packageName;
            
            // Pre-load saved state from DB
            AppFilterEntity savedApp = db.appFilterDao().getByPackage(strPackageName);
            boolean isEnabled = false;
            if (savedApp != null) {
                isEnabled = savedApp.isEnabled();
            }
            
            appList.add(new AppFilter(strAppName, strPackageName, icon, isEnabled));
        }
    }

    private void updateAppList(){
        adapter = new AppFilterAdapter(this, appList, new AppFilterAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(AppFilter appFilter) {
                if (!appFilter.isEnabled()){
                    Toast.makeText(context, "Please enable the app to edit rules", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Go to regex editor screen
                Intent intent = new Intent(context, RegexEditorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("package_name", appFilter.getPackageName());
                intent.putExtra("app_name", appFilter.getAppName());
                context.startActivity(intent);
            }

            @Override
            public void onToggleChanged(AppFilter appFilter, boolean isChecked) {
                // The toggle state is already updated in the adapter
                Log.d("[DEBUG]", "Toggle changed: " + appFilter.getAppName() + " enabled: " + isChecked);
            }
        });

        recyclerView.setAdapter(adapter);
        if(adapter.getItemCount() == 0) {
            Toast.makeText(this, R.string.empty_log_file, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}