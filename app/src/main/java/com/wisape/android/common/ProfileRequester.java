//package com.wisape.android.common;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
///**
// * Created by LeiGuoting on 6/7/15.
// */
//public interface ProfileRequester<P extends ProfileRequester.Param>{
//
//    ProfileInfo request(P param);
//
//    class ProfileInfo{
//        ProfileInfo(){}
//
//        ProfileInfo(String platform){
//            this.platform = platform;
//        }
//
//        public String platform;
//        public String nickName;
//        public String icon;
//        public String uniqueStr;
//        public String email;
//    }
//
//    class Param implements Parcelable{
//        public String token;
//        public String screen;
//
//
//        @Override
//        public int describeContents() {
//            return 0;
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            dest.writeString(this.token);
//            dest.writeString(this.screen);
//        }
//
//        public Param() {
//        }
//
//        protected Param(Parcel in) {
//            this.token = in.readString();
//            this.screen = in.readString();
//        }
//
//        public static final Creator<Param> CREATOR = new Creator<Param>() {
//            public Param createFromParcel(Parcel source) {
//                return new Param(source);
//            }
//
//            public Param[] newArray(int size) {
//                return new Param[size];
//            }
//        };
//    }
//}
