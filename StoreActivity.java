package weakling.segunda.mano;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.paypal.android.sdk.payments.PayPalService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weakling.segunda.mano.authentication.Authentication;
import weakling.segunda.mano.gallery.GalleryAdapter;
import weakling.segunda.mano.utils.mFx;

public class StoreActivity extends AppCompatActivity {
    private FirebaseUser user;
    private List<Map<String,Object>> data = new ArrayList<>();
    private Map<String,Object> imgData = new HashMap<>();
    private Dialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Rb.activity = this;
        Rb.instatiateProgressDialog();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Rb.progressDialog.show();
        // TODO: authenticate here

        if(!isAuthenticated()){
            // TODO: do some stuff here like login
            Rb.redirect = new Intent(Rb.activity,StoreActivity.class);
            Intent intent = new Intent(StoreActivity.this, Authentication.class);
            startActivity(intent);
            finish();
        }else{
            Rb.utilityPipeline.database.collection("store")
                    .document(Rb.utilityPipeline.authentication.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                String store_name = (String)task.getResult().get("name");
                                if(store_name!=null) {
                                    boolean isVerified = (boolean)task.getResult().get("verified");
                                    if(isVerified) {
                                        if(getIntent().getBooleanExtra("sell",false)){
                                            Intent intent = new Intent(StoreActivity.this,SellActivity.class);
                                            startActivity(intent);
                                        }
                                        String store_info = (String)task.getResult().get("info");
                                        setGrid(store_name, Rb.utilityPipeline.authentication.getCurrentUser().getDisplayName(),store_info);
                                    }else{
                                        dialog = new Dialog(StoreActivity.this,android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
                                        dialog.setContentView(R.layout.dialog_store_new3);
                                        ((TextView)dialog.findViewById(R.id.store_msg)).setText("Please wait for your account to be verified.\nSometimes it takes 3-10 working days.\nThank you for your patience.\nFrom Segunda Mano Team.");
                                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                finish();
                                            }
                                        });
                                        dialog.show();
                                    }
                                }else{
                                    dialog = new Dialog(StoreActivity.this,android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
                                    dialog.setContentView(R.layout.dialog_store_new1);
                                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            finish();
                                        }
                                    });
                                    dialog.show();
                                }
                            }
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(StoreActivity.this, PayPalService.class));
        super.onDestroy();
    }

    public void createStore(){
        Map<String,Object> store_details = new HashMap<>();
        store_details.put("name",store_name);
        store_details.put("info",store_info);
        store_details.put("address",store_address);
        store_details.put("number",store_number);
        store_details.put("verified",false);
        store_details.put("payment",payment_method);
        Rb.utilityPipeline.database.collection("store")
                .document(Rb.utilityPipeline.authentication.getCurrentUser().getUid())
                .set(store_details)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            dialog.dismiss();
                            dialog = new Dialog(StoreActivity.this,android.R.style.Theme_DeviceDefault_Light_Dialog);
                            dialog.setContentView(R.layout.dialog_store_new3);
                            ((TextView)dialog.findViewById(R.id.store_msg)).setText("Please wait for your account to be verified.\nSometimes it takes 3-10 working days.\nThank you for your patience.\nFrom Segunda Mano Team.");

                            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    finish();
                                }
                            });
                            dialog.show();
                        }else{
                            Toast.makeText(StoreActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void setUpPayment(View v){
        EditText paypal = dialog.findViewById(R.id.store_paypal);
        payment_method = new ArrayList<>();
        String paypal_account = paypal.getText().toString();
        if(paypal_account != null){
            payment_method.add("Paypal");
            payment_method.add(paypal_account);
            createStore();
            //Code below will literally use cash in our case for now we
            //dont use cash cause we must verify first the seller before
            //charging them. lol
            /*PayPalConfiguration config  = new PayPalConfiguration()
                    .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                    .clientId("AWsy53OMTdMIqfrGhcuqNarPUYDWMNKVRwCULPvlvdVm3Oi4ayAOFz4Q1jeqOltzaonbg5jnNTU6C6V9")
                    .defaultUserEmail(paypal_account);
            Intent intent = new Intent(StoreActivity.this, PayPalService.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
            startService(intent);
            //Payment
            PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(10.00)),"USD","Segunda Mano",PayPalPayment.PAYMENT_INTENT_SALE);
            Intent intent2 = new Intent(this, PaymentActivity.class);
            intent2.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            intent2.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
            startActivityForResult(intent2, 1234);*/
        }
    }

    //payment sumpay
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 1234 && resultCode == Activity.RESULT_OK){
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if(confirm != null){
                //Success
                createStore();
            }
        }
    }*/

    String store_name;
    String store_info;
    String store_address;
    String store_number;
    List<String> payment_method;

    public void setUpStore(View v){
        EditText store = dialog.findViewById(R.id.store_name);
        EditText info = dialog.findViewById(R.id.store_info);
        EditText address = dialog.findViewById(R.id.store_address);
        EditText number = dialog.findViewById(R.id.store_number);
        store_name = store.getText().toString();
        store_info = info.getText().toString();
        store_address = address.getText().toString();
        store_number = number.getText().toString();
        dialog.dismiss();
        dialog = new Dialog(StoreActivity.this,android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_store_new2);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
    }

    //GridView grid;
    private FlexboxLayoutManager flexboxLayoutManager;
    private RecyclerView recyclerView;

    public void setGrid(String storeName, String merchant, String info){
        ((TextView)findViewById(R.id.store_head_name)).setText(storeName);
        ((TextView)findViewById(R.id.store_head_merch)).setText(merchant);
        ((TextView)findViewById(R.id.collapsible_store_details)).setText(info);
        toggleStoreDetails(findViewById(R.id.store_more_details));

        //grid = findViewById(R.id.store_grid);
        flexboxLayoutManager = new FlexboxLayoutManager(Rb.activity);
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexboxLayoutManager.setAlignItems(AlignItems.STRETCH);
        recyclerView = Rb.activity.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(flexboxLayoutManager);

        FirebaseUser currentUser = Rb.utilityPipeline.authentication.getCurrentUser();
        Rb.utilityPipeline.database.collection("items")
                .whereEqualTo("uploaderId",currentUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        data.clear();
                        for(QueryDocumentSnapshot document : snapshots){
                            if(document.exists()){
                                Map<String, Object> doc = new HashMap<>();
                                doc.put("id", document.getId());
                                doc.putAll(document.getData());
                                getActualImage((String)document.get("prod_img"));
                                data.add(doc);
                            }
                        }
                    }
                });

        FloatingActionButton add = findViewById(R.id.store_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreActivity.this,SellActivity.class);
                startActivity(intent);
            }
        });
        Rb.progressDialog.dismiss();
    }

    private void getActualImage(final String image){
        StorageReference storageReference = Rb.utilityPipeline.storage.getReference().child("items/"+image+".png");
        try {
            final File localFile = File.createTempFile(image,"png");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            imgData.put(image, BitmapFactory.decodeFile(localFile.getPath()));
                            if(recyclerView!=null)
                                recyclerView.setAdapter(new GalleryAdapter(setupViews()));
                            //grid.setAdapter(new MyAdapter(StoreActivity.this));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Rb.activity,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(Rb.activity,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public boolean isAuthenticated(){
        user = Rb.utilityPipeline.authentication.getCurrentUser();
        return user!=null;
    }

    private LayoutInflater inflater;

    public List<View> setupViews(){
        List<View> view = new ArrayList<>();
        for(int i=0; i<data.size(); i++){
            ViewGroup.LayoutParams param1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ViewGroup.LayoutParams param2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            LinearLayout container = new LinearLayout(StoreActivity.this);
            container.setLayoutParams(param1);
            container.setOrientation(LinearLayout.VERTICAL);

            LinearLayout wrapper = new LinearLayout(StoreActivity.this);
            wrapper.setLayoutParams(param1);
            container.setOrientation(LinearLayout.HORIZONTAL);

            ImageView prev = new ImageView(StoreActivity.this);
            prev.setLayoutParams(param2);
            if(imgData.size()>=i){
                Bitmap bmp = (Bitmap)imgData.get(data.get(i).get("prod_img"));
                prev.setImageBitmap(bmp);
            }

            Time exp = new Time();
            exp.set((Long)data.get(i).get("expiration"));

            Time now = new Time();
            now.setToNow();

            String expiration = "";
            if(now.toMillis(false)>exp.toMillis(false)) {
                Long xxx = now.toMillis(false)-exp.toMillis(false);
                Time time = new Time();
                time.set(xxx);
                expiration = "Expires in "+time.second+"s";
            }else{
                expiration = "Expired";
            }

            TextView content = new TextView(StoreActivity.this);
            content.setLayoutParams(param2);
            String desc = data.get(i).get("prod_desc").toString();
            content.setText((String)data.get(i).get("prod_name")+"\n"+
                    (desc.length()>=20?desc.substring(0,20):desc)+"...\n"+
                    "Php "+data.get(i).get("prod_price")+"\n"+
                    expiration);

            Button mngBtn = new Button(StoreActivity.this);
            mngBtn.setLayoutParams(param1);
            mngBtn.setText("Manage");
            final int pos = i;
            mngBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StoreActivity.this,ManageActivity.class);
                    intent.putExtra("id",(String)data.get(pos).get("id"));
                    startActivity(intent);
                }
            });

            wrapper.addView(prev);
            wrapper.addView(content);

            container.addView(wrapper);
            container.addView(mngBtn);

            /*View tmpView;
            if(inflater==null) inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            tmpView = inflater.inflate(R.layout.rows_items_1,null,false);
            LinearLayout v = ((LinearLayout)tmpView.findViewById(R.id.rows_item_addon));
            final ImageView img = tmpView.findViewById(R.id._1img);
            TextView titl = tmpView.findViewById(R.id._1txt1);
            TextView desc = tmpView.findViewById(R.id._1txt2);
            if(imgData.size()>=i){
                Bitmap bmp = (Bitmap)imgData.get(data.get(i).get("prod_img"));
                img.setImageBitmap(bmp);
            }
            titl.setText((String)data.get(i).get("prod_name"));
            desc.setText("Php "+data.get(i).get("prod_price"));
            LinearLayout lo = (LinearLayout)inflater.inflate(R.layout.def_button,null);
            Button mng = ((Button)lo.findViewById(R.id.defBtnId));
            mng.setText("Manage");
            final int pos = i;
            mng.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StoreActivity.this,ManageActivity.class);
                    intent.putExtra("id",(String)data.get(pos).get("id"));
                    startActivity(intent);
                }
            });
            v.addView(lo);
            */
            view.add(container);
        }
        return view;
    }

    public void toggleStoreDetails(View v){
        View view = findViewById(R.id.collapsible_store_details);
        if(view.isShown()){
            mFx.slide_up(this, view);
            view.setVisibility(View.GONE);
            ((Button)v).setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
        } else{
            view.setVisibility(View.VISIBLE);
            mFx.slide_down(this, view);
            ((Button)v).setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_up_float, 0);
        }
    }

    public void toggleProducts(View v){
        View view = findViewById(R.id.recyclerview);
        if(view.isShown()){
            mFx.slide_up(this, view);
            view.setVisibility(View.GONE);
            ((Button)v).setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
        }else{
            view.setVisibility(View.VISIBLE);
            mFx.slide_down(this, view);
            ((Button)v).setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_up_float, 0);
        }
    }

}
