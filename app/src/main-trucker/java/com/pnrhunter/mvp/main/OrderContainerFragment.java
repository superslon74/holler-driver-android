package com.pnrhunter.mvp.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.activity.DaggerFragment;
import com.pnrhunter.mvp.utils.server.objects.order.RequestedOrderResponse;

import butterknife.BindView;
import butterknife.ButterKnife;


public class OrderContainerFragment extends DaggerFragment {
    private RequestedOrderResponse currentRequest = null;

    @BindView(R.id.oc_order_container) protected View orderContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_container, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    public void setOrderRequest(RequestedOrderResponse order) {
        //TODO: switch by status
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment requestOrderFragment = RateOrderFragment.newInstance(order);

        fragmentManager
                .beginTransaction()
                .replace(orderContainer.getId(), requestOrderFragment)
                .commitNow();
    }
}
