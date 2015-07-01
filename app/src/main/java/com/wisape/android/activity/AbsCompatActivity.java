package com.wisape.android.activity;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * @author Duke
 */
public abstract class AbsCompatActivity extends AppCompatActivity {
    private boolean destroyed;

    @Override
    protected void onDestroy() {
        if(17 > Build.VERSION.SDK_INT){
            destroyed = true;
        }
        super.onDestroy();
    }

    public boolean isDestroyed() {
        if(17 > Build.VERSION.SDK_INT){
            return destroyed;
        }else{
            return super.isDestroyed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(android.R.id.home == item.getItemId()){
            return onBackNavigation();
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    protected boolean onBackNavigation(){
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }
}
