package tofira.imagepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.providers.GenericFileProvider;


/**
 * Created by Mickael on 10/10/2016.
 */

public abstract class PickerManager {
    public static final int REQUEST_CODE_SELECT_IMAGE = 200;
    public static final int REQUEST_CODE_IMAGE_PERMISSION = 201;
    protected Uri mProcessingPhotoUri;
    private boolean withTimeStamp = true;
    private String folder = null;
    private String imageName;
    private Uri imageFileUri;
    protected Activity activity;
    private UCrop uCrop;
    protected PickerBuilder.onImageReceivedListener imageReceivedListener;
    protected PickerBuilder.onPermissionRefusedListener permissionRefusedListener;
    private int cropActivityColor = Color.MAGENTA;
    public PickerManager setOnImageReceivedListener(PickerBuilder.onImageReceivedListener listener) {
        this.imageReceivedListener = listener;
        return this;
    }

    public PickerManager setOnPermissionRefusedListener(PickerBuilder.onPermissionRefusedListener listener) {
        this.permissionRefusedListener = listener;
        return this;
    }

    public PickerManager(Activity activity) {
        this.activity = activity;
        this.imageName = activity.getString(R.string.app_name);
    }


    public void pickPhotoWithPermission()
    {
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_IMAGE_PERMISSION);
        }
        else
            sendToExternalApp();
    }

    public void handlePermissionResult(int[] grantResults)
    {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted
            sendToExternalApp();

        } else {

            // permission denied
            if(permissionRefusedListener != null)
                permissionRefusedListener.onPermissionRefused();
            activity.finish();
        }
    }


    protected abstract void sendToExternalApp();

    protected Uri getImageFile()
    {
        if (this.imageFileUri != null) {
            return this.imageFileUri;
        }

        String imagePathStr = Environment.getExternalStorageDirectory() + "/" +
                (folder == null ?
                "Pictures/" + activity.getString(R.string.app_name) :
                folder);

        File path = new File(imagePathStr);
        if (!path.exists() && !path.mkdir()) {
            throw new RuntimeException("Path doesn't exist and could not be created.");
        }

        String finalPhotoName = imageName +
            (withTimeStamp ? "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date(System.currentTimeMillis())) :  "")
            + ".jpg";

        // long currentTimeMillis = System.currentTimeMillis();
        // String photoName = imageName + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date(currentTimeMillis)) + ".jpg";
        File photo = new File(path, finalPhotoName);
        this.imageFileUri = GenericFileProvider.getUriForFile(
                    this.activity,
                    this.activity.getApplicationContext().getPackageName() + ".providers",
                    photo);

        return this.imageFileUri;
    }

    public void setUri(Uri uri)
    {

    }

    public void startCropActivity()
    {
        if(uCrop == null) {
            UCrop.Options options = new UCrop.Options();
            options.setFreeStyleCropEnabled(false);
            options.setToolbarColor(cropActivityColor);
            options.setStatusBarColor(cropActivityColor);
            options.setActiveWidgetColor(cropActivityColor);

            uCrop = UCrop
                    .of(mProcessingPhotoUri, getImageFile())
                    .withAspectRatio(1, 1)
                    .withOptions(options);
        }

        uCrop.start(activity);
    }

    public void handleCropResult(Intent data)
    {
        Uri resultUri = UCrop.getOutput(data);
        if(imageReceivedListener != null)
            imageReceivedListener.onImageReceived(resultUri);

        activity.finish();
    }


    public PickerManager setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public PickerManager setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public PickerManager setCropActivityColor(int cropActivityColor) {
        this.cropActivityColor = cropActivityColor;
        return this;
    }

    public PickerManager withTimeStamp(boolean withTimeStamp) {
        this.withTimeStamp = withTimeStamp;
        return this;
    }

    public PickerManager setImageFolderName(String folder) {
        this.folder = folder;
        return this;
    }

    public PickerManager setCustomizedUcrop(UCrop customizedUcrop) {
        this.uCrop = customizedUcrop;
        return this;
    }
}
