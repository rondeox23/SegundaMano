package weakling.segunda.mano;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import weakling.segunda.mano.authentication.Authentication;
import weakling.segunda.mano.utils.CustomSnackbar;
import weakling.segunda.mano.utils.MyBitmapFactory;

public class PreviewActivity extends AppCompatActivity {
    private Dialog dialogComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Rb.activity = this;
        Rb.instatiateProgressDialog();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateUI(true);
        setup();
        //String message = getIntent().getStringExtra("message");
        //if(message != null){
        //    postComment(message);
        //}
    }

    private void setup(){
        ImageView img = findViewById(R.id.prev_img);
        TextView title = findViewById(R.id.prev_titl);
        TextView price = findViewById(R.id.prev_price);
        TextView desc = findViewById(R.id.prev_desc);
        TextView tags = findViewById(R.id.prev_tags);
        Bitmap bmp = (Bitmap)Rb.tmpData.get("prod_image");
        img.setImageBitmap(bmp);
        title.setText((String)Rb.tmpData.get("prod_name"));
        price.setText("Php "+Rb.tmpData.get("prod_price"));
        desc.setText((String)Rb.tmpData.get("prod_desc"));
        for(String str : (List<String>)Rb.tmpData.get("tags")){
            tags.append("#"+str+" ");
        }
        //Uploader info
        final Map up_info = (Map)Rb.tmpData.get("uploader");
        final ImageView up_img = findViewById(R.id.prev_up_img);
        final TextView up_name = findViewById(R.id.prev_up_name);
        try{
            new MyBitmapFactory.GetImageFromUrl(new MyBitmapFactory.GetImageFromUrl.SuccessListener() {
                @Override
                public void onPostExecute(Bitmap result) {
                    float dp24px = MyBitmapFactory.convertDpToPx(PreviewActivity.this,24);
                    up_img.setImageBitmap(Bitmap.createScaledBitmap(result,(int)dp24px,(int)dp24px,true));
                    up_name.setText((String)up_info.get("name"));
                    updateUI(false);
                }
            })
                    .execute((String)up_info.get("photo"));
        } catch (Exception e){
            Snackbar.make(findViewById(R.id.preview_id),"Unable to Retrieve Image",Snackbar.LENGTH_LONG).show();
        }

        ((Button)findViewById(R.id.prev_visit_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PreviewActivity.this,StoreActivity_.class);
                intent.putExtra("merchant",(String)Rb.tmpData.get("uploaderId"));
                startActivity(intent);
            }
        });
        displayReviews();
    }

    private boolean yescando = true;
    private void displayReviews(){
        Rb.utilityPipeline.database.collection("comments")
                .whereEqualTo("item_id",Rb.tmpData.get("id"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Map<String,Object>> tmp = new ArrayList<>();
                        int m =0;
                        for(QueryDocumentSnapshot doc : snapshots){
                            allCommentos.add(doc.getData());
                            if(doc.get("user_id").toString().equals(Rb.utilityPipeline.authentication.getCurrentUser().getUid())){
                                yescando = false;
                                commentos.add(doc.getData());
                            }else{
                                if(m<6){
                                    tmp.add(doc.getData());
                                }
                            }
                            m++;
                            ratingf+=Float.parseFloat(doc.get("rate").toString());
                        }
                        if(yescando){
                            findViewById(R.id.comment_btn).setVisibility(View.VISIBLE);
                        }
                        ratingf = ratingf/allCommentos.size();
                        RatingBar allRating = findViewById(R.id.allRatings);
                        TextView allRatingTxt = findViewById(R.id.allRatingTxt);
                        allRating.setRating(ratingf);
                        allRatingTxt.setText(ratingf+"");
                        commentos.addAll(tmp);
                        LinearLayout list = findViewById(R.id.prev_comments);
                        ((Button)findViewById(R.id.prev_comment_header)).setText("Reviews ("+allCommentos.size()+")");
                        MyAdapter adapter = new MyAdapter(PreviewActivity.this,R.layout.frag_review,commentos);
                        if(adapter.getCount()<1){
                            TextView no_item = new TextView(PreviewActivity.this);
                            no_item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            no_item.setGravity(Gravity.CENTER);
                            no_item.setText("Be the first one to give a feedback!");
                            list.addView(no_item);
                            allRating.setRating(0);
                            allRatingTxt.setText("0.0");
                        }else {
                            for (int i = 0; i < adapter.getCount(); i++) {
                                View item = adapter.getView(i, null, list);
                                list.addView(item);
                            }
                        }
                        if(adapter.getCount()>7){
                            findViewById(R.id.all_comment_btn).setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private List<Map<String,Object>> commentos = new ArrayList<>();
    private List<Map<String,Object>> allCommentos = new ArrayList<>();
    private float ratingf = 0;

    private void updateUI(boolean show){
        final int[] okay = {0,0,0};

        final LinearLayout content = findViewById(R.id.prev_content);
        content.setVisibility(View.VISIBLE);
        Rb.progressDialog.dismiss();
        dialogComment = new Dialog(PreviewActivity.this,android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        dialogComment.setContentView(R.layout.snack_comment);
        //comment = CustomSnackbar.make(findViewById(R.id.preview_id),R.layout.snack_comment,CustomSnackbar.LENGTH_INDEFINITE);
        //comment.getView().setBackgroundColor(Color.parseColor("#FFFFFF"));

        final RatingBar ratingBar = (RatingBar) dialogComment.findViewById(R.id.rating);
        final EditText subText = (EditText) dialogComment.findViewById(R.id.subject);
        final EditText feedText = (EditText) dialogComment.findViewById(R.id.feedback);
        final Button pBtn = (Button) dialogComment.findViewById(R.id.submit_btn);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(rating>0)
                    okay[0] = 1;
                if(okay[0]==1 && okay[1]==1 && okay[2]==1)
                    pBtn.setEnabled(true);
            }
        });
        subText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count>0)
                    okay[1] = 1;
                if(okay[0]==1 && okay[1]==1 && okay[2]==1)
                    pBtn.setEnabled(true);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        feedText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count>0)
                    okay[2] = 1;
                if(okay[0]==1 && okay[1]==1 && okay[2]==1)
                    pBtn.setEnabled(true);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        (dialogComment.findViewById(R.id.submit_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Rb.utilityPipeline.authentication.getCurrentUser()==null){
                    //Rb.redirect = new Intent(Rb.activity,PreviewActivity.class);
                    //Rb.redirect.putExtra("message",((EditText)dialogComment.findViewById(R.id.feedback)).getText().toString());
                    //Intent intent = new Intent(PreviewActivity.this, Authentication.class);
                    //startActivity(intent);
                    //finish();
                }else{
                    dialogComment.dismiss();
                    postComment(ratingBar.getRating(),subText.getText().toString(),feedText.getText().toString());
                }
            }
        });

        int counter = 0;
        if(show){
            try{
                counter = Integer.parseInt(Rb.tmpData.get("popularity").toString());
            }catch(Exception e){
                Snackbar.make(findViewById(R.id.preview_id),e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
            counter++;
            Rb.utilityPipeline.database.collection("items").document((String)Rb.tmpData.get("id")).update("popularity",counter);
            content.setVisibility(View.GONE);
            Rb.progressDialog.show();
        }
    }

    int i = 1;
    public void addToCart(View v){
        final int avail = Integer.parseInt(Rb.tmpData.get("prod_avail").toString());
        final CustomSnackbar add_cart = CustomSnackbar.make(findViewById(R.id.preview_id),R.layout.snack_cart_add,CustomSnackbar.LENGTH_INDEFINITE);
        add_cart.getView().setBackgroundColor(Color.parseColor("#FFFFFF"));
        final EditText count = (EditText)add_cart.findViewById(R.id.snackbar_cart_count);
        add_cart.findViewById(R.id.snackbar_cart_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(i+1<=avail){
                    i++;
                    count.setText(""+i);
                }
            }
        });
        add_cart.findViewById(R.id.snackbar_cart_min).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(i-1>=1){
                    i--;
                    count.setText(""+i);
                }
            }
        });
        add_cart.findViewById(R.id.submit_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> cart_details = new HashMap<>();
                cart_details.put("item_id",Rb.tmpData.get("id"));
                cart_details.put("user_id",Rb.utilityPipeline.authentication.getCurrentUser().getUid());
                cart_details.put("merchant_id",Rb.tmpData.get("uploaderId"));
                cart_details.put("quantity",Integer.parseInt(((EditText)add_cart.findViewById(R.id.snackbar_cart_count)).getText().toString()));
                Rb.utilityPipeline.database.collection("cart")
                        .add(cart_details)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference ref) {
                                add_cart.dismiss();
                                Snackbar.make(findViewById(R.id.preview_id),"Successfully Added to Cart!",Snackbar.LENGTH_LONG)
                                        .setAction("Goto Cart", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(PreviewActivity.this,CartActivity.class);
                                                startActivity(intent);
                                            }
                                        }).show();
                            }
                        });
            }
        });
        add_cart.show();
    }

    public void postComment(float rate, String subject, String feedback){
        Rb.progressDialog.show();
        final Map<String,Object> data = new HashMap<>();
        data.put("item_id",Rb.tmpData.get("id"));
        data.put("user_id",Rb.utilityPipeline.authentication.getCurrentUser().getUid());
        data.put("name",Rb.utilityPipeline.authentication.getCurrentUser().getDisplayName());
        data.put("rate",rate);
        data.put("subject",subject);
        data.put("feedback",feedback);
        Time time = new Time();
        time.setToNow();
        data.put("time",time.toMillis(false));
        //data.put("image",Rb.utilityPipeline.authentication.getCurrentUser().getPhotoUrl());
        Rb.utilityPipeline.database.collection("comments").add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Rb.progressDialog.dismiss();
                        if(dialogComment.isShowing())
                            dialogComment.dismiss();
                        MyAdapter adapter = new MyAdapter(PreviewActivity.this,R.layout.frag_review,commentos);
                        LinearLayout list = findViewById(R.id.prev_comments);
                        if(adapter.getCount()<1){
                            list.removeAllViews();
                        }
                        commentos.add(0,data);
                        for (int i = 0; i < adapter.getCount(); i++) {
                            View item = adapter.getView(i, null, list);
                            list.addView(item);
                        }
                        if(adapter.getCount()>7){
                        findViewById(R.id.all_comment_btn).setVisibility(View.VISIBLE);
                    }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Rb.progressDialog.dismiss();
                    }
                });
    }

    public void addComment(View v){
        if(Rb.utilityPipeline.authentication.getCurrentUser()==null) {
            // TODO: do some stuff here like login
            Rb.redirect = new Intent(Rb.activity,PreviewActivity.class);
            Intent intent = new Intent(PreviewActivity.this, Authentication.class);
            startActivity(intent);
            finish();
        }else
            dialogComment.show();
    }

    //CustomSnackbar comment;

    @Override
    public void onBackPressed() {
        if(dialogComment.isShowing() && dialogComment!=null)
            dialogComment.dismiss();
        else
            super.onBackPressed();
    }

    private LayoutInflater inflater = null;

    private class MyAdapter extends BaseAdapter {
        private Context context;
        private int layout;
        private List<Map<String,Object>> resources;

        /**
         * The third arguments must contain 3 data:
         * <br>first - image resource (int)
         * <br>second - (string)
         * <br>third - (string)
         * <br>
         * <br>if third arguments length < 3 this may throw
         * <br>an exception and causes error
         * <br><b>Rondeo</b>
         * @author Rondeo Balos
         */
        public MyAdapter(Context context, int layout, List<Map<String,Object>> resources) {
            this.context = context;
            this.layout = layout;
            this.resources = resources;
        }

        @Override
        public int getCount() {
            return resources.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        //In Preview Activity with the comments we will use rows_items_3.xml
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(inflater==null) inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,parent,false);
            convertView.setPadding(10,10,10,10);
            ImageView img = convertView.findViewById(R.id._3img);
            TextView text1 = convertView.findViewById(R.id._3txt1);
            TextView text2 = convertView.findViewById(R.id._3txt2);
            //img.setImageResource((Integer)resources.get(position).get(0));
            Time time = new Time();
            time.set((Long)resources.get(position).get("time"));
            text1.setText((String)resources.get(position).get("name")+" - "+time.format("%Y/%M%d %H:%M%A"));
            text2.setText((String)resources.get(position).get("subject")+"\n"+(String)resources.get(position).get("feedback"));

            RatingBar ratingBar = convertView.findViewById(R.id.header);
            ratingBar.setRating(Float.valueOf(resources.get(position).get("rate").toString()));
            return convertView;
        }

    }

}
