package com.pnrhunter.mvp.details;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pnrhunter.Models.AccessDetails;
import com.pnrhunter.R;
import com.pnrhunter.di.app.modules.RetrofitModule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class TripsListFragment extends Fragment {

    public static final String ARG_LIST = "list";

    @BindView(R.id.tlf_list_container) protected RecyclerView listContainer;
    @BindView(R.id.tlf_empty_list) protected View emptyListView;

    private ArrayList<RetrofitModule.ServerAPI.OrderResponse> orders;

    public static TripsListFragment newInstance(ArrayList<RetrofitModule.ServerAPI.OrderResponse> orders) {
        TripsListFragment fragment = new TripsListFragment();
        Bundle args = new Bundle();

        args.putParcelableArrayList(ARG_LIST, orders);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orders = getArguments().getParcelableArrayList(ARG_LIST);
        }
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trips_list, container, false);
        ButterKnife.bind(this, view);

        RecyclerTripsListAdapter adapter = new RecyclerTripsListAdapter(orders);
        if(orders==null || orders.size()==0){
            emptyListView.setVisibility(View.VISIBLE);
            listContainer.setVisibility(View.GONE);
        }else{
            listContainer.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
            listContainer.setItemAnimator(new DefaultItemAnimator());
            listContainer.setAdapter(adapter);
            emptyListView.setVisibility(View.GONE);
            listContainer.setVisibility(View.VISIBLE);
        }


//        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
//        errorLayout = (RelativeLayout) rootView.findViewById(R.id.errorLayout);
//        errorLayout.setVisibility(View.GONE);
//
//        toolbar = (LinearLayout) rootView.findViewById(R.id.lnrTitle);
//        backImg = (ImageView) rootView.findViewById(R.id.backArrow);
//
//
//        backImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getFragmentManager().popBackStack();
//            }
//        });
//
//        Bundle bundle = getArguments();
//        String toolbar = null;
//        if (bundle != null)
//            toolbar = bundle.getString("toolbar");
//
//        if (toolbar != null && toolbar.length() > 0) {
//            this.toolbar.setVisibility(View.VISIBLE);
//        }

        return view;
    }


    public class RecyclerTripsListAdapter extends RecyclerView.Adapter<RecyclerTripsListAdapter.TripViewHolder> {
        List<RetrofitModule.ServerAPI.OrderResponse> orders;

        public RecyclerTripsListAdapter(List<RetrofitModule.ServerAPI.OrderResponse> orders) {
            this.orders = orders;
        }

        @Override
        public RecyclerTripsListAdapter.TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_trips_list_item, parent, false);
            return new RecyclerTripsListAdapter.TripViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerTripsListAdapter.TripViewHolder holder, final int position) {
            final RetrofitModule.ServerAPI.OrderResponse currentOrder = orders.get(position);

            Glide
                    .with(getActivity())
                    .load(currentOrder.mapImage)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.mapView);

            if (currentOrder.isScheduled()) {
                holder.dateView.setText(generateDateRepresentation(currentOrder.scheduleAt));
            }else{
                holder.dateView.setText(generateDateRepresentation(currentOrder.createdAt));
                holder.cancelButton.setVisibility(View.GONE);
            }
            holder.bookingView.setText(getActivity().getString(R.string.detas_caption_booking)+": "+currentOrder.bookingId);


            if (currentOrder.hasService()) {
                holder.serviceView.setText(currentOrder.service.name);
                Glide
                        .with(getActivity())
                        .load(currentOrder.service.image)
                        .placeholder(R.drawable.car_select)
                        .error(R.drawable.car_select)
                        .dontAnimate()
                        .into(holder.avatarView);
            }else{
                holder.serviceView.setVisibility(View.GONE);
                holder.avatarView.setVisibility(View.GONE);
            }

            holder.view.setOnClickListener(v -> {
                ((TripsView)getActivity()).openDetails(currentOrder.id);
            });

            holder.cancelButton.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(AccessDetails.site_icon);
                builder.setMessage(getString(R.string.cencel_request))
                        .setCancelable(false)
                        .setPositiveButton("YES", (dialog, id) -> {
                            dialog.dismiss();
//                                cancelRequest(currentOrder);
                        })
                        .setNegativeButton("NO", (dialog, id) -> dialog.dismiss());
                AlertDialog alert = builder.create();
                alert.show();
            });

        }
        //TODO: get locale
        private String generateDateRepresentation(String date) {
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
                return new SimpleDateFormat("d'th' MMM yyyy 'at' hh.mm a", Locale.ENGLISH).format(d);
            } catch (ParseException e) {
                e.printStackTrace();
                return "Wrong date";
            }
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        public class TripViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tlif_image_map) protected ImageView mapView;
            @BindView(R.id.tlif_avatar) protected CircleImageView avatarView;

            @BindView(R.id.tlif_date_value) protected TextView dateView;
            @BindView(R.id.tlif_booking_value) protected TextView bookingView;
            @BindView(R.id.tlif_service_value) protected TextView serviceView;
            @BindView(R.id.tlif_amount) protected TextView amountView;

            @BindView(R.id.tlif_button_cancel) protected TextView cancelButton;
            protected View view;

            public TripViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(TripViewHolder.this,itemView);
                view = itemView;
            }
        }
    }



}
