package weakling.segunda.mano.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import weakling.segunda.mano.R;

public class Recent extends Fragment {
    private LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        return this.inflater.inflate(R.layout.fragment_cart,container,false);
    }
}
