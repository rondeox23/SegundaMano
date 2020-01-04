package weakling.segunda.mano.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import weakling.segunda.mano.R;

public class MyFragment {
    public static final int HOME = 0;
    public static final int RECENT = 1;
    public static final int INBOX = 2;

    private Fragment[] fragments;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private int currentFragment = -1;

    public MyFragment(AppCompatActivity activity) {
        fragments = new Fragment[]{
            new Home(),
            new Recent(),
            new Inbox()
        };
        fragmentManager = activity.getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.pager,fragments[HOME]);
        fragmentTransaction.add(R.id.pager,fragments[RECENT]);
        fragmentTransaction.add(R.id.pager,fragments[INBOX]);
        fragmentTransaction.commit();
    }

    public void setFragment(int fragment){
        if(currentFragment!=fragment){
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.pager,fragments[fragment]);
            fragmentTransaction.commit();
            currentFragment = fragment;
        }else{
            ((Home)fragments[fragment]).update();
        }
    }

}