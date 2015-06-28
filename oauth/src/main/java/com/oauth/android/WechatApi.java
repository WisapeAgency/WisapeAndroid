package com.oauth.android;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * Created by Xugm on 15/6/17.
 */
public class WechatApi extends DefaultApi20 {

    private static final String AUTHORIZE_URL = "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code";
    private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s&state=STATE#wechat_redirect";

    @Override
    public String getAccessTokenEndpoint() {
        return "https://api.weixin.qq.com/sns/oauth2/access_token";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        // Append scope if present
        if (config.hasScope()) {
            String callback = config.getCallback();
            try {
                callback = URLEncoder.encode(config.getCallback(), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format(SCOPED_AUTHORIZE_URL,
                    config.getApiKey(),
                    callback,
                    OAuthEncoder.encode(config.getScope()));
        } else {
            return String.format(AUTHORIZE_URL,
                    config.getApiKey(),
                    OAuthEncoder.encode(config.getCallback()));
        }
    }
}
