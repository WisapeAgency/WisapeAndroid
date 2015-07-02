package com.wisape.android.activity;

import android.os.Bundle;

/**
 * Created by LeiGuoting on 2/7/15.
 */
/*package*/ interface AsyncTaskLoaderCallback<Data>{
    Data onAsyncLoad(int what, Bundle args) throws AsyncLoaderError;

    class AsyncLoaderError extends Exception{}
}
