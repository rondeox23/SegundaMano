package weakling.segunda.mano;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private LayoutInflater inflater;
    List<Integer> icons;
    List<String> suggestions;
    List<String> tags;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Rb.activity = this;
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Rb.utilityPipeline.database.collection("keywords")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        tags = new ArrayList<>();
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            List<String> tag = (List<String>)doc.get("tags");
                            for(String str : tag){
                                tags.add(str);
                            }
                            //tags.addAll((List<String>)doc.get("tags"));
                        }
                        setSearchBar();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(R.id.search_id),e.getMessage(),Snackbar.LENGTH_INDEFINITE).show();
                    }
                });
    }

    public void setSearchBar(){
        inflater = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.action_search, null);
        v.setLayoutParams(new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)));

        Spinner catView = v.findViewById(R.id.search_cat);
        String[] lists = {
                "All",
                "Antiques", "Art", "Baby", "Books", "Gadgets",
                "Business & Industrial", "Cameras & Photo",
                "Cellphones & Accessories", "Clothing, Shoes & Accessories",
                "Coins & Paper Money", "Collectibles",
                "Computer/Tablets & Networking", "Consumer Electronic",
                "Crafts", "Dolls & Bear", "DVDs & Movies", "Motors",
                "Entertainment", "Health & Beauty", "Home & Garden", "Jewelry & Watches",
                "Music", "Musical Instruments & Gear", "Pet Supplies",
                "Pottery & Glass", "Sporting Goods", "Toys & Hobbies"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lists);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        catView.setAdapter(adapter);

        final EditText srch  = ((EditText)v.findViewById(R.id.search_query));
        srch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                icons = new ArrayList<>();
                suggestions = new ArrayList<>();
                //setSuggestions();
                if(count>0){
                    for(int i=0; i<tags.size(); i++){
                        String strA = tags.get(i).toLowerCase();
                        String strB = s.toString().toLowerCase();
                        if(strA.startsWith(strB)){
                            icons.add(R.drawable.ic_search_black_24dp);
                            suggestions.add(tags.get(i));
                        }
                    }
                }
                setSuggestions();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        srch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event.getKeyCode()==KeyEvent.KEYCODE_SEARCH){
                    Rb.searchQuery.setQuery(srch.getText().toString());
                    Rb.fragmentAdapter.setFragment(Rb.fragmentAdapter.HOME);
                    finish();
                }
                return true;
            }
        });
        actionBar.setCustomView(v);
    }

    public void setSuggestions(){
        ListView list = findViewById(R.id.search_list_suggestions);
        Integer[] tmp1 = icons.toArray(new Integer[icons.size()]);
        String[] tmp2 = suggestions.toArray(new String[suggestions.size()]);
        list.setAdapter(new MyAdapter<Integer,String>(this,R.layout.rows_items_4,tmp1,tmp2));
    }

    private class MyAdapter<I,J> extends BaseAdapter {
        private Context context;
        private int layout;
        private I[] res;
        private J[] items;

        public MyAdapter(Context context, int layout, I[] res, J[] items) {
            this.context = context;
            this.layout = layout;
            this.res = res;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.length;
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
            convertView = inflater.inflate(layout,parent,false);
            convertView.setPadding(10,10,10,10);
            ImageView img = convertView.findViewById(R.id._4img);
            TextView text = convertView.findViewById(R.id._4txt);
            img.setImageResource((Integer)res[position]);
            text.setText((String)items[position]);
            convertView.setBackgroundResource(R.drawable.def_state_list);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Rb.searchQuery.setQuery((String)items[position]);
                    Rb.fragmentAdapter.setFragment(Rb.fragmentAdapter.HOME);
                    finish();
                }
            });
            return convertView;
        }

    }

}