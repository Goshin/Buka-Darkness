package io.goshin.bukadarkness.sited;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Client {
    public static String request(JSONObject params) throws Throwable {
        Socket socket = new Socket(InetAddress.getLocalHost(), Server.PORT);
        Writer out = new OutputStreamWriter(socket.getOutputStream(), "utf-8");
        out.write(URLEncoder.encode(params.toString(), "utf-8") + "\n");
        out.flush();
        Reader in = new InputStreamReader(socket.getInputStream(), "utf-8");
        StringBuilder inputString = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if (((char) c) == '\n')
                break;
            inputString.append((char) c);
        }
        socket.close();
        return URLDecoder.decode(inputString.toString(), "utf-8");
    }
}
