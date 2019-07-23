package com.pnrhunter.mvp.documents;

import android.content.Context;

import com.pnrhunter.R;
import com.pnrhunter.mvp.authorization.AuthenticationInterface;
import com.pnrhunter.mvp.utils.DeviceInfo;
import com.pnrhunter.mvp.utils.activity.Finishable;
import com.pnrhunter.mvp.utils.activity.MessageDisplayer;
import com.pnrhunter.mvp.utils.activity.SpinnerShower;
import com.pnrhunter.mvp.utils.router.AbstractRouter;
import com.pnrhunter.mvp.utils.server.objects.Document;
import com.pnrhunter.mvp.utils.router.DriverRouter;
import com.pnrhunter.mvp.utils.server.ServerAPI_Documents;

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
    private AbstractRouter router;
    private DeviceInfo deviceInfo;
    private ServerAPI_Documents serverAPI;
    private AuthenticationInterface auth;

    private Map<String, Document> documents;


    public DocumentsPresenter(Context context,
                              View view,
                              AbstractRouter router,
                              DeviceInfo deviceInfo,
                              ServerAPI_Documents serverAPI,
                              AuthenticationInterface auth) {
        this.context = context;
        this.view = view;
        this.router = router;
        this.deviceInfo = deviceInfo;
        this.serverAPI = serverAPI;
        this.auth = auth;
    }

    public void requestDocumentsList() {

        serverAPI
                .getRequiredDocuments(
                        auth.getAuthHeader(),
                        deviceInfo.getType(),
                        deviceInfo.getId(),
                        deviceInfo.getToken())
                .toObservable()
                .flatMap(response -> {
                    documents = generateDocumentsMap(response);
                    if (checkAllDocumentsUploaded()) {
                        router.goTo(DriverRouter.ROUTE_WAITING_FOR_APPROVAL);
                    } else {
                        view.displayList(documents);
                    }
                    return Observable.empty();
                })
                .doOnError(throwable -> view.showMessage(throwable.getMessage()))
                .subscribe();


    }


    private Map<String, Document> generateDocumentsMap(List<Document> response) {
        Map<String, Document> result = new HashMap<>();

        for (Document d : response) {
            result.put(d.id, d);
        }

        return result;
    }


    private boolean checkAllDocumentsUploaded() {
        for (String dId : documents.keySet()) {
            Document d = documents.get(dId);
            if (d.remoteUrl == null) return false;
        }
        return true;
    }

    public void uploadDocuments() {
        List<Document> toUpload = new ArrayList<>();
        for (String id : documents.keySet()) {
            Document d = documents.get(id);
            if (d.localUrl != null) {
                toUpload.add(d);
            }
        }
        
        Observable
                .fromIterable(toUpload)
                .doOnSubscribe(disposable -> {
                    view.showSpinner();
                })
                .flatMap(document -> {
                    return generateDocumentUploadingSource(document).toObservable();
                })
                .flatMap(uploadedDocument -> {
                    documents.put(uploadedDocument.id, uploadedDocument);
                    return Observable.empty();
                })
                .doFinally(() -> {
                    view.hideSpinner();
                    view.displayList(documents);
                    if(checkAllDocumentsUploaded()){
                        router.goTo(DriverRouter.ROUTE_WAITING_FOR_APPROVAL);
                        view.finish();
                    }else{
                        view.showMessage(context.getString(R.string.error_documents_required));
                    }
                })
                .doOnError(throwable -> {
                    view.showMessage(throwable.getMessage());
                })
                .subscribe();



    }

    private Single<Document> generateDocumentUploadingSource(Document document){

        RequestBody deviceType = RequestBody.create(MediaType.parse("text/plain"), deviceInfo.getType());
        RequestBody deviceId = RequestBody.create(MediaType.parse("text/plain"), deviceInfo.getId());
        RequestBody deviceToken = RequestBody.create(MediaType.parse("text/plain"), deviceInfo.getToken());

        File photo = new File(document.localUrl);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), photo);
//        CountingRequestBody countingBody = new CountingRequestBody(requestFile, (bytesWritten, contentLength) -> {
//            double progress = (1.0 * bytesWritten) / contentLength;
//            progressListener.onNext(progress);
//        });
        String filename = photo.getName();
        MultipartBody.Part fileData = MultipartBody.Part.createFormData("document", filename, requestFile);

        Single<Document> sendingDocumentSource = serverAPI.sendDocument(
                auth.getAuthHeader(),
                deviceType,
                deviceId,
                deviceToken,
                document.id,
                fileData
        );



        return sendingDocumentSource;
    }

    public void onDocumentChanged(Document document) {
        documents.put(document.id,document);
    }


    public interface View extends MessageDisplayer, Finishable, SpinnerShower {
        void displayList(Map<String, Document> documents);
        ObservableEmitter<Double> showProgress(Document document);
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
