package net.dengzixu.danmumu.filter;

import net.dengzixu.java.filter.Filter;
import net.dengzixu.java.message.Message;
import net.dengzixu.java.message.UserInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.util.Objects;

public class FaceCacheFilter implements Filter {
    @Override
    public void doFilter(Message message) {
        UserInfo userInfo = message.getUserInfo();

        String faceFileName;

        String tempDir = System.getProperty("java.io.tmpdir") + "/danmumu/";

        if (Objects.nonNull(userInfo) && Objects.nonNull(userInfo.getFace()) && !"".equals(userInfo.getFace())) {
            String faceUrlBili = userInfo.getFace();

            if (faceUrlBili.contains("data:")) {
                return;
            }

            faceFileName = faceUrlBili.substring(faceUrlBili.lastIndexOf("/") + 1);

            File file = new File(tempDir + faceFileName);

            if (file.exists()) {
                message.getUserInfo().setFace(toBase64(file));
            } else {
                new Thread(() -> {
                    downloadFile(faceUrlBili, tempDir + faceFileName);
                }).start();
            }
        }
    }

    private String toBase64(File file) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        byte[] bytes;
        try {
            inputStream = new FileInputStream(file);
            outputStream = new ByteArrayOutputStream();

            bytes = new byte[inputStream.available()];

            inputStream.read(bytes);
            outputStream.write(bytes);

            return toBase64(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Objects.requireNonNull(inputStream).close();
                Objects.requireNonNull(outputStream).close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private String toBase64(byte[] bytes) {
        return "data:image/jpg;base64," + Base64.encodeBase64String(bytes);
    }

    private void downloadFile(String url, String filePath) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {
                saveFile(response, new File(filePath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(Response response, File file) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        try {
            inputStream = Objects.requireNonNull(response.body()).byteStream();
            outputStream = new FileOutputStream(file);

            byte[] bytes = Objects.requireNonNull(response.body()).bytes();

            outputStream.write(bytes);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            try {
                Objects.requireNonNull(inputStream).close();
                Objects.requireNonNull(outputStream).close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}
