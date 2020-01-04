package weakling.segunda.mano;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weakling.segunda.mano.gallery.GalleryAdapter;
import weakling.segunda.mano.utils.mFx;

public class StoreActivity_ extends AppCompatActivity {
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

        final String id = getIntent().getStringExtra("merchant");
        if(id==null) finish();

        Rb.utilityPipeline.database.collection("store")
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            String store_name = (String)task.getResult().get("name");
                            String store_info = (String)task.getResult().get("info");
                            setGrid(store_name, Rb.utilityPipeline.authentication.getCurrentUser().getDisplayName(),id,store_info);
                        }
                    }
                });
    }

    //GridView grid;
    private FlexboxLayoutManager flexboxLayoutManager;
    private RecyclerView recyclerView;

    public void setGrid(String storeName, String merchant, String id, String info){
        ((TextView)findViewById(R.id.store_head_name)).setText(storeName);
        ((TextView)findViewById(R.id.store_head_merch)).setText(merchant);
        ((TextView)findViewById(R.id.collapsible_store_details)).setText(info);
        toggleStoreDetails(findViewById(R.id.store_more_details));

        //grid = findViewById(R.id.store_grid);
        //grid = findViewById(R.id.store_grid);
        flexboxLayoutManager = new FlexboxLayoutManager(Rb.activity);
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexboxLayoutManager.setAlignItems(AlignItems.STRETCH);
        recyclerView = Rb.activity.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(flexboxLayoutManager);

        Rb.utilityPipeline.database.collection("items")
                .whereEqualTo("uploaderId",id)
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
                /*.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            Map<String, Object> doc = new HashMap<>();
                            doc.put("id",document.getId());
                            doc.put("popularity",document.get("popularity"));
                            doc.put("prod_name",document.get("prod_name"));
                            doc.put("prod_price",document.get("prod_price"));
                            doc.put("prod_desc",document.get("prod_desc"));
                            doc.put("prod_img",document.get("prod_img"));
                            doc.put("uploader",document.get("uploader"));
                            doc.put("tags",document.get("tags"));
                            doc.put("prod_avail",document.get("prod_avail"));
                            doc.put("uploaderId",document.get("uploaderId"));
                            getActualImage((String)document.get("prod_img"));
                            data.add(doc);
                        }
                    }
                });*/

        FloatingActionButton add = findViewById(R.id.store_add);
        add.hide();
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
                            //grid.setAdapter(new MyAdapter(StoreActivity_.this));
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

    private LayoutInflater inflater;

    public List<View> setupViews(){
        List<View> view = new ArrayList<>();
        for(int i=0; i<data.size(); i++){
            View tmpView;
            if(inflater==null) inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            tmpView = inflater.inflate(R.layout.rows_items_1,null,false);
            final ImageView img = tmpView.findViewById(R.id._1img);
            TextView titl = tmpView.findViewById(R.id._1txt1);
            TextView desc = tmpView.findViewById(R.id._1txt2);
            if(imgData.size()>=i){
                Bitmap bmp = (Bitmap)imgData.get(data.get(i).get("prod_img"));
                img.setImageBitmap(bmp);
            }
            titl.setText((String)data.get(i).get("prod_name"));
            desc.setText("Php "+data.get(i).get("prod_price"));
            final int pos = i;
            tmpView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Rb.tmpData = data.get(pos);
                    Rb.tmpData.put("prod_image",imgData.get(data.get(pos).get("prod_img")));
                    Intent intent = new Intent(Rb.activity, PreviewActivity.class);
                    startActivity(intent);
                }
            });
            view.add(tmpView);
        }
        return view;
    }
/*
    private class MyAdapter extends BaseAdapter {
        private Context context;

        public MyAdapter(Context context){
            this.context = context;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(inflater==null) inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.rows_items_1,parent,false);
            LinearLayout view = ((LinearLayout)convertView.findViewById(R.id.rows_item_id));
            view.setOrientation(LinearLayout.VERTICAL);
            final ImageView img = convertView.findViewById(R.id._1img);
            TextView titl = convertView.findViewById(R.id._1txt1);
            TextView desc = convertView.findViewById(R.id._1txt2);
            if(imgData.size()>position){
                Bitmap bmp = (Bitmap)imgData.get(data.get(position).get("prod_img"));
                img.setImageBitmap(bmp);
            }
            titl.setText((String)data.get(position).get("prod_name"));
            desc.setText("Php "+data.get(position).get("prod_price"));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Rb.tmpData = data.get(position);
                    Rb.tmpData.put("prod_image",imgData.get(data.get(position).get("prod_img")));
                    Intent intent = new Intent(Rb.activity, PreviewActivity.class);
                    startActivity(intent);
                }
            });
            return convertView;
        }
    }
    */

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
