package weakling.segunda.mano.utils;

import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class CustomSnackbar extends BaseTransientBottomBar<CustomSnackbar> {
    private static View mView;

    protected CustomSnackbar(@NonNull ViewGroup parent, @NonNull View content, @NonNull android.support.design.snackbar.ContentViewCallback contentViewCallback) {
        super(parent, content, contentViewCallback);
    }

    public View findViewById(int resId){
        return mView.findViewById(resId);
    }

    public static CustomSnackbar make(View view, int customLayout, int duration){
        ViewGroup parent = findSuitableParent(view);
        mView = LayoutInflater.from(view.getContext())
                .inflate(customLayout,parent,false);
        return new CustomSnackbar(parent, mView, new android.support.design.snackbar.ContentViewCallback() {
            @Override
            public void animateContentIn(int delay, int duration) {
                ViewCompat.setScaleY(mView, 0f);
                ViewCompat.animate(mView).scaleY(1f).setDuration(duration).setStartDelay(delay);
            }

            @Override
            public void animateContentOut(int delay, int duration) {
                ViewCompat.setScaleY(mView, 1f);
                ViewCompat.animate(mView).scaleY(0f).setDuration(duration).setStartDelay(delay);
            }
        }).setDuration(duration);
    }

    private static ViewGroup findSuitableParent(View view){
        ViewGroup fallback = null;
        View mView = view;
        do{
            if(mView instanceof CoordinatorLayout)
                return (ViewGroup) mView;
            else if(mView instanceof FrameLayout){
                if(mView.getId()==android.R.id.content)
                    return (ViewGroup) mView;
                else
                    fallback = (ViewGroup) mView;
            }
            if(mView != null){
                mView = view.getParent() instanceof View ? (ViewGroup) view.getParent() : null;
            }
        }while(mView != null);
        return fallback;
    }

}
