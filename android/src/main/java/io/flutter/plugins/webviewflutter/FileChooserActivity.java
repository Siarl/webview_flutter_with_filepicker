package android.src.main.java.io.flutter.plugins.webviewflutter;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

  private static final int PERMISSION_REQUEST_CODE = 383938;
  private static final String[] REQUIRED_PERMISSIONS = {
          Manifest.permission.CAMERA,
          Manifest.permission.READ_EXTERNAL_STORAGE
  };


  public static void getfilePathCallback(ValueCallback<Uri[]> filePathCallback){
    mUploadMessages = filePathCallback;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    overridePendingTransition(0,0);

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

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
    fileIntent.setType("*/*");
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

    if (EasyPermissions.hasPermissions(this, REQUIRED_PERMISSIONS)) {
      openImageIntent(true);
    } else if (EasyPermissions.somePermissionPermanentlyDenied(
            this, Arrays.asList(REQUIRED_PERMISSIONS))) {
      new AppSettingsDialog.Builder(this).build().show();
    } else {
      EasyPermissions.requestPermissions(this,
              "This app needs access to your camera and files to upload a profile picture.",
              PERMISSION_REQUEST_CODE, REQUIRED_PERMISSIONS);
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
          int requestCode, @NonNull @NotNull List<String> perms) {

    if (perms.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
      openImageIntent(perms.contains(Manifest.permission.CAMERA));
    } else {
      finish();
    }

  }

  @Override
  public void onPermissionsDenied(
          int requestCode, @NonNull @NotNull List<String> perms) {
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