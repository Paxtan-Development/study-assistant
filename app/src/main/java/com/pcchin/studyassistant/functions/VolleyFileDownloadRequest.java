package com.pcchin.studyassistant.functions;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

/** Request format used by Volley to download a file. **/
public class VolleyFileDownloadRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> requestResponse;
    private Map<String, String> dlParams;

    /** Default constructor. **/
    public VolleyFileDownloadRequest(int method, String mUrl ,Response.Listener<byte[]> listener,
                                    Response.ErrorListener errorListener, HashMap<String, String> params) {
        super(method, mUrl, errorListener);
        setShouldCache(false);
        requestResponse = listener;
        dlParams = params;
    }

    /** Returns the params. Nothing to see here. **/
    @Override
    protected Map<String, String> getParams() {
        return dlParams;
    }

    /** Delivers the response, nothing to see here. **/
    @Override
    protected void deliverResponse(byte[] response) {
        requestResponse.onResponse(response);
    }

    /** Pass on the response data. Nothing to see here. **/
    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success( response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}
