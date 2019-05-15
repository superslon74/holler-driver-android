package com.holler.app.mvp.profile;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.RadioButton;

import com.bumptech.glide.Glide;
import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.User;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerProfileComponent;
import com.holler.app.di.app.components.profile.modules.ProfileModule;
import com.holler.app.utils.CustomActivity;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.core.content.FileProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;


public class EditProfileView
        extends CustomActivity
        implements ProfilePresenter.View {


    @BindView(R.id.ep_avatar)
    protected CircleImageView avatarView;
    @BindView(R.id.ep_firstName_input)
    protected EditText firstNameView;
    @BindView(R.id.ep_lastName_input)
    protected EditText lastNameView;
    @BindView(R.id.ep_phone_input)
    protected EditText phoneView;
    @BindView(R.id.ep_email_input)
    protected EditText emailView;
    @BindView(R.id.ep_service_input)
    protected EditText serviceView;
    @BindView(R.id.ep_gender_male_option)
    protected RadioButton optionMale;
    @BindView(R.id.ep_gender_female_option)
    protected RadioButton optionFemale;

    @Inject
    protected ProfilePresenter presenter;

    private String newAvatarUrl = null;


    private void setupComponent() {
        AppComponent appComponent = (AppComponent) AndarApplication.getInstance().component();
        DaggerProfileComponent.builder()
                .appComponent(appComponent)
                .profileModule(new ProfileModule(this))
                .build()
                .inject(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        ButterKnife.bind(this);
        setupComponent();
        presenter.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(R.id.ep_button_save)
    public void saveChanges() {
        Logger.d("SAVE");
        String firstName = firstNameView.getText().toString();
        String lastName = lastNameView.getText().toString();
        String gender = (optionMale.isChecked())?User.GENDER_MALE:User.GENDER_FEMALE;
        String avatar = newAvatarUrl;
        presenter.sendChanges(avatar,firstName,lastName,gender);
    }

    @OnClick(R.id.ep_button_changePassword)
    public void goToForgotPassword() {
        presenter.goToForgotPassword();
    }

    @OnClick(R.id.ep_avatar)
    public void choosePhoto() {
        pickImage();
    }

    @Override
    public void setFields(User user) {
        runOnUiThread(() -> {
            Glide
                    .with(EditProfileView.this)
                    .load(user.getAvatarUrl())
                    .centerCrop()
                    .error(R.drawable.avatar)
                    .into(avatarView);
            firstNameView.setText(user.firstName);
            lastNameView.setText(user.lastName);
            emailView.setText(user.email);
            phoneView.setText(user.mobile);
            serviceView.setText(user.getServiceName());

            optionMale.setChecked(user.isMale());
            optionFemale.setChecked(!user.isMale());
        });
    }

    private void updateAvatarView(String imageUri) {
        Glide
                .with(EditProfileView.this)
                .load(Uri.parse("file://" + imageUri))
                .centerCrop()
                .into(avatarView);
        newAvatarUrl=imageUri;
    }

    private File capturedPhoto;
    private static int CHOOSE_FILE_REQUEST_CODE = 64005;

    protected void pickImage() {

        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/jpg", "image/png"};
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        Intent chooserIntent;

        try {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            capturedPhoto = createTemporaryFile();
            Uri photoUri = FileProvider.getUriForFile(
                    EditProfileView.this,
                    "com.holler.app.FileProvider",
                    capturedPhoto);

            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);

            chooserIntent = Intent.createChooser(new Intent(), "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent, captureIntent});

        } catch (Exception ex) {
            chooserIntent = Intent.createChooser(new Intent(), "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        }

        final CustomActivity activity = (CustomActivity)EditProfileView.this;
        activity.startActivityForResult(chooserIntent, CHOOSE_FILE_REQUEST_CODE);
    }

    private File createTemporaryFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File externalStorageDir = EditProfileView.this.getFilesDir();
        File appDir = new File(externalStorageDir,"HollerImages");
        File filesDir = EditProfileView.this.getFilesDir();
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                externalStorageDir
        );

        MediaScannerConnection.scanFile(
                EditProfileView.this,
                new String[] { image.toString() }, null,
                (path, uri) -> {
                    Logger.i("ExternalStorage", "Scanned " + path + ":");
                    Logger.i("ExternalStorage", "-> uri=" + uri);
                });
//        getArguments().putString(ARG_TEMP_FILE_URI, image.getAbsolutePath());

        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String imageUri;

        if (resultCode == Activity.RESULT_OK && requestCode == CHOOSE_FILE_REQUEST_CODE) {
            if (data == null) {
//                if (capturedPhoto == null)
//                    capturedPhoto = new File(getArguments().getString(ARG_TEMP_FILE_URI));
                imageUri = capturedPhoto.getAbsolutePath();
            } else {
                deleteTemporaryFile();
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = EditProfileView.this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                imageUri = imgDecodableString;
            }

            updateAvatarView(imageUri);
            Logger.d(imageUri);

        } else {
            deleteTemporaryFile();
        }

    }

    private void deleteTemporaryFile() {

        try {
            capturedPhoto.delete();
            Logger.e("Temp file removed");
        } catch (Exception e) {
            Logger.e("Can't remove temp file");
        }
    }
}
