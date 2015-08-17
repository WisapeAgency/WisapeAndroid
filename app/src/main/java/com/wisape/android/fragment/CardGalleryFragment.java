package com.wisape.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.wisape.android.R;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.TestActivity;
import com.wisape.android.util.image.StackBlur;
//import com.wisape.android.view.GalleryRelativeLayoutWrapper;
import com.wisape.android.view.GalleryView;
import com.wisape.android.widget.PopupWindowMenu;

import org.w3c.dom.Text;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * @author Duke
 */
public class CardGalleryFragment extends AbsFragment {

    private static final String TAG = CardGalleryFragment.class.getSimpleName();

    @InjectView(R.id.card_gallery)
    GalleryView mCardGallery;
    @InjectView(R.id.add_story)
    AddFloatingActionButton mAddStory;
    @InjectView(R.id.gift)
    ImageView mGift;

    private PopupWindowMenu popupWindow;




    GalleryAdapter mGalleryAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_gallery, null, false);
        ButterKnife.inject(this, rootView);
        popupWindow = new PopupWindowMenu(getActivity());
        init();
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        StackBlur.init();
    }

    @OnClick(R.id.add_story)
    @SuppressWarnings("unused")
    protected void doAddStory() {
        //StoryTemplateActivity.launch(this, 0);
        TestActivity.launch(getActivity(), 0);
    }

    @OnClick(R.id.gift)
    public void showGift() {

        FragmentManager fm = getActivity().getSupportFragmentManager();

        FragmentTransaction trans = fm.beginTransaction();

        trans.add(R.id.drawer_main, new GiftFragment());

        trans.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        StackBlur.free();
    }

    private int mPrimaryPosition;

    private void init() {
        mCardGallery.setSpace((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mDisplayMetrics));
        mCardGallery.setAdapter(mGalleryAdapter = new GalleryAdapter(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        popupWindow.dismiss();
    }

    public class GalleryAdapter extends RecyclerView.Adapter<GHolder> {

        private Context mContext;

        public GalleryAdapter(Context c) {
            this.mContext = c;
        }

        @Override
        public GHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_main_view_card_item, parent, false);
            GHolder holder = new GHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(GHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 1;
        }

        public int getItem(int position) {
            return 1;
        }
    }

    public class GHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.main_story_bg)
        RelativeLayout mStoryBg;
        @InjectView(R.id.text_story_state)
        TextView mTextStoryState;
        @InjectView(R.id.text_eye_count)
        TextView mTextEyecount;
        @InjectView(R.id.text_zan_count)
        TextView mTextZanCount;
        @InjectView(R.id.text_share_count)
        TextView mTextShareCount;
        @InjectView(R.id.story_name)
        TextView mTextStoryName;


        public GHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }

        /**
         * 弹出菜单
         */
        @OnClick(R.id.main_story_more)
        public void onImgMoreClicked() {
            showPopuWindowMenu();
        }

    }

    private void showPopuWindowMenu(){
        if (!popupWindow.isShowing()) {
            popupWindow.showAtLocation(getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        }    }


    @OnClick(R.id.menu_switch)
    public void onClickMenuSwitch(View view) {
        Activity activity = getActivity();
        if (null == activity) return;
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).openOrCloseMainMenu();
        }
    }
}
