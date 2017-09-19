package tofira.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.providers.GenericFileProvider;

/**
 * Created by Mickael on 10/10/2016.
 *
 * TODO: Current implementation not working. Fix
 */

public class CameraPickerManager extends PickerManager {
    private Uri imageFileUri;

    public CameraPickerManager(Activity activity) {
        super(activity);
    }

    protected void sendToExternalApp()
    {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        mProcessingPhotoUri = getImageFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mProcessingPhotoUri);
        activity.startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    protected Uri getImageFile()
    {
        if (this.imageFileUri != null) {
            return this.imageFileUri;
        }

        String imagePathStr = Environment.getExternalStorageDirectory() + "/" +
                (this.getImageFolderName() == null ?
                        "Pictures/" + activity.getString(R.string.app_name) :
                        this.getImageFolderName());

        File path = new File(imagePathStr);
        if (!path.exists() && !path.mkdir()) {
            throw new RuntimeException("Path doesn't exist and could not be created.");
        }

        String finalPhotoName = this.getImageName() +
                (this.isWithTimeStamp() ? "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date(System.currentTimeMillis())) :  "")
                + ".jpg";

        // long currentTimeMillis = System.currentTimeMillis();
        // String photoName = imageName + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date(currentTimeMillis)) + ".jpg";
        File photo = new File(path, finalPhotoName);
        this.imageFileUri = GenericFileProvider.getUriForFile(this.activity, "fi.jamk.saunaapp.providers", photo);
        return this.imageFileUri;
    }
}
