import android.content.Context;

import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.runner.AndroidJUnitRunner;

import com.holler.app.AndarApplication;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.DaggerAppComponent;
import com.holler.app.di.app.modules.AppModule;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.OrderModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.SharedPreferencesModule;
import com.holler.app.di.app.modules.UserModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.main.UserModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;

@SmallTest
public class CreateOrderFromDriverTest {

    private static final String userEmail = "a@a.com";
    private static final String userPassword = "1aaaaaaa";
    private Context context;
    private AppComponent component;


    @Before
    public void createLogHistory() {

    }

    @Test
    public void logHistory_ParcelableWriteRead() {

    }

}
