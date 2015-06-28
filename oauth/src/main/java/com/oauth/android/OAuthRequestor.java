package com.oauth.android;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.util.Map;

/**
 *
 * Created by Xugm on 15/6/18.
 */
public class OAuthRequestor {

    public static String get(OAuthParams params, String token, String secret, String url, Map<String, String> getParams)
            throws Exception {
        OAuthService service = new ServiceBuilder()
                .provider(params.getApiProvider())
                .apiKey(params.getApiKey())
                .apiSecret(params.getApiSecret())
                .callback(params.getCallback())
                .build();
        OAuthRequest request = new OAuthRequest(Verb.GET, url);
        if (getParams != null && !getParams.isEmpty()) {
            for (Map.Entry<String, String> entry : getParams.entrySet()) {
                request.addQuerystringParameter(entry.getKey(), entry.getValue());
            }
        }
        service.signRequest(new Token(token, secret), request);
        Response response = request.send();
        return response.getBody();
    }

    public static String post(OAuthParams params, String token, String secret, String url, Map<String, String> postParams)
            throws Exception {
        OAuthService service = new ServiceBuilder()
                .provider(params.getApiProvider())
                .apiKey(params.getApiKey())
                .apiSecret(params.getApiSecret())
                .callback(params.getCallback())
                .build();
        OAuthRequest request = new OAuthRequest(Verb.POST, url);
        if (postParams != null && !postParams.isEmpty()) {
            for (Map.Entry<String, String> entry : postParams.entrySet()) {
                request.addBodyParameter(entry.getKey(), entry.getValue());
            }
        }
        service.signRequest(new Token(token, secret), request);
        Response response = request.send();
        return response.getBody();
    }
}