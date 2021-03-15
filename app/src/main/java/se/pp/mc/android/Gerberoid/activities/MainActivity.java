/*
 * This file is part of the Gerberoid project.
 *
 * Copyright (C) 2017 Marcus Comstedt <marcus@mc.pp.se>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.pp.mc.android.Gerberoid.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.kennyc.bottomsheet.BottomSheetListener;
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import se.pp.mc.android.Gerberoid.GerberoidApplication;
import se.pp.mc.android.Gerberoid.adapters.LayerSpinnerAdapter;
import se.pp.mc.android.Gerberoid.gerber.DisplayOptions;
import se.pp.mc.android.Gerberoid.model.GerberZipEntry;
import se.pp.mc.android.Gerberoid.model.FileType;
import se.pp.mc.android.Gerberoid.tasks.GerberFile;
import se.pp.mc.android.Gerberoid.tasks.SourceDescriptor;
import se.pp.mc.android.Gerberoid.tasks.FileSourceDescriptor;
import se.pp.mc.android.Gerberoid.tasks.LayerLoadCallback;
import se.pp.mc.android.Gerberoid.tasks.LayerLoadTask;
import se.pp.mc.android.Gerberoid.tasks.LoadRequest;
import se.pp.mc.android.Gerberoid.gerber.GerberViewer;
import se.pp.mc.android.Gerberoid.gerber.Layers;
import se.pp.mc.android.Gerberoid.R;
import se.pp.mc.android.Gerberoid.gerber.ViewPort;
import se.pp.mc.android.Gerberoid.tasks.UriSourceDescriptor;
import se.pp.mc.android.Gerberoid.utils.Preferences;
import se.pp.mc.android.Gerberoid.views.ToolsDrawer;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_GERBER = 10001;
    private static final int REQUEST_DRILL = 10002;
    private static final int REQUEST_ARCHIVE = 10003;
    private static final int REQUEST_SELECT_ARCHIVE = 10004;

    private final List<GerberFile<? extends SourceDescriptor>> loadedFiles = new ArrayList<>();

    private GerberViewer gerber;
    private Layers layers;
    private DisplayOptions displayOptions;
    private ViewPort viewPort;
    private Spinner layerSpinner;
    private DrawerLayout drawerLayout;
    private View toolsDrawer;
    private View progress;

    private View btnAdd;
    private View btnClear;
    private View ivFullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar_bottom);

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> addLayers());

        btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> clearLayers());

        ivFullscreen = findViewById(R.id.ivFullscreen);
        ivFullscreen.setOnClickListener(view -> {
                showFullscreen();
            }

        );

        progress = findViewById(R.id.progress);

        gerber = (GerberViewer) findViewById(R.id.gerber_viewer);
        if (gerber != null) {
            gerber.onCreate();
            gerber.onRestoreInstanceState((savedInstanceState == null ? new Bundle() : savedInstanceState));
            layers = gerber.getLayers();
            viewPort = gerber.getViewPort();
            displayOptions = gerber.getDisplayOptions();
        }

        final Preferences prefs = ((GerberoidApplication)getApplication()).getPreferences();
        prefs.restoreDisplayOptions(displayOptions);
        prefs.restoreLayerColors(layers);

        layerSpinner = (Spinner) findViewById(R.id.layer_spinner);
        if (layerSpinner != null) {
            layerSpinner.setAdapter(new LayerSpinnerAdapter(this, layers));
            layerSpinner.setSelection(layers.getActiveLayer());
            layerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    layers.setActiveLayer(pos);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolsDrawer = findViewById(R.id.tools_drawer);
        if (toolsDrawer != null)
            new ToolsDrawer(this, toolsDrawer, displayOptions);
    }

    private void showFullscreen() {

        findViewById(R.id.toolbar_bottom).setVisibility(View.GONE);
        findViewById(R.id.toolbar).setVisibility(View.GONE);
        ivFullscreen.setVisibility(View.GONE);

        hideSystemUI();

    }

    private void exitFullscreen() {

        findViewById(R.id.toolbar_bottom).setVisibility(View.VISIBLE);
        findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
        ivFullscreen.setVisibility(View.VISIBLE);

        showSystemUI();

    }

    @Override
    public void onBackPressed() {

        if(ivFullscreen.getVisibility() == View.GONE){
            exitFullscreen();
            return;
        }

        super.onBackPressed();

    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (gerber != null)
            gerber.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        final Preferences prefs = ((GerberoidApplication)getApplication()).getPreferences();
        prefs.storeDisplayOptions(displayOptions);
        prefs.storeLayerColors(layers);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        drawerLayout = null;
        toolsDrawer = null;
        layerSpinner = null;
        layers = null;
        viewPort = null;
        displayOptions = null;
        if (gerber != null) {
            gerber.onDestroy();
            gerber = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                if (drawerLayout.isDrawerOpen(toolsDrawer))
                    drawerLayout.closeDrawer(toolsDrawer);
                else
                    drawerLayout.openDrawer(toolsDrawer);
                break;
            case R.id.action_zoom_in:
                viewPort.SetPreviousZoom();
                break;
            case R.id.action_zoom_out:
                viewPort.SetNextZoom();
                break;
            case R.id.action_zoom_fit:
                viewPort.Zoom_Automatique();
                break;
            case R.id.action_about:
                String version = "?";
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.action_about)
                        .setMessage(getResources().getString(R.string.about_text, version))
                        .create()
                        .show();
                break;
        }
        return true;
    }

    private void addLayers() {

        new BottomSheetMenuDialogFragment.Builder(this)
                .setSheet(R.menu.bottom_toolbar)
                .setTitle(R.string.add_layers_title)
                .setListener(new BottomSheetListener() {

                    @Override
                    public void onSheetShown(@NonNull BottomSheetMenuDialogFragment bottomSheet, @Nullable Object object) {

                    }

                    @Override
                    public void onSheetItemSelected(@NonNull BottomSheetMenuDialogFragment bottomSheet, MenuItem item, @Nullable Object object) {

                        switch (item.getItemId()) {

                            case R.id.action_gerber:
                                SelectFile(REQUEST_GERBER);
                                break;
                            case R.id.action_drill:
                                SelectFile(REQUEST_DRILL);
                                break;
                            case R.id.action_archive:
                                SelectFile(REQUEST_ARCHIVE);
                                break;


                        }

                    }

                    @Override
                    public void onSheetDismissed(@NonNull BottomSheetMenuDialogFragment bottomSheet, @Nullable Object object, int dismissEvent) {

                    }
                }).show(getSupportFragmentManager());

    }

    private void clearLayers() {
        layers.Clear_DrawLayers();
        layerSpinner.setSelection(layers.getActiveLayer());
    }

    private void SelectFile(GerberZipEntry[] files) {

        final List<LoadRequest<FileSourceDescriptor>> requests = new ArrayList<>();

        for (GerberZipEntry e : files) {

            if(e != null && e.getFile() != null && e.getType() != null) {
                requests.add(new LoadRequest<>(new GerberFile<>(new FileSourceDescriptor(e.getFile()), e.getType())));
            }

        }

        if(requests.size() > 0) {
            new LayerLoadTask(getApplicationContext(), layers, mLoadCallback).execute(requests.toArray(new LoadRequest[0]));
        }

    }

    private void SelectFile(int requestCode) {

        final Intent intent  = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(requestCode == REQUEST_ARCHIVE ? "application/zip" : "application/octet-stream");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && requestCode == REQUEST_GERBER) {
            String[] extraMimeTypes = {"application/vnd.gerber", "application/octet-stream"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && requestCode != REQUEST_ARCHIVE) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        startActivityForResult(intent, requestCode);

    }

    private final LayerLoadCallback mLoadCallback = new LayerLoadCallback() {

        @Override
        public void onFinished(boolean success, @NotNull List<? extends GerberFile<? extends SourceDescriptor>> files) {

            if(success) {
                loadedFiles.addAll(files);
            }

            progress.setVisibility(View.GONE);
            if (!success) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.dialog_error_title))
                        .setMessage(getString(R.string.dialog_title_file_not_loaded))
                        .setPositiveButton(getString(R.string.dialog_ok), null)
                        .show();

            }

        }

        @Override
        public void onStarted() {
            progress.setVisibility(View.VISIBLE);
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_ARCHIVE:

                if (resultCode == RESULT_OK) {

                    final ArchiveActivity.ResultSet files = (ArchiveActivity.ResultSet)data.getSerializableExtra(ArchiveActivity.EXTRA_FILES);
                    if (files != null) {
                        SelectFile(files.getData());
                    }

                }
                break;

            case REQUEST_ARCHIVE:

                if (resultCode == RESULT_OK) {
                    startActivityForResult(ArchiveActivity.newInstance(this, data.getData()), REQUEST_SELECT_ARCHIVE);
                }

                break;

            case REQUEST_GERBER:
            case REQUEST_DRILL:
                if (resultCode == RESULT_OK) {

                    ClipData clipData = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        clipData = data.getClipData();
                    }

                    if (clipData != null) {

                        final List<LoadRequest<UriSourceDescriptor>> loadRequests = new ArrayList<>();
                        final int n = clipData.getItemCount();
                        for (int i = 0; i < n; i++) {
                            loadRequests.add(new LoadRequest<>(new GerberFile<>(new UriSourceDescriptor(clipData.getItemAt(i).getUri()), requestCode == REQUEST_GERBER ? FileType.GERBER : FileType.DRILL)));
                        }

                        new LayerLoadTask(getApplicationContext(), layers, mLoadCallback).execute(loadRequests.toArray(new LoadRequest[0]));

                    } else {

                        final Uri uri = data.getData();
                        if(uri != null) {
                            new LayerLoadTask(getApplicationContext(), layers, mLoadCallback).execute(new LoadRequest<>(new GerberFile<>(new UriSourceDescriptor(uri), requestCode == REQUEST_GERBER ? FileType.GERBER : FileType.DRILL)));
                        }

                    }

                    layerSpinner.setSelection(layers.getActiveLayer());

                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    public Layers getLayers() {
        return layers;
    }

    public DisplayOptions getDisplayOptions() {
        return displayOptions;
    }



}


