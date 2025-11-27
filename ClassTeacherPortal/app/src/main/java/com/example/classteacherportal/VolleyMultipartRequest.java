package com.example.classteacherportal;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public abstract class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    @Override
    public String getBodyContentType() {
        return mimeType;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Text params
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    buildTextPart(bos, entry.getKey(), entry.getValue());
                }
            }

            // File params
            Map<String, DataPart> data = getByteData();
            if (data != null && data.size() > 0) {
                for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                    buildDataPart(bos, entry.getValue(), entry.getKey());
                }
            }

            // end boundary
            bos.write(("--" + boundary + "--").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return Response.success(response,
                HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    protected abstract Map<String, DataPart> getByteData() throws AuthFailureError;

    // ---- helpers ----

    private void buildTextPart(ByteArrayOutputStream bos,
                               String paramName,
                               String value) throws IOException {
        bos.write(("--" + boundary + "\r\n").getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());
        bos.write("Content-Type: text/plain; charset=UTF-8\r\n\r\n".getBytes());
        bos.write(value.getBytes("UTF-8"));
        bos.write("\r\n".getBytes());
    }

    private void buildDataPart(ByteArrayOutputStream bos,
                               DataPart dataFile,
                               String inputName) throws IOException {

        bos.write(("--" + boundary + "\r\n").getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + inputName
                + "\"; filename=\"" + dataFile.getFileName() + "\"\r\n").getBytes());
        bos.write(("Content-Type: " + dataFile.getType() + "\r\n\r\n").getBytes());

        byte[] fileData = dataFile.getContent();
        bos.write(fileData);
        bos.write("\r\n".getBytes());
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private String type;

        public DataPart(String name, byte[] data) {
            this(name, data, "application/octet-stream");
        }

        public DataPart(String name, byte[] data, String type) {
            this.fileName = name;
            this.content = data;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        // add extra headers if needed
        Map<String, String> headers = new HashMap<>();
        return headers;
    }
}
