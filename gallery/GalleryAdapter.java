package weakling.segunda.mano.gallery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import weakling.segunda.mano.R;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryViewHolder> {
    List<View> mView;
    public GalleryAdapter(List<View> view){
        mView = view;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.gallery_view_holder,viewGroup,false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder galleryViewHolder, int i) {
        galleryViewHolder.bindTo(mView.get(i));
    }

    @Override
    public int getItemCount() {
        return mView.size();
    }
}
