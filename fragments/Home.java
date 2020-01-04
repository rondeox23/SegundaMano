package weakling.segunda.mano.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
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

import weakling.segunda.mano.PreviewActivity;
import weakling.segunda.mano.R;
import weakling.segunda.mano.Rb;
import weakling.segunda.mano.gallery.GalleryAdapter;

public class Home extends Fragment {
    private LayoutInflater inflater;
    private List<Map<String,Object>> data = new ArrayList<>();
    private Map<String,Object> imgData = new HashMap<>();
    private int limit = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    private ProgressBar progress;
    //private GridView grid;
    private FlexboxLayoutManager flexboxLayoutManager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        progress = (ProgressBar) Rb.activity.findViewById(R.id.home_progress);
        //grid = Rb.activity.findViewById(R.id.grid_view_home);
        flexboxLayoutManager = new FlexboxLayoutManager(Rb.activity);
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexboxLayoutManager.setAlignItems(AlignItems.STRETCH);
        recyclerView = Rb.activity.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(flexboxLayoutManager);
        data.clear();
        startFetching();
    }

    public void update(){
        data.clear();
        startFetching();
    }

    private int visibleThreshold = 2;
    private int currentPage = 0;
    private int previousTotal = 0;
    private boolean loading = true;

    private void startFetching(){
        if(progress!=null)
            progress.setVisibility(View.VISIBLE);
        Time time = new Time();
        time.setToNow();
        Rb.utilityPipeline.database.collection("items")
                .whereArrayContains("tags",Rb.searchQuery.getQuery())
                .whereEqualTo("verified",true)
                //.whereLessThan("expiration",time.toMillis(false))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        data.clear();
                        for(QueryDocumentSnapshot document : snapshots){
                            if(document.exists()){
                                Map<String,Object> doc = new HashMap<>();
                                doc.put("id",document.getId());
                                doc.putAll(document.getData());
                                getActualImage((String)document.get("prod_img"));
                                data.add(doc);
                            }
                        }
                    }
                });
    }

    private RecyclerView recyclerView;

    private void setGridView(){
        progress.setVisibility(View.GONE);
        if(recyclerView!=null)
            recyclerView.setAdapter(new GalleryAdapter(setupViews()));
    }

    public List<View> setupViews(){
        List<View> view = new ArrayList<>();

        ViewGroup.LayoutParams param1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout container = new LinearLayout(Rb.activity);
        container.setLayoutParams(param1);
        container.setOrientation(LinearLayout.HORIZONTAL);

        param1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textView = new TextView(Rb.activity);
        textView.setLayoutParams(param1);
        textView.setText("Showing Results for 'All'");

        container.addView(textView);
        view.add(container);

        for(int i=0; i<data.size(); i++){
            View tmpView;
            if(inflater==null) inflater = (LayoutInflater) Rb.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    private void getActualImage(final String image){
        StorageReference storageReference = Rb.utilityPipeline.storage.getReference().child("items/"+image+".png");
        try {
            final File localFile = File.createTempFile(image,"png");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            imgData.put(image,BitmapFactory.decodeFile(localFile.getPath()));
                            setGridView();
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