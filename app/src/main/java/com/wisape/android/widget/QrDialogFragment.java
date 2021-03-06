package com.wisape.android.widget;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.wisape.android.R;
import com.wisape.android.util.FileUtils;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 二维码fragment
 * Created by huangmeng on 15/9/16.
 */
public class QrDialogFragment extends DialogFragment {

    private static final String TAG = QrDialogFragment.class.getSimpleName();

    private static final String EXTRAS_URL = "url";
    private static final String EXRAS_SAVE_PATH = "save_path";
    private static final int QR_WIDTH_AND_HEIGHT = 600;
    private static final String QR_NAME = "qr.jpg";

    private String url;
    private String savePath;
    private Bitmap bitmap;
    public DisplayMetrics mDisplayMetrics;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (null != args) {
            url = args.getString(EXTRAS_URL);
            savePath = args.getString(EXRAS_SAVE_PATH);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View veiw = inflater.inflate(R.layout.fragment_qr, container, false);
        mDisplayMetrics = getResources().getDisplayMetrics();
        int width =(int)(mDisplayMetrics.widthPixels * 0.6);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,width);
        veiw.setLayoutParams(params);
        initView(veiw);
        return veiw;
    }

    private void initView(View veiw) {
        ImageView imgQr = (ImageView) veiw.findViewById(R.id.img_qr);
        TextView textSave = (TextView) veiw.findViewById(R.id.text_save);
        TextView textCancle = (TextView) veiw.findViewById(R.id.text_cancle);
        try {
            bitmap = FileUtils.Create2DCode(url);
            if (null != bitmap) {
                imgQr.setImageBitmap(bitmap);
            }
        } catch (WriterException e) {
            Utils.showToast(getActivity(),"生成二维码失败");
            dismiss();
        }

        textSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileUtils.saveBitmap(savePath + "/" + QR_NAME, bitmap);
                dismiss();
            }
        });

        textCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }


    public static QrDialogFragment instance(String url, String savePath) {
        Bundle args = new Bundle();
        args.putString(EXTRAS_URL, url);
        args.putString(EXRAS_SAVE_PATH, savePath);
        QrDialogFragment qrDialogFragment = new QrDialogFragment();
        qrDialogFragment.setArguments(args);
        return qrDialogFragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != bitmap){
            bitmap.recycle();
        }
    }
}
