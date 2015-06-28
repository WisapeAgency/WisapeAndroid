package com.wisape.android.util;

import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * @author Duke
 */
public final class FrescoFactory {


    public final static void bindImage(SimpleDraweeView view, int resId) {
        ImageRequest imageRequest =
                ImageRequestBuilder.newBuilderWithResourceId(resId)
//                            .setResizeOptions(new ResizeOptions(view.getLayoutParams().width, view.getLayoutParams().height))
                        .build();
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setOldController(view.getController())
//                    .setControllerListener(mImageView.getListener())
                .setAutoPlayAnimations(true)
                .build();
        view.setController(draweeController);
    }

    public final static void bindImageFromUri(SimpleDraweeView view, String url) {
        ImageRequest imageRequest =
                ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
//                            .setResizeOptions(new ResizeOptions(view.getLayoutParams().width, view.getLayoutParams().height))
                        .build();
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setOldController(view.getController())
//                    .setControllerListener(mImageView.getListener())
                .setAutoPlayAnimations(true)
                .build();
        view.setController(draweeController);
    }
}
