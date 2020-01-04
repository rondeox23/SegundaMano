package weakling.segunda.mano.gallery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;

import weakling.segunda.mano.R;

public class GalleryViewHolder extends RecyclerView.ViewHolder {
    LinearLayout container;
    public GalleryViewHolder(@NonNull View itemView) {
        super(itemView);
        container = itemView.findViewById(R.id.gallery_container);
    }

    public void bindTo(View v){
        if(v.getParent()==null) {
            container.addView(v);
            ViewGroup.LayoutParams params = container.getLayoutParams();
            if (params instanceof FlexboxLayout.LayoutParams) {
                ((FlexboxLayout.LayoutParams) params).setFlexGrow(1);
            }else{
                ((FlexboxLayoutManager.LayoutParams) params).setFlexGrow(1);
            }
        }
    }

}
