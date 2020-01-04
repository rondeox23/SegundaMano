package weakling.segunda.mano;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weakling.segunda.mano.utils.MyBitmapFactory;

public class ManageActivity extends AppCompatActivity {
    private EditText nameView;
    private EditText priceView;
    private EditText descView;
    private TextView tagsView;
    private EditText availView;
    private Spinner catView;
    private ImageView imgView;
    private Uri imageUri;
    private boolean isImageChosen = false;
    private String id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);
        Rb.activity = this;
        Rb.instatiateProgressDialog();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        id = getIntent().getStringExtra("id");
        if(id == null) finish();

        Rb.utilityPipeline.database.collection("items")
                .document(id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        nameView = findViewById(R.id.sell_item_name);
                        nameView.setText((String)documentSnapshot.get("prod_name"));
                        priceView = findViewById(R.id.sell_item_price);
                        priceView.setText(documentSnapshot.get("prod_price").toString());
                        catView = findViewById(R.id.sell_item_cat);
                        String[] lists = { "Entertainment", "Sports", "Outdoor", "Appliances", "Gadgets" };
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ManageActivity.this, android.R.layout.simple_spinner_item, lists);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        catView.setAdapter(adapter);
                        catView.setSelection(Integer.parseInt(documentSnapshot.get("prod_category").toString()));
                        descView = findViewById(R.id.sell_item_desc);
                        descView.setText((String)documentSnapshot.get("prod_desc"));
                        imgView = (ImageView) findViewById(R.id.sell_item_img);
                        tagsView = findViewById(R.id.sell_item_tags);
                        availView = findViewById(R.id.sell_item_avail);
                        availView.setText(documentSnapshot.get("prod_avail").toString());

                        nameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if(nameView.getText().toString().length()>0){
                                    List<String> tags = generateRelationalTags(nameView.getText().toString());
                                    for(String str : tags){
                                        tagsView.append(" #"+str);
                                    }
                                }
                            }
                        });
                        final Button btn_cancel = findViewById(R.id.cancel_item_btn);
                        btn_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(ManageActivity.this).setTitle("Are you sure?")
                                        .setMessage("Do you really want to delete this product forever?\nThis cannot be undone.")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Rb.utilityPipeline.database.collection("items")
                                                        .document(id)
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                finish();
                                                            }
                                                        });
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).create().show();
                            }
                        });
                        final Button btn_sell = (Button) findViewById(R.id.sell_item_btn);
                        btn_sell.setText("Update");
                        btn_sell.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Setting Loading Bar
                                Rb.progressDialog.show();
                                if(isContentsOk() && isImageChosen){
                                    //Putting some data into the item
                                    Map<String,Object> item = new HashMap<>();
                                    item.put("prod_name",nameView.getText().toString());
                                    item.put("prod_price",priceView.getText().toString());
                                    item.put("prod_desc",descView.getText().toString());
                                    item.put("prod_category",catView.getSelectedItemPosition());
                                    item.put("prod_avail",Integer.valueOf(availView.getText().toString()));
                                    item.put("verified",false);
                                    //Tags
                                    List<String> tags = generateRelationalTags(nameView.getText().toString());
                                    tags.add("all");
                                    item.put("tags",tags);
                                    //User Info
                                    FirebaseUser currentUser = Rb.utilityPipeline.authentication.getCurrentUser();
                                    Map<String,String> uploader = new HashMap<>();
                                    uploader.put("name",currentUser.getDisplayName());
                                    uploader.put("email",currentUser.getEmail());
                                    uploader.put("mobile",currentUser.getPhoneNumber());
                                    uploader.put("photo",currentUser.getPhotoUrl().toString());
                                    item.put("uploader",uploader);
                                    item.put("uploaderId",currentUser.getUid());
                                    //Uploading Item and Image
                                    try{
                                        uploadImage(imageUri,item);
                                    }catch (Exception e){
                                        Toast.makeText(ManageActivity.this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                }
                                Rb.progressDialog.dismiss();
                            }
                        });
                    }
                });
    }

    public boolean isContentsOk(){
        if(TextUtils.isEmpty(nameView.getText())){
            nameView.setError("Product name must not be empty");
            return false;
        }
        if(TextUtils.isEmpty(priceView.getText())){
            priceView.setText("0");
        }
        if(TextUtils.isEmpty(descView.getText())){
            descView.setError("Description must not be empty");
            return false;
        }
        return true;
    }

    private final int READ_REQUEST_CODE = 42;

    public void performFileSearch(View view){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,READ_REQUEST_CODE);
    }

    public void uploadImage(Uri uri, final Map<String,Object> item) throws Exception{
        Time time = new Time();
        time.setToNow();
        final String name = "item_no-"+time.toMillis(false);
        Bitmap tmpImage = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
        tmpImage = Bitmap.createScaledBitmap(tmpImage,250,250,true);
        StorageReference reference = Rb.utilityPipeline.storage.getReference().child("items/"+name+".png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tmpImage.compress(Bitmap.CompressFormat.PNG,0,baos);
        byte[] imgData = baos.toByteArray();
        reference.putBytes(imgData)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        item.put("prod_img",name);
                        publishItem(item);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Rb.utilityPipeline.gAuth.setOnActivityForResult(requestCode,resultCode,data);
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            if(data != null){
                imageUri = data.getData();
                try {
                    imgView.setImageBitmap(MyBitmapFactory.getImagefromUri(this,imageUri));
                    isImageChosen = true;
                } catch (IOException e){
                    Toast.makeText(this,"Unable to upload Image.",Toast.LENGTH_LONG).show();
                    Rb.progressDialog.dismiss();
                    finish();
                }
            }
        }
    }

    public void publishItem(final Map<String,Object> item){
        Rb.utilityPipeline.database.collection("items")
                .document(id)
                .update(item)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // TODO: do something after publish
                        final Map<String,Object> tags = new HashMap<>();
                        tags.put("tags",item.get("tags"));
                        Rb.utilityPipeline.database.collection("keywords")
                                .document(id)
                                .set(tags)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(findViewById(R.id.sell_id),"Success!",Snackbar.LENGTH_LONG).show();
                                        Rb.progressDialog.dismiss();
                                        finish();
                                    }
                                });
                    }
                });
    }

    public List<String> generateRelationalTags(String rawData){
        String[] tmp = rawData.toLowerCase().split(" ");
        List<String> out = new ArrayList<>();
        for(String str : tmp){
            if(str.length()>=2 && !rubbish(str))
                out.add(str);
        }
        return out;
    }

    public boolean rubbish(String str){
        return str=="for" && str=="but" && str=="are" && str=="and" && str=="pre";
    }
}
