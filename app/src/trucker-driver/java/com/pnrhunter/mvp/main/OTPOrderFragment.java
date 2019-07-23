package com.pnrhunter.mvp.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;
import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.server.objects.order.RequestedOrderResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;

public class OTPOrderFragment extends Fragment{

    @BindView(R.id.of_otp1) protected EditText otp1View;
    @BindView(R.id.of_otp2) protected EditText otp2View;
    @BindView(R.id.of_otp3) protected EditText otp3View;
    @BindView(R.id.of_otp4) protected EditText otp4View;

    public Observable<Boolean> source;
    private ObservableEmitter<Boolean> emitter;

    public OTPOrderFragment() {
        this.source = Observable.create(emitter -> {
            OTPOrderFragment.this.emitter = emitter;
        });
    }


    private static final String ARG_ADDRESS_FROM = "address_from";
    private static final String ARG_ADDRESS_TO = "address_to";
    private static final String ARG_WEIGHT = "weight";
    private static final String ARG_DATE = "date";
    private static final String ARG_PAYMENT = "payment";
    private static final String ARG_TIME = "time";

    public static OTPOrderFragment newInstance(RequestedOrderResponse order) {
        OTPOrderFragment fragment = new OTPOrderFragment();
        Bundle args = new Bundle();

//        args.putString(ARG_ADDRESS_FROM, order.order.sAddress);
//        args.putString(ARG_ADDRESS_TO, order.order.dAddress);
//        args.putInt(ARG_WEIGHT, order.order.weight);
//        args.putString(ARG_DATE, order.order.startedAt);
//        args.putString(ARG_PAYMENT, order.order.paymentMode);
//        args.putInt(ARG_TIME, order.timeToRespond);
        fragment.setArguments(args);
        return fragment;
    }

    protected boolean onTouchEvent (MotionEvent me) {
        return true;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            addressFrom = getArguments().getString(ARG_ADDRESS_FROM);
//            addressTo = getArguments().getString(ARG_ADDRESS_TO);
//            weight = getArguments().getInt(ARG_WEIGHT) + "";
//            date = getArguments().getString(ARG_DATE);
//            payment = getArguments().getString(ARG_PAYMENT);
//            time = getArguments().getInt(ARG_TIME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_otp, container, false);
        ButterKnife.bind(this,view);
//        fromAddressView.setText(addressFrom);
//        toAddressView.setText(addressTo);
//        weightView.setText(weight);
//        dateView.setText(date);
//        paymentView.setText(payment);
//        timer = createTimer(time).subscribe();
        otp1View.addTextChangedListener(new CustomTextWatcher(otp2View));
        otp2View.addTextChangedListener(new CustomTextWatcher(otp3View));
        otp3View.addTextChangedListener(new CustomTextWatcher(otp4View));
        otp4View.addTextChangedListener(new CustomTextWatcher(null));
//        otp4View.setImeOptions(EditorInfo.IME_ACTION_DONE);
        otp4View.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT && checkAllFields()) {
                Logger.d("YYEAH");
                return true;
            }
            return false;
        });

        view.setOnTouchListener((v, event) -> true);
        return view;
    }

    @OnClick({R.id.of_otp1,R.id.of_otp2,R.id.of_otp3,R.id.of_otp4})
    public void clickInput(EditText v){
        v.setText("");
    }

    private boolean checkAllFields(){
        return otp1View.getText().toString().length()==1 &&
               otp2View.getText().toString().length()==1 &&
               otp3View.getText().toString().length()==1 &&
               otp4View.getText().toString().length()==1;
    }

    private class CustomTextWatcher implements TextWatcher {
        private EditText focusNext;
        public CustomTextWatcher(EditText next) {
            focusNext = next;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!"".equals(s.toString())){
                if(focusNext!=null) focusNext.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


}
