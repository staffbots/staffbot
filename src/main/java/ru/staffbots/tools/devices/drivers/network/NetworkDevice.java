package ru.staffbots.tools.devices.drivers.network;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import ru.staffbots.Staffbot;
import ru.staffbots.tools.Translator;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.webserver.WebServer;

public abstract class NetworkDevice extends Device {

    private AddressSettings addressSettings;
    private int timeout = 1500;
    private HttpClient httpClient = new HttpClient();
    private boolean connected = false;

    public NetworkDevice(AddressSettings addressSettings, String name, String note){
        this.addressSettings = addressSettings;
        this.name = name;
        this.note = note;
        httpClient.setConnectTimeout(timeout);
    }

    public boolean connected(){
        return connected(false);
    }

    private boolean connected(boolean tryConnect){
        return tryConnect ? connect() : connected;
    }

    public boolean disconnected(){
        return !connected;
    }

    private boolean connect(){
        connected = true; //need for right work send-method
        post("device_name=" + name);
        return connected;
    }

    public AddressSettings getAddressSettings(){
        return addressSettings;
    }

    public Double getAsDouble(String query, Double defaultValue){
        String stringValue = get(query);
        if ((stringValue == null) || stringValue.isEmpty())
            return defaultValue;
        try {
            return Double.parseDouble(stringValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String get(String query){
        return send(true, query);
    }

    public String post(String query){
        return send(false, query);
    }

    private String send(boolean isGetQuery, String query){
        String badResult = null;
        if (disconnected())
            if (!connect())
                return badResult;
        String uri = "http://" + addressSettings.getAddress() + ":" + WebServer.httpPort + "/" + query;
        ContentResponse response;
        try {
            httpClient.start();
            response = isGetQuery ? httpClient.GET(uri) : httpClient.POST(uri).send();
        } catch (Exception exception) {
            connected = false;
            return badResult;
        }
        connected = (response.getStatus() == 200);
        System.out.println(" status:" + response.getStatus() + "   query:" + query + "   value:" + response.getContentAsString().trim());
        if (response.getStatus() == 500)
            return send(isGetQuery, query); //reconnect in case reboot remote device
        return connected ? response.getContentAsString() : badResult;
    }

    @Override
    public String getNote(){
        return super.getNote() + (connected(true) ? "" :
            " (" + Translator.getValue("device_disconnect") + ")");
    }

    static public NetworkDevice convertDevice(Device device){
        return (device instanceof NetworkDevice) ? (NetworkDevice) device : null;
    }

    /**
     * Link to resource ino-file contein sketch for remote device.
     * This sketch diferent for diferent devices.
     * Every device type descripted diferent class.
     * Class name is set in method as parameter.
     **/
    protected String getInoResourceLink(String className){
        String shortClassName = className.replaceFirst("Device", "");
        return "resource?ino/" + Staffbot.solutionName.toLowerCase() + "/" + shortClassName + ".ino"
                + "&" + name;
    }

}
