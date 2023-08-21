package com.example.calculater.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.calculater.R;
import com.example.calculater.adapters.MoreAdapter;
import com.example.calculater.databinding.ActivityMoreBinding;
import com.example.calculater.fragments.ImageFragment;
import com.example.calculater.fragments.VideoFragment;
import com.example.calculater.utils.CommonFunctions;
import com.example.calculater.utils.FileType;
import com.example.calculater.utils.SavedData;
import com.rajat.pdfviewer.PdfViewerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoreActivity extends BaseActivity<ActivityMoreBinding> {


    private final List<SavedData> selectedVideoUris = new ArrayList<>();
    private final List<File> imageUris = new ArrayList<>();
    private final List<File> videoUris = new ArrayList<>();
    private final List<File> audioUris = new ArrayList<>();
    private final List<File> documentUris = new ArrayList<>();
    private MoreAdapter fileAdapter;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri selectedFileUri = result.getData().getData();
            if (selectedFileUri != null) {
                FileType fileType = CommonFunctions.getFileType(this, selectedFileUri);
                if (fileType != null) {
                    File uriToAdd = CommonFunctions.saveFile(this, selectedFileUri, fileType);
                    if (uriToAdd != null) {
                        addToList(uriToAdd, fileType);
                    }
                }
            }
        }
    });
    private ActionMode actionMode;
    private Set<Integer> selectedPositions;
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.actionDelete) {
                deleteSelectedVideos();
                mode.finish();
                return true;
            } else if (item.getItemId() == R.id.restore) {
                restoreSelectedVideos();
                mode.finish();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selectedPositions.clear();
            fileAdapter.setSelectedPositions(selectedPositions);
        }
    };

    @Override
    ActivityMoreBinding getLayout() {
        return ActivityMoreBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileAdapter = new MoreAdapter(selectedVideoUris);
        binding.moreRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.moreRecyclerView.setAdapter(fileAdapter);
        onclickButton();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        getSavedData();
        AdapterFunction();
    }

    private void onclickButton() {

        binding.arrow.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.moreAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("*/*");
            String[] mimetypes = {"image/*", "video/*", "audio/*", "application/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            launcher.launch(intent);
        });

    }

    private void getSavedData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {

                imageUris.addAll(CommonFunctions.getFilesOne(FileType.IMAGE));
                videoUris.addAll(CommonFunctions.getFilesOne(FileType.VIDEO));
                audioUris.addAll(CommonFunctions.getFilesOne(FileType.AUDIO));
                documentUris.addAll(CommonFunctions.getFilesOne(FileType.DOCUMENT));

                for (File file : imageUris) {
                    selectedVideoUris.add(new SavedData(file, FileType.IMAGE));
                }
                for (File file : videoUris) {
                    selectedVideoUris.add(new SavedData(file, FileType.VIDEO));
                }
                for (File file : audioUris) {
                    selectedVideoUris.add(new SavedData(file, FileType.AUDIO));
                }
                for (File file : documentUris) {
                    selectedVideoUris.add(new SavedData(file, FileType.DOCUMENT));
                }

                int count = selectedVideoUris.size();
                fileAdapter.notifyItemRangeInserted(0, count);

                if (count > 0) {
                    binding.tvNoFilesYet.setVisibility(View.GONE);
                } else binding.tvNoFilesYet.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        selectedPositions = new HashSet<>();
    }

    private void AdapterFunction() {
        try {
            fileAdapter.setOnItemClickListener(new MoreAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                        new PdfViewerActivity();
                    } else {

                        switch (selectedVideoUris.get(position).fileType) {
                            case DOCUMENT:
                                startActivity(PdfViewerActivity.Companion.launchPdfFromPath(
                                        MoreActivity.this,
                                        "pdf_url",
                                        "Pdf title/name ",
                                        "pdf directory to save",
                                        false, false));
                                break;
                            case IMAGE:
                                Bundle bundle = new Bundle();
                                if (selectedVideoUris.get(position) != null) {
                                    bundle.putString("imagePath", String.valueOf(Uri.fromFile(selectedVideoUris.get(position).file)));
                                }
                                DialogFragment fragment = new ImageFragment();
                                fragment.setArguments(bundle);
                                fragment.show(getSupportFragmentManager(), "");
                                break;

                            case VIDEO:
                                Uri videoUri = Uri.fromFile(selectedVideoUris.get(position).file);
                                Bundle b = new Bundle();
                                b.putString("videoPath", videoUri.toString());
                                DialogFragment frag = new VideoFragment();
                                frag.setArguments(b);
                                frag.show(getSupportFragmentManager(), "");
                                break;

                            case AUDIO:
                                // Play audio
                                break;
                        }
                    }

                }

                @Override
                public boolean onItemLongClick(int position) {
                    if (actionMode == null) {
                        actionMode = startActionMode(actionModeCallback);
                    }
                    toggleSelection(position);
                    return true;
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
        }

    }

    // menu function
    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        fileAdapter.setSelectedPositions(selectedPositions);

        if (selectedPositions.isEmpty()) {
            actionMode.finish();
        } else {
            actionMode.invalidate();
            updateActionModeTitle();
        }

    }

    private void deleteSelectedVideos() {
        List<File> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedVideos.add(selectedVideoUris.get(position).file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CommonFunctions.moveToTrash(this, Uri.fromFile(selectedVideoUris.get(position).file));
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        fileAdapter.setSelectedPositions(selectedPositions);
        fileAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedVideoUris.size() > 0) {
            binding.tvNoFilesYet.setVisibility(View.GONE);
        } else binding.tvNoFilesYet.setVisibility(View.VISIBLE);

    }

    private void restoreSelectedVideos() {
        List<File> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedVideos.add(selectedVideoUris.get(position).file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileType fileType = CommonFunctions.getFileType(this, Uri.fromFile(selectedVideoUris.get(position).file));
                if (fileType != null) {
                    CommonFunctions.recoverFile(this, Uri.fromFile(selectedVideoUris.get(position).file), fileType);
//                    CommonFunctions.deleteFile(this, selectedVideoUris.get(position).fileUri);
                    selectedVideoUris.get(position).file.delete();
                }
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        fileAdapter.setSelectedPositions(selectedPositions);
        fileAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedVideoUris.size() > 0) {
            binding.tvNoFilesYet.setVisibility(View.GONE);
        } else binding.tvNoFilesYet.setVisibility(View.VISIBLE);

    }

    private void updateActionModeTitle() {
        int selectedCount = selectedPositions.size();
        actionMode.setTitle(String.valueOf(selectedCount));
    }

    private void addToList(File file, FileType fileType) {
        int positionToAdd = -1;
        switch (fileType) {
            case AUDIO:
                positionToAdd = (imageUris.size() + videoUris.size() + audioUris.size());
                audioUris.add(file);
                break;
            case VIDEO:
                positionToAdd = (imageUris.size() + videoUris.size());
                videoUris.add(file);
                break;
            case IMAGE:
                positionToAdd = (imageUris.size());
                imageUris.add(file);
                break;
            case DOCUMENT:
                positionToAdd = (imageUris.size() + videoUris.size() + audioUris.size() + documentUris.size());
                documentUris.add(file);
                break;
            default:
                break;
        }
        if (positionToAdd == -1) {
            selectedVideoUris.add(0, new SavedData(file, fileType));
            fileAdapter.notifyItemInserted(0);
        } else if (positionToAdd >= 0) {
            selectedVideoUris.add(positionToAdd, new SavedData(file, fileType));
            fileAdapter.notifyItemInserted(positionToAdd);
        }
    }

}