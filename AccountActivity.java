package weakling.segunda.mano;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        LinearLayout cover = findViewById(R.id.account_cover);
    }

    public void gotoStore(View v){
        Intent intent = new Intent(AccountActivity.this,StoreActivity.class);
        startActivity(intent);
        finish();
    }

}
