//package com.wisape.android.network;
//
//import android.net.Uri;
//import android.util.Base64;
//import android.util.Log;
//
//import java.io.BufferedInputStream;
//import java.io.FileInputStream;
//import java.io.IOException;
//
//import fi.iki.elonen.NanoHTTPD;
//
///**
// * Created by LeiGuoting on 8/7/15.
// */
//public class NanoServer extends NanoHTTPD {
//    public static final String TAG = NanoServer.class.getSimpleName();
//
//    public static final String HOST = "127.0.0.1";
//    public static final int PORT = 7029;
//    public static final String AUTHORITY = String.format("%1$s:%2$s", HOST, Integer.toString(PORT));
//
//    public static final String PATH_IMAGE = "/image";
//
//    public static final String MIME_IMAGE_JPEG = "image/jpeg";
//    public static final String MIME_IMAGE_PNG = "image/png";
//    public static final String MIME_IMAGE_GIF = "image/gif";
//
//    private static NanoServer server = new NanoServer();
//
//    public static NanoServer launch(){
//        return server;
//    }
//
//    public static Uri makeImageUrl(String localPath){
//        Uri.Builder builder = new Uri.Builder();
//        builder.scheme("http");
//        builder.encodedAuthority(AUTHORITY);
//        builder.encodedPath(PATH_IMAGE);
//
//        String newSegment = Base64.encodeToString(localPath.getBytes(), Base64.DEFAULT);
//        builder.appendEncodedPath(newSegment);
//        Uri uri = builder.build();
//        Log.d(TAG, "#makeImageUrl uri:" + uri.toString());
//        return uri;
//    }
//
//    public NanoServer(){
//        super(HOST, PORT);
//        try{
//            start();
//        }catch (IOException e){
//            throw new IllegalStateException(e);
//        }
//    }
//
//    @Override
//    public Response serve(IHTTPSession session) {
//        String uriStr = session.getUri();
//        Uri uri = Uri.parse(uriStr);
//        String path = uri.getPath();
//        Log.d(TAG, "#serve uri:" + uriStr + ", path:" + path);
//        if(path.startsWith(PATH_IMAGE)){
//            String segment = uri.getLastPathSegment();
//            String localPath = new String(Base64.decode(segment.getBytes(), Base64.DEFAULT));
//            Log.d(TAG, "#serve segment:" + segment + ", localPath:" + localPath);
//
//            Response response;
//            try{
//                FileInputStream fileInputStream = new FileInputStream(localPath);
//                BufferedInputStream inputStream = new BufferedInputStream(fileInputStream);
//                response = new Response(Response.Status.OK, MIME_IMAGE_JPEG, inputStream);
//            }catch (IOException e){
//                response = new Response(Response.Status.NOT_FOUND, MIME_IMAGE_JPEG, "Not Found");
//            }
//            return response;
//        }
//        return super.serve(session);
//    }
//}
