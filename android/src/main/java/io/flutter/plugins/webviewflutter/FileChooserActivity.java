package io.flutter.plugins.webviewflutter;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.pm.PackageManager;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import androidx.annotation.NonNull;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import android.widget.Toast;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/*
 * Used by our version of FlutterWebView.java to assist with file inputs.
 *
 * Based on https://gist.github.com/tneotia/adc61196cfa59a5a7bdef6efb0ab5883
 */
public class FileChooserActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    private static ValueCallback<Uri[]> mUploadMessages;
    private Object Utils;
    private File outputFile;
    private Uri mOutputFileUri;
    private Uri image;
    private Uri outputFileUri;
    private String mimeType;

    private static final int PERMISSION_REQUEST_CODE = 383938;
    public static final String EXTRA_MIME_TYPE =
            "io.flutter.plugins.webviewflutter.FileChooserActivity.EXTRA_MIME_TYPE";


    public static void getfilePathCallback(ValueCallback<Uri[]> filePathCallback){
        mUploadMessages = filePathCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0,0);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        if (getIntent().hasExtra(EXTRA_MIME_TYPE)) {
            mimeType = getIntent().getExtras().getString(EXTRA_MIME_TYPE);
        }

        if (mimeType == null || mimeType.length() == 0) {
            mimeType = "*/*";
        }

        requestPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            openImageIntent(true);
        } else {
            Uri[] results = null;
            try {
                if (resultCode != RESULT_OK) {
                    results = null;
                } else {
                    if (intent != null) {
                        String dataString = intent.getDataString();
                        ClipData clipData = intent.getClipData();
                        if (clipData != null) {
                            results = new Uri[clipData.getItemCount()];
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                ClipData.Item item = clipData.getItemAt(i);
                                results[i] = item.getUri();
                            }
                        }
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    } else {
                        results = new Uri[]{mOutputFileUri};
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mUploadMessages.onReceiveValue(results);
            mUploadMessages = null;
            finish();
        }
    }

    private void openImageIntent(boolean shouldShowCamera) {
        try {
            outputFile = File.createTempFile("tmp", ".jpg", getCacheDir());
        } catch (IOException pE) {
            pE.printStackTrace();
        }
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FolderName");

        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        mOutputFileUri = Uri.fromFile(file);

        // Camera.
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri);

        // Filesystem.
        final Intent fileIntent = new Intent();
        fileIntent.setType(mimeType);
        fileIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(fileIntent, "Select Source");

        // Add the camera options.
        if (shouldShowCamera) {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});
        }
        startActivityForResult(chooserIntent, 42);
    }

    private void requestPermissions() {

        String[] permissions;
        if (Build.VERSION.SDK_INT < 33) {
            permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.CAMERA
            };
        }

        if (EasyPermissions.hasPermissions(this, permissions)) {
            openImageIntent(true);
        } else {
            EasyPermissions.requestPermissions(this,
                    "This app needs access to your camera and files to upload them.",
                    PERMISSION_REQUEST_CODE, permissions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(
            int requestCode, @NonNull List<String> perms) {
        if (perms.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            openImageIntent(perms.contains(Manifest.permission.CAMERA));
        } else if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED) {
                openImageIntent(perms.contains(Manifest.permission.CAMERA));
            } else {
                Toast.makeText(getApplicationContext(), "Allow the Photo permission.", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_MEDIA_IMAGES"}, 123123);
            }
        } else {
            finish();
        }

    }

    @Override
    public void onPermissionsDenied(
            int requestCode, @NonNull List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }

        if (perms.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            finish();
        }
    }
}