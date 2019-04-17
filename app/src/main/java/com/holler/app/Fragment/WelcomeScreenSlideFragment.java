package com.holler.app.Fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.holler.app.R;

public class WelcomeScreenSlideFragment extends Fragment {

    private static final String ARG_HEADER = "wssf_header";
    private static final String ARG_DESCRIPTION= "wssf_description";
    private static final String ARG_IMAGE = "wssf_image";

    private int headerResource;
    private int descriptionResource;
    private int imageResource;


    public static WelcomeScreenSlideFragment newInstance(int headerResource, int descriptionResource, int imageResource) {

        final WelcomeScreenSlideFragment f = new WelcomeScreenSlideFragment ();

        final Bundle args = new Bundle();
        args.putInt(ARG_HEADER, headerResource);
        args.putInt(ARG_DESCRIPTION, descriptionResource);
        args.putInt(ARG_IMAGE, imageResource);
        f.setArguments(args);

        return f;
    }

    public WelcomeScreenSlideFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            this.headerResource = getArguments().getInt(ARG_HEADER);
            this.descriptionResource = getArguments().getInt(ARG_DESCRIPTION);
            this.imageResource = getArguments().getInt(ARG_IMAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_welcome_slide, container, false);
        TextView headerView = (TextView) v.findViewById(R.id.ma_header);
        TextView descriptionView = (TextView) v.findViewById(R.id.ma_description);
        ImageView imageView = (ImageView) v.findViewById(R.id.ma_image);

        headerView.setText(headerResource);
        descriptionView.setText(descriptionResource);
        imageView.setImageResource(imageResource);
        return v;
    }

}
