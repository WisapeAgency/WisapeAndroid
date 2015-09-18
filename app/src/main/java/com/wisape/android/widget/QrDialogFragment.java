package com.wisape.android.widget;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.wisape.android.R;
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
        View veiw = inflater.inflate(R.layout.fragment_qr, container, false);
        initView(veiw);
        return veiw;
    }

    private void initView(View veiw) {
        ImageView imgQr = (ImageView) veiw.findViewById(R.id.img_qr);
        TextView textSave = (TextView) veiw.findViewById(R.id.text_save);
        TextView textCancle = (TextView) veiw.findViewById(R.id.text_cancle);
        try {
            bitmap = Utils.createQRCode(url, QR_WIDTH_AND_HEIGHT);
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
                saveQrToLocal(bitmap);
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

    private void saveQrToLocal(Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(savePath + "/" + QR_NAME);
            if (file.exists()) {
                file.delete();
            }
            fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            bitmap.recycle();
        } catch (IOException e) {
            Log.e(TAG, "保存二维码失败!");
        } finally {
            if (null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
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
