package com.pnrhunter.mvp.documents;

import android.content.Context;

import com.orhanobut.logger.Logger;
import com.pnrhunter.R;
import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.mvp.main.UserModel;
import com.pnrhunter.utils.Finishable;
import com.pnrhunter.utils.MessageDisplayer;
import com.pnrhunter.utils.SpinnerShower;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class DocumentsPresenter {

    private Context context;
    private View view;
    private RouterModule.Router router;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private RetrofitModule.ServerAPI serverAPI;
    private UserModel userModel;

    private Map<String, RetrofitModule.ServerAPI.Document> documents;


    public DocumentsPresenter(Context context,
                              View view,
                              RouterModule.Router router,
                              DeviceInfoModule.DeviceInfo deviceInfo,
                              RetrofitModule.ServerAPI serverAPI,
                              UserModel userModel) {
        this.context = context;
        this.view = view;
        this.router = router;
        this.deviceInfo = deviceInfo;
        this.serverAPI = serverAPI;
        this.userModel = userModel;
    }

    public void requestDocumentsList() {

        serverAPI
                .getRequiredDocuments(
                        userModel.getAuthHeader(),
                        deviceInfo.deviceType,
                        deviceInfo.deviceId,
                        deviceInfo.deviceToken)
                .toObservable()
                .flatMap(response -> {
                    documents = generateDocumentsMap(response);
                    if (checkAllDocumentsUploaded()) {
                        router.goToWaitingForApprovalScreen();
                    } else {
                        view.displayList(documents);
                    }
                    return Observable.empty();
                })
                .doOnError(throwable -> view.showMessage(throwable.getMessage()))
                .subscribe();


    }


    private Map<String, RetrofitModule.ServerAPI.Document> generateDocumentsMap(List<RetrofitModule.ServerAPI.Document> response) {
        Map<String, RetrofitModule.ServerAPI.Document> result = new HashMap<>();

        for (RetrofitModule.ServerAPI.Document d : response) {
            result.put(d.id, d);
        }

        return result;
    }


    private boolean checkAllDocumentsUploaded() {
        for (String dId : documents.keySet()) {
            RetrofitModule.ServerAPI.Document d = documents.get(dId);
            if (d.remoteUrl == null) return false;
        }
        return true;
    }

    public void uploadDocuments() {
        List<RetrofitModule.ServerAPI.Document> toUpload = new ArrayList<>();
        for (String id : documents.keySet()) {
            RetrofitModule.ServerAPI.Document d = documents.get(id);
            if (d.localUrl != null) {
                toUpload.add(d);
            }
        }

        for (String id : documents.keySet()) {
            RetrofitModule.ServerAPI.Document d = documents.get(id);
            if (d.localUrl == null && d.remoteUrl==null ){
                view.showMessage(context.getString(R.string.error_documents_required));
                return;
            }
        }
        
        Observable
                .fromIterable(toUpload)
                .doOnSubscribe(disposable -> {
                    router.goToWaitingForApprovalScreen();
                    view.showSpinner();
                })
                .flatMap(document -> {
                    Logger.d("Documents -> generate sources");
                    return generateDocumentUploadingSource(document).toObservable();
                })
                .flatMap(uploadedDocument -> {
                    Logger.d("Documents -> form result");
                    documents.put(uploadedDocument.id, uploadedDocument);
                    return Observable.empty();
                })
                .doFinally(() -> {
                    Logger.d("Documents -> complete chain");
//                    view.hideSpinner();
//                    view.displayList(documents);
//                    if(checkAllDocumentsUploaded()){
//                        router.goToWaitingForApprovalScreen();
//                        view.finish();
//                    }else{
//                        view.showMessage(context.getString(R.string.error_documents_required));
//                    }
                })
                .doOnError(throwable -> {
                    view.showMessage(throwable.getMessage());
                })
                .subscribe();



    }

    private Single<RetrofitModule.ServerAPI.Document> generateDocumentUploadingSource(RetrofitModule.ServerAPI.Document document){

        RequestBody deviceType = RequestBody.create(MediaType.parse("text/plain"), deviceInfo.deviceType);
        RequestBody deviceId = RequestBody.create(MediaType.parse("text/plain"), deviceInfo.deviceId);
        RequestBody deviceToken = RequestBody.create(MediaType.parse("text/plain"), deviceInfo.deviceToken);

        File photo = new File(document.localUrl);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), photo);
//        CountingRequestBody countingBody = new CountingRequestBody(requestFile, (bytesWritten, contentLength) -> {
//            double progress = (1.0 * bytesWritten) / contentLength;
//            progressListener.onNext(progress);
//        });
        String filename = photo.getName();
        MultipartBody.Part fileData = MultipartBody.Part.createFormData("document", filename, requestFile);

        Single<RetrofitModule.ServerAPI.Document> sendingDocumentSource = serverAPI.sendDocument(
                userModel.getAuthHeader(),
                deviceType,
                deviceId,
                deviceToken,
                document.id,
                fileData
        );



        return sendingDocumentSource;
    }

    public void onDocumentChanged(RetrofitModule.ServerAPI.Document document) {
        documents.put(document.id,document);
    }


    public interface View extends MessageDisplayer, Finishable, SpinnerShower {
        void displayList(Map<String, RetrofitModule.ServerAPI.Document> documents);
        ObservableEmitter<Double> showProgress(RetrofitModule.ServerAPI.Document document);
        void hideProgress();
    }

    public static class CountingRequestBody extends RequestBody {

        protected RequestBody delegate;
        protected Listener listener;

        protected CountingSink countingSink;

        public CountingRequestBody(RequestBody delegate, Listener listener)
        {
            this.delegate = delegate;
            this.listener = listener;
        }

        @Override
        public MediaType contentType()
        {
            return delegate.contentType();
        }

        @Override
        public long contentLength()
        {
            try
            {
                return delegate.contentLength();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            return -1;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException
        {
            countingSink = new CountingSink(sink);
            BufferedSink bufferedSink = Okio.buffer(countingSink);

            delegate.writeTo(bufferedSink);

            bufferedSink.flush();
        }

        protected final class CountingSink extends ForwardingSink
        {

            private long bytesWritten = 0;

            public CountingSink(Sink delegate)
            {
                super(delegate);
            }

            @Override
            public void write(Buffer source, long byteCount) throws IOException
            {
                super.write(source, byteCount);

                bytesWritten += byteCount;
                listener.onRequestProgress(bytesWritten, contentLength());
            }

        }

        public static interface Listener
        {
            public void onRequestProgress(long bytesWritten, long contentLength);
        }
    }
}
