package weakling.segunda.mano;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {
    Snackbar snack;
    List<CheckBox> checkers = new ArrayList<>();
    List<String> checked = new ArrayList<>();
    List<String> merch = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        if(inflater==null) inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        snack = Snackbar.make(findViewById(R.id.cart_id),"items selected",Snackbar.LENGTH_INDEFINITE)
                .setAction("Buy out", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(int i=0; i<merch.size(); i++){
                            Map<String,Object> notif = new HashMap<>();
                            notif.put("to",merch.get(i));
                            notif.put("code",Rb.DELIVERY_SYSTEM);
                            notif.put("title","Product Purchase");
                            notif.put("message","Your product has been bought by...");
                            Map<String,Object> extra = new HashMap<>();
                            extra.put("user_id",Rb.utilityPipeline.authentication.getCurrentUser().getUid());
                            extra.put("item_id",checked.get(i));
                            Rb.utilityPipeline.database.collection("notifications")
                                    .add(notif)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {

                                        }
                                    });
                        }
                    }
                });
        Rb.utilityPipeline.database.collection("cart")
                .whereEqualTo("user_id",Rb.utilityPipeline.authentication.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(final DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            final View view = inflater.inflate(R.layout.rows_items_5,(ViewGroup)findViewById(R.id.cart_id),false);
                            ((LinearLayout)findViewById(R.id.cart_container)).addView(view);
                            Rb.utilityPipeline.database.collection("items")
                                    .document((String)doc.get("item_id"))
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                            //Toast.makeText(CartActivity.this,(String)documentSnapshot.get("prod_img"),Toast.LENGTH_LONG);
                                            getActualImage((String)documentSnapshot.get("prod_img"),(ImageView)view.findViewById(R.id._5img));
                                            ((TextView)view.findViewById(R.id._5txt1)).setText((String)documentSnapshot.get("prod_name"));
                                            int quantity = Integer.parseInt(doc.get("quantity").toString());
                                            int price = Integer.parseInt(documentSnapshot.get("prod_price").toString());
                                            ((TextView)view.findViewById(R.id._5txt2)).setText("Php "+price+" x "+quantity);
                                            ((TextView)view.findViewById(R.id._5txt3)).setText("Php "+quantity*price);
                                            final CheckBox check = ((CheckBox)view.findViewById(R.id._5check));
                                            checkers.add(check);
                                            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    if(isChecked){
                                                        snack.show();
                                                    }
                                                    boolean allUnchecked = true;
                                                    int count = 0;
                                                    for(CheckBox chk : checkers){
                                                        if(chk.isChecked()){
                                                            count++;
                                                        }
                                                    }
                                                    for(CheckBox chk : checkers){
                                                        if(chk.isChecked()){
                                                            allUnchecked = false;
                                                            break;
                                                        }
                                                    }
                                                    if(allUnchecked) snack.dismiss();
                                                    snack.setText(count+" item(s) selected.");
                                                }
                                            });
                                            view.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if(check.isChecked())
                                                        check.setChecked(false);
                                                    else
                                                        check.setChecked(true);
                                                }
                                            });
                                            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    if(isChecked) {
                                                        checked.add(documentSnapshot.getId());
                                                        merch.add(documentSnapshot.get("uploaderId").toString());
                                                    }else {
                                                        checked.remove(documentSnapshot.getId());
                                                        merch.remove(documentSnapshot.get("uploaderId").toString());
                                                    }
                                                }
                                            });
                                        }
                                    });
                        }
                    }
                });
    }

    LayoutInflater inflater;

    private void getActualImage(final String image, final ImageView imgView){
        StorageReference storageReference = Rb.utilityPipeline.storage.getReference().child("items/"+image+".png");
        try {
            final File localFile = File.createTempFile(image,"png");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            imgView.setImageBitmap(BitmapFactory.decodeFile(localFile.getPath()));
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
}
