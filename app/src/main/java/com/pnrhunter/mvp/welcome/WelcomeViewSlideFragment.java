package com.pnrhunter.mvp.welcome;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pnrhunter.R;

public class WelcomeViewSlideFragment extends Fragment {

    private static final String ARG_HEADER = "wssf_header";
    private static final String ARG_DESCRIPTION= "wssf_description";
    private static final String ARG_IMAGE = "wssf_image";

    private int headerResource;
    private int descriptionResource;
    private int imageResource;


    public static WelcomeViewSlideFragment newInstance(int headerResource, int descriptionResource, int imageResource) {

        final WelcomeViewSlideFragment f = new WelcomeViewSlideFragment();

        final Bundle args = new Bundle();
        args.putInt(ARG_HEADER, headerResource);
        args.putInt(ARG_DESCRIPTION, descriptionResource);
        args.putInt(ARG_IMAGE, imageResource);
        f.setArguments(args);

        return f;
    }

    public WelcomeViewSlideFragment() {}

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
        ButterKnife.bind(this,v);
        return v;
    }
//
//    @OnClick(R.id.ma_image)
//    public void crash(){
//        throw new RuntimeException("Testing crash reports");
//    }


}
