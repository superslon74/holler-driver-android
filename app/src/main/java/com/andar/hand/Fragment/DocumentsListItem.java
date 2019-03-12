package com.andar.hand.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andar.hand.Activity.DocumentsActivity;
import com.andar.hand.BuildConfig;
import com.andar.hand.R;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DocumentsListItem extends Fragment {

    private static final String ARG_DOCUMENT = "document";
    private static final String ARG_TEMP_FILE_URI = "temp";
    private static final int CHOOSE_FILE_REQUEST_CODE = 5113;
    private static final int REQUEST_READ_PERMISSIONS_CODE = 937;
    private static final int REQUEST_CAMERA_PERMISSIONS_CODE = 6411;

    private DocumentsActivity.Document document;

    private OnDocumentViewInteractions mListener;

    private ImageView imageView;
    private TextView documentNameView;
    private TextView fileNameView;

    private File capturedPhoto;

    public DocumentsListItem() {
    }

    public static DocumentsListItem newInstance(DocumentsActivity.Document d) {
        DocumentsListItem fragment = new DocumentsListItem();
        Bundle args = new Bundle();
        args.putParcelable(ARG_DOCUMENT, d);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            document = getArguments().getParcelable(ARG_DOCUMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_documents_list_item, container, false);
        imageView = (ImageView) view.findViewById(R.id.document_icon);
        documentNameView = (TextView) view.findViewById(R.id.document_name_text);
        fileNameView = (TextView) view.findViewById(R.id.file_name_text);

        setupView();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });

        return view;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSIONS_CODE);
        }else if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        }else{
            pickImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        checkPermissions();
    }

    private void pickImage() {

        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        Intent chooserIntent;

        try {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            capturedPhoto = createTemporaryFile();
            captureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", capturedPhoto));

            chooserIntent = Intent.createChooser(new Intent(), "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent, captureIntent});

        } catch (IOException ex) {
            ex.printStackTrace();
            chooserIntent = Intent.createChooser(new Intent(), "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        }

        startActivityForResult(chooserIntent, CHOOSE_FILE_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String imageUri;

        if (resultCode == Activity.RESULT_OK && requestCode==CHOOSE_FILE_REQUEST_CODE){
            if(data==null){
                imageUri = "file://" + capturedPhoto.getAbsolutePath();
            }else{
                deleteTemporaryFile();
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                imageUri = "file://"+imgDecodableString;
            }

            modifyDocumentWithNewLocalFile(imageUri);


        }else{
            deleteTemporaryFile();
        }

    }

    private void setupView(){

        documentNameView.setText(document.name);
        if (document.remoteUrl==null && document.localUrl==null) {
            imageView.setImageResource(R.drawable.ic_add_a_photo_black_24dp);
            fileNameView.setText("Document required. Tap icon to select file..");
        } else if(document.remoteUrl!=null){
            Glide
                    .with(getActivity())
                    .load(document.remoteUrl)
                    .centerCrop()
                    .into(imageView);
            fileNameView.setText("Uploaded to "+document.remoteUrl);

            imageView.setMaxHeight(200);
            imageView.setPadding(0,0,0,0);

        } else if(document.localUrl!=null){
            Glide
                    .with(getActivity())
                    .load(Uri.parse(document.localUrl))
                    .centerCrop()
                    .into(imageView);

            fileNameView.setText("Located at "+document.localUrl);
            imageView.setMaxHeight(200);
            imageView.setPadding(0,0,0,0);
        }
    }




    private void modifyDocumentWithNewLocalFile(String uri){
        document.remoteUrl=null;
        document.localUrl=uri;
        getArguments().putParcelable(ARG_DOCUMENT,document);
        setupView();
        mListener.onDocumentSelected(document);
    }

    private void deleteTemporaryFile(){
        capturedPhoto.delete();
    }

    private File createTemporaryFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        //put temp file location to arguments to prevent crash while device rotation
        getArguments().putString(ARG_TEMP_FILE_URI,"file://" + image.getAbsolutePath());
        return image;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDocumentViewInteractions) {
            mListener = (OnDocumentViewInteractions) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnDocumentViewInteractions {
        void onDocumentSelected(DocumentsActivity.Document document);
    }
}
