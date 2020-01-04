package weakling.segunda.mano.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weakling.segunda.mano.R;
import weakling.segunda.mano.Rb;

public class Inbox extends Fragment {
    private LayoutInflater inflater;
    private GridView grid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        return inflater.inflate(R.layout.fragment_inbox,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        grid = Rb.activity.findViewById(R.id.grid_view_inbox);
        /*if(Rb.utilityPipeline.authentication.getCurrentUser()!=null)
        Rb.utilityPipeline.database.collection("notifications")
                .whereEqualTo("to",Rb.utilityPipeline.authentication.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc : queryDocumentSnapshots){
                            data.add(doc.getData());
                            final MyAdapter adapter = new MyAdapter(Rb.activity);
                            grid.setAdapter(adapter);
                        }
                        if(queryDocumentSnapshots.size()<=0){
                            getActivity().findViewById(R.id.inbox_none).setVisibility(View.VISIBLE);
                        }
                    }
                });*/
    }

    private List<Map<String,Object>> data = new ArrayList<>();

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
        public View getView(int position, View convertView, ViewGroup parent) {
            if(inflater==null) inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.rows_items_2,parent,false);
            TextView titl = convertView.findViewById(R.id._2txt1);
            TextView desc = convertView.findViewById(R.id._2txt2);
            titl.setText(data.get(position).get("title").toString());
            desc.setText(data.get(position).get("message").toString());
            return convertView;
        }
    }

}
