package tofira.imagepicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import fi.jamk.saunaapp.R;

import static com.yalantis.ucrop.UCrop.REQUEST_CROP;

public class TempActivity extends AppCompatActivity {

    private static final String TAG = "ImagePicker";
    PickerManager pickerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.pickerManager = GlobalHolder.getInstance().getPickerManager();
        this.pickerManager.setActivity(TempActivity.this);
        this.pickerManager.pickPhotoWithPermission();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            if (resultCode != RESULT_CANCELED) {
                final Throwable cropError = UCrop.getError(data);
                if (cropError != null) {
                    Log.e(TAG, "handleCropError: ", cropError);
                    Toast.makeText(TempActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TempActivity.this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
                }
            }
            finish();
            return;
        }
        switch (requestCode) {
            case PickerManager.REQUEST_CODE_SELECT_IMAGE:
                Uri uri;
                if (data != null) uri = data.getData();
                else uri = pickerManager.getImageFile();

                pickerManager.setImageFile(uri);
                pickerManager.startCropActivity();
                break;
            case REQUEST_CROP:
                if (data != null) pickerManager.handleCropResult(data);
                else finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == PickerManager.REQUEST_CODE_IMAGE_PERMISSION)
            pickerManager.handlePermissionResult(grantResults);
        else finish();
    }

}
