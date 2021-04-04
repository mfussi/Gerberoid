package se.pp.mc.android.Gerberoid;

import java.io.IOException;
import java.util.HashMap;

import fi.iki.elonen.NanoHTTPD;
import se.pp.mc.android.Gerberoid.activities.MainActivity;

public class WebServer {

    private String path;
    private int port;

    private Server server;

    private boolean running = false;
    private MainActivity activity;

    public WebServer(int port, String path){
        this.path = path;
        this.port = port;
        server = new Server(port);
    }

    public void start(MainActivity activity) {

        if(!running) {

            this.activity = activity;

            try {
                server.start(2000, false);
                running = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void stop() {

        activity = null;

        if(running){
            server.stop();
            running = false;
        }

    }

    public boolean isRunning() {
        return running;
    }

    public class Server extends NanoHTTPD {

        public Server(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {

            try {

                if (session.getUri().startsWith(path)) {

                    final NanoHTTPD.Method method = session.getMethod();
                    final String function = session.getUri().replace(path, "");

                    if (method != Method.POST) {
                        return sendResponse(false);
                    }

                    HashMap<String, String> files = new HashMap<>();

                    try {

                        session.parseBody(files);

                    } catch (Exception e) {

                       return sendResponse(false);

                    }

                    if ("fullscreen".equals(function)) {

                        final boolean enable = getParameter(session, "enable", false);
                        execute(() -> activity.enableFullscreen(enable));
                        return sendResponse(true);

                    } else if ("overlay".equals(function)) {

                        final boolean enable = getParameter(session, "enable", false);
                        execute(() -> activity.enableOverlay(enable));
                        return sendResponse(true);

                    } else if ("load/gerber".equals(function)) {

                        final String data = files.get("postData");
                        if(data != null && !data.equals("")){

                            execute(() -> activity.load(data, true));
                            return sendResponse(true);

                        } else {
                            return sendResponse(false);
                        }

                    } else if ("load/drill".equals(function)) {

                        final String data = files.get("postData");
                        if(data != null && !data.equals("")){

                            execute(() -> activity.load(data, true));
                            return sendResponse(true);

                        } else {
                            return sendResponse(false);
                        }

                    } else if ("clear".equals(function)) {

                        execute(() -> activity.clearLayers());
                        return sendResponse(true);

                    } else if ("zoom/in".equals(function)) {

                        execute(() -> activity.zoomIn());
                        return sendResponse(true);

                    } else if ("zoom/out".equals(function)) {

                        execute(() -> activity.zoomOut());
                        return sendResponse(true);

                    } else if ("zoom/fit".equals(function)) {

                        execute(() -> activity.zoomFit());
                        return sendResponse(true);

                    } else if ("move".equals(function)) {

                        final boolean abs = getParameter(session, "absolute", false);
                        final int x = getParameter(session, "x", 0);
                        final int y = getParameter(session, "y", 0);

                        execute(() -> activity.move(x, y, abs));
                        return sendResponse(true);

                    } else if ("scale".equals(function)) {

                        final boolean abs = getParameter(session, "absolute", false);
                        final float scale = getParameter(session, "scale", (abs) ? 1.0f : 0.0f);

                        execute(() -> activity.scale(scale, abs));
                        return sendResponse(true);

                    } else if ("layer/color".equals(function)){

                        final int color = getParameter(session, "color", 4);
                        final int layer = getParameter(session, "layer", 0);

                        if(color >= 0 && layer >= 0) {
                            execute(() -> activity.setLayerColor(color, layer));
                            return sendResponse(true);
                        } else {
                            return sendResponse(false);
                        }

                    }

                }

            } catch (Exception e){
                e.printStackTrace();
            }

            return sendResponse(false);

        }

        private void execute(Runnable runnable){

            if(activity != null) {

                try {
                    activity.runOnUiThread(runnable);
                } catch (Exception e){
                    e.printStackTrace();
                }

            }

        }

        private Response sendResponse(boolean success) {

            final Response.Status status = (success) ? Response.Status.OK : Response.Status.NOT_FOUND;
            final String body = (success) ? "true" : "false";

            return newFixedLengthResponse(status, "text/html", body);

        }

        private boolean getParameter(NanoHTTPD.IHTTPSession session, String key, boolean defaultValue) {

            if(session.getParms().containsKey(key)){
                return Boolean.parseBoolean(session.getParms().get(key));
            } else {
                return defaultValue;
            }

        }

        private int getParameter(NanoHTTPD.IHTTPSession session, String key, int defaultValue) {

            if(session.getParms().containsKey(key)){
                return Integer.parseInt(session.getParms().get(key));
            } else {
                return defaultValue;
            }

        }

        private float getParameter(NanoHTTPD.IHTTPSession session, String key, float defaultValue) {

            if(session.getParms().containsKey(key)){
                return Float.parseFloat(session.getParms().get(key));
            } else {
                return defaultValue;
            }

        }

        private String getParameter(NanoHTTPD.IHTTPSession session, String key, String defaultValue) {

            if(session.getParms().containsKey(key)){
                return session.getParms().get(key);
            } else {
                return defaultValue;
            }

        }

    }

}
