package tool;

import spiderpak.utils.Log;

import java.io.IOException;
import java.net.*;

public class PingTool {
    public static int pingUrl(final String proxyIp, final int port, int timeout, final String address,final String protocol) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, port));
        try {
            final URL url = new URL(protocol+"://" + address);
            final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection(proxy);
            urlConn.setConnectTimeout(1000 * timeout); // mTimeout is in seconds
            urlConn.setReadTimeout(1000 * timeout);
            final long startTime = System.currentTimeMillis();

            urlConn.connect();
            final long endTime = System.currentTimeMillis();
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.info("Time (ms) : " + (endTime - startTime));
                Log.info("Ping to "+address +" was success");
                return (int) (endTime - startTime);
            }
        } catch (final MalformedURLException e1) {
            e1.getMessage();
        } catch (final IOException e) {
            e.getMessage();
        }
        return -1;
    }
}
