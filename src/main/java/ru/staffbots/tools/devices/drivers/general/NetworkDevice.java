package ru.staffbots.tools.devices.drivers.general;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import ru.staffbots.tools.devices.Device;

public abstract class NetworkDevice extends Device {

    private String address = null;
    private int port = 80;
    private int timeout = 1500;

    public NetworkDevice(String name, String note){
        this.name = name;
        this.note = note;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public boolean connected(){
        return (address != null);
    }

    public String getAddress(){
        return address;
    }

    public String get(String query){
        return send(true, query);
    }

    public String post(String query){
        return send(false, query);
    }

    private String send(boolean isGetQuery, String query){
        String badResult = null;
        if (address == null) return badResult;
        String uri = "http://" + address + ":" + port + "/" + query;
        HttpClient httpClient = new HttpClient();
        httpClient.setConnectTimeout(timeout);
        ContentResponse response = null;
        try {
            httpClient.start();
            response = isGetQuery ? httpClient.GET(uri) : httpClient.POST(uri).send();
        } catch (Exception exception) {
            //System.out.println(exception.getMessage());
            //exception.printStackTrace();
            address = null;
            return badResult;
        }
        return (response.getStatus() == 200) ? response.getContentAsString() : badResult;
    }

    static public NetworkDevice convertDevice(Device device){
        return (device instanceof NetworkDevice) ? (NetworkDevice) device : null;
    }

}
