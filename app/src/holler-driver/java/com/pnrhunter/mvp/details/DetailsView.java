package com.pnrhunter.mvp.details;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pnrhunter.HollerApplication;
import com.pnrhunter.R;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.DaggerTripDetailsComponent;
import com.pnrhunter.di.app.components.trips.modules.TripDetailsModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.utils.CustomActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class DetailsView extends CustomActivity implements TripDetailsPresenter.View {

    public static final String ARG_TYPE = "type";
    public static final String ARG_ID = "id";
    public static final String TYPE_PAST_TRIPS = "past_trips";
    public static final String TYPE_UPCOMING_TRIPS = "upcoming_trips";

    @Inject protected TripDetailsPresenter presenter;

    private String type;
    private String id;

    @BindView(R.id.h_title) protected TextView header;

    @BindView(R.id.deta_scroll_container) protected View scrollContainerView;
    @BindView(R.id.deta_gradient) protected View gradientView;
    @BindView(R.id.deta_buttons) protected View buttonsView;

    @BindView(R.id.deta_image_map) protected ImageView mapView;
    @BindView(R.id.deta_user_avatar) protected CircleImageView userAvatarView;
    @BindView(R.id.deta_user_name) protected TextView userNameView;
    @BindView(R.id.deta_user_rating) protected RatingBar userRatingView;
    @BindView(R.id.deta_user_date) protected TextView dateView;

    @BindView(R.id.deta_scheme_start) protected View schemeStartView;
    @BindView(R.id.deta_scheme_finish) protected View schemeFinishView;
    @BindView(R.id.deta_scheme_trip) protected View schemeTripView;

    @BindView(R.id.deta_address_start) protected TextView startAddressView;
    @BindView(R.id.deta_address_finish) protected TextView finishAddressView;

    @BindView(R.id.deta_booking_id) protected TextView bookingIdView;

    @BindView(R.id.deta_payment_type) protected TextView paymentTypeView;
    @BindView(R.id.deta_payment_amount) protected TextView paymentAmountView;

    @BindView(R.id.deta_comment) protected TextView commentView;
    @BindView(R.id.deta_comment_block) protected View commentBlockView;

    @BindView(R.id.deta_button_start) protected View startRideButton;
    @BindView(R.id.deta_button_cancel) protected View cancelRideButton;
    @BindView(R.id.deta_button_receipt) protected View showDetailsButton;
    @BindView(R.id.deta_button_close) protected View hideDetailsButton;

    @BindView(R.id.deta_invoice) protected View invoiceView;
    @BindView(R.id.deta_inv_value_booking) protected TextView invoiceBookingView;
    @BindView(R.id.deta_inv_value_distance) protected TextView invoiceDistanceView;
    @BindView(R.id.deta_inv_value_time) protected TextView invoiceTimeView;
    @BindView(R.id.deta_inv_value_base_fare) protected TextView invoiceBaseFareView;
    @BindView(R.id.deta_inv_value_distance_fare) protected TextView invoiceDistanceFareView;
    @BindView(R.id.deta_inv_value_tax) protected TextView invoiceTaxView;
    @BindView(R.id.deta_inv_value_total) protected TextView invoiceTotalView;
    @BindView(R.id.deta_inv_value_amount) protected TextView invoiceAmountView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        buildComponent();
        ButterKnife.bind(this);
        try{
            Intent intent = getIntent();
            type = intent.getExtras().getString(ARG_TYPE);
            id = intent.getExtras().getString(ARG_ID);
            presenter.requestData(type,id);
        }catch (NullPointerException e){

        }
    }

    private void buildComponent() {
        AppComponent component = HollerApplication.getInstance().component();
        DaggerTripDetailsComponent.builder()
                .appComponent(component)
                .tripDetailsModule(new TripDetailsModule(this))
                .build()
                .inject(this);
    }

    @OnClick(R.id.deta_button_receipt)
    public void showInvoice(){
        invoiceView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.deta_button_close)
    public void hideInvoice(){
        invoiceView.setVisibility(View.GONE);
    }

    @OnClick(R.id.deta_button_cancel)
    public void cancelRide(){
        presenter.cancelRide();
    }

    @OnClick(R.id.deta_button_start)
    public void startRide(){
        presenter.startRide();
    }

    @Override
    public void setView(final RetrofitModule.ServerAPI.OrderResponse order) {
        runOnUiThread(() -> {
            scrollContainerView.setVisibility(View.VISIBLE);
            buttonsView.setVisibility(View.VISIBLE);
            gradientView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(order.mapImage)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(mapView);

//            USER BLOCK
            if(!order.hasUser()){
                userAvatarView.setVisibility(View.GONE);
                userNameView.setVisibility(View.GONE);
            }else{
                Glide.with(this)
                        .load(order.user.avatar)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(userAvatarView);
                userNameView.setText(order.user.firstName+" "+order.user.lastName);
            }
            if(!order.hasRating()){
                userRatingView.setVisibility(View.GONE);
            }else{
                userRatingView.setRating(order.user.getRating());
            }
            dateView.setText(order.getFormatedDate());

//            ADDRESS BLOCK
            if(!order.hasStartAddress()){
                schemeStartView.setVisibility(View.GONE);
                startAddressView.setVisibility(View.GONE);
            }else{
                startAddressView.setText(order.sAddress);
            }

            if(!order.hasFinishAddress()){
                schemeFinishView.setVisibility(View.GONE);
                finishAddressView.setVisibility(View.GONE);
            }else{
                finishAddressView.setText(order.dAddress);
            }

            if(!order.hasStartAddress() || !order.hasFinishAddress()){
                schemeTripView.setVisibility(View.GONE);
            }

//            BOOKING ID / PAYMENT / COMMENT
            bookingIdView.setText(order.bookingId);
            paymentTypeView.setText(order.paymentMode);
            paymentAmountView.setText(order.paid);
            if(order.hasComment()){
                commentView.setText(order.rating.comment);
            }else{
                commentView.setText(getString(R.string.detas_no_comment));
            }

//            BUTTONS
            if(DetailsView.TYPE_PAST_TRIPS.equals(type)){
                cancelRideButton.setVisibility(View.GONE);
                startRideButton.setVisibility(View.GONE);
                showDetailsButton.setVisibility(View.VISIBLE);
                header.setText(getString(R.string.detas_header_past_trips));
            }else{
                cancelRideButton.setVisibility(View.VISIBLE);
                startRideButton.setVisibility(View.VISIBLE);
                showDetailsButton.setVisibility(View.GONE);
                header.setText(getString(R.string.detas_header_upcoming_trips));
            }

//            INVOICE
            if(order.hasPayment()){
                invoiceBookingView.setText(order.bookingId);
                invoiceDistanceView.setText(order.distance);
                invoiceTimeView.setText(order.travelTime);
                invoiceBaseFareView.setText(order.payment.fixed);
                invoiceDistanceFareView.setText(order.payment.distance);
                invoiceTaxView.setText(order.tax);
                invoiceTotalView.setText(order.payment.total);
                invoiceAmountView.setText(order.payment.payable);
            }else{
                showDetailsButton.setVisibility(View.GONE);
            }

        });
    }

    //TODO: start ride
    //TODO: cancel ride

}
