package weakling.segunda.mano;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import weakling.segunda.mano.authentication.Authentication;
import weakling.segunda.mano.fragments.MyFragment;
import weakling.segunda.mano.utils.MyBitmapFactory;
import weakling.segunda.mano.utils.UtilityPipeline;

 public class MainActivity extends AppCompatActivity {
    Bitmap img_acc = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Rb.utilityPipeline = new UtilityPipeline(getApplicationContext());
        //Rb.query = new Query().set(Query.Type.ARRAY_CONTAINS,"tags","All");
        Rb.activity = this;
        loadBottomNavigation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        setUpMenu(menu);
        /*SearchManager mgr = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.search_bar).getActionView();
        search.setSearchableInfo(mgr.getSearchableInfo(getComponentName()));
        search.setQueryHint("Search Products");*/
        return true;
    }

     private void setUpMenu(Menu menu){
        if(Rb.utilityPipeline.authentication.getCurrentUser()==null){
            menu.findItem(R.id.account_set).getIcon().mutate().setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_IN);
        }else{
            if(img_acc==null){
                try{
                    img_acc = new MyBitmapFactory.GetImageFromUrl().execute(Rb.utilityPipeline.authentication.getCurrentUser().getPhotoUrl().toString()).get();
                    float dp24px = MyBitmapFactory.convertDpToPx(this,24);
                    menu.findItem(R.id.account_set).setIcon(new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(img_acc,(int)dp24px,(int)dp24px,true)));
                } catch (Exception e){
                    //Maybe theres no internet connection
                }
            }
        }
        menu.findItem(R.id.account_set).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO: do some account settings here instead of signing out
                if(Rb.utilityPipeline.authentication.getCurrentUser()==null){
                    Rb.redirect = new Intent(Rb.activity,AccountActivity.class);
                    Intent intent = new Intent(MainActivity.this, Authentication.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });
        menu.findItem(R.id.shopping_cart).getIcon().mutate().setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_IN);
        menu.findItem(R.id.shopping_cart).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this,CartActivity.class);
                startActivity(intent);
                return true;
            }
        });
        menu.findItem(R.id.sell_item).getIcon().mutate().setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_IN);
        menu.findItem(R.id.sell_item).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this,StoreActivity.class);
                intent.putExtra("sell",true);
                startActivity(intent);
                return true;
            }
        });
        menu.findItem(R.id.search_bar).getIcon().mutate().setColorFilter(ContextCompat.getColor(this,android.R.color.white), PorterDuff.Mode.SRC_IN);
        menu.findItem(R.id.search_bar).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO: Do search drawer here
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

     private void loadBottomNavigation(){
        Rb.fragmentAdapter = new MyFragment(this);
        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Rb.fragmentAdapter.setFragment(MyFragment.HOME);
                        return true;
                    case R.id.navigation_trending:
                        Rb.fragmentAdapter.setFragment(MyFragment.HOME);
                        return true;
                    case R.id.navigation_recent:
                        Rb.fragmentAdapter.setFragment(MyFragment.RECENT);
                        return true;
                    case R.id.navigation_notifications:
                        Rb.fragmentAdapter.setFragment(MyFragment.INBOX);
                        return true;
                }
                return false;
            }
        };
        Rb.navigation = findViewById(R.id.navigation);
        Rb.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Rb.navigation.setSelectedItemId(R.id.navigation_home);
    }

}
