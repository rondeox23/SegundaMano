package weakling.segunda.mano;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import java.util.Map;
import weakling.segunda.mano.fragments.MyFragment;
import weakling.segunda.mano.utils.RbQuery;
import weakling.segunda.mano.utils.UtilityPipeline;

public class Rb {

    public static MyFragment fragmentAdapter;

    public static BottomNavigationView navigation;

    public static Activity activity;

    public static UtilityPipeline utilityPipeline;

    public static Map<String,Object> tmpData;

    public static Intent redirect;

    public static Dialog progressDialog;

    public static void instatiateProgressDialog(){
        progressDialog = new Dialog(activity,android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        progressDialog.setContentView(R.layout.template_progress);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                activity.finish();
            }
        });
    }

    public static RbQuery searchQuery = new RbQuery().setQuery("all");

    public static final int DELIVERY_SYSTEM = 7582;

    public static final int STOCK_SYSTEM = 3740;

}
