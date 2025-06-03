package org.hcilab.projects.nlogx.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.service.AppDatabase;
import org.hcilab.projects.nlogx.Entity.AppFilterEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppFilterAdapter extends RecyclerView.Adapter<AppFilterAdapter.ViewHolder> {
    private final AppDatabase db;
    private List<AppFilter> appList;
    private final Context context;
    private final OnItemClickListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Executor executor = Executors.newSingleThreadExecutor();
    
    AppFilterAdapter(Context context, List<AppFilter> appList, OnItemClickListener onItemClickListener) {
        this.appList = appList;
        this.context = context;
        this.listener = onItemClickListener;
        db = AppDatabase.getInstance(context);
        
        // Load all app states from database initially
        loadAllAppStatesFromDb();
    }

    private void loadAllAppStatesFromDb() {
        executor.execute(() -> {
            for (int i = 0; i < appList.size(); i++) {
                AppFilter app = appList.get(i);
                AppFilterEntity savedApp = db.appFilterDao().getByPackage(app.getPackageName());
                if (savedApp != null) {
                    final int position = i;
                    app.setEnabled(savedApp.isEnabled());
                    mainHandler.post(() -> notifyItemChanged(position));
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_filter_browse, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(appList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public interface OnItemClickListener {
        void onItemClicked(AppFilter appFilter);
        void onToggleChanged(AppFilter appFilter, boolean isChecked);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout item;
        public ImageView imgIcon;
        public TextView txtAppName;
        public SwitchMaterial switchEnable;

        public ViewHolder(View itemView){
            super(itemView);

            item = itemView.findViewById(R.id.filter_item);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtAppName = itemView.findViewById(R.id.txtAppName);
            switchEnable = itemView.findViewById(R.id.switchEnable);
        }

        public void bind(AppFilter app, OnItemClickListener listener){
            if (app == null){
                return;
            }

            txtAppName.setText(app.getAppName());
            imgIcon.setImageDrawable(app.getIcon());
            
            // Temporarily remove listener to avoid triggering events during binding
            switchEnable.setOnCheckedChangeListener(null);
            switchEnable.setChecked(app.isEnabled());
            
            // Update background color based on app enabled state
            updateItemBackground(app.isEnabled());
            
            item.setOnClickListener(v -> listener.onItemClicked(app));

            // Re-attach the listener after setting the initial state
            switchEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                app.setEnabled(isChecked);
                updateItemBackground(isChecked);
                
                // Save changes to database on background thread
                executor.execute(() -> {
                    db.appFilterDao().insertOrUpdate(new AppFilterEntity(
                            app.getPackageName(), 
                            app.getAppName(), 
                            isChecked));
                    
                    Log.d("[DEBUG]", "Updated in DB: " + app.getAppName() + " enabled: " + isChecked);
                });
                
                listener.onToggleChanged(app, isChecked);
            });
        }
        
        private void updateItemBackground(boolean enabled) {
            item.setBackgroundColor(ContextCompat.getColor(context, 
                    enabled ? R.color.white : R.color.grey));
        }
    }
}