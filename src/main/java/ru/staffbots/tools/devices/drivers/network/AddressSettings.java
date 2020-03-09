package ru.staffbots.tools.devices.drivers.network;

/**
 *
 */
public class AddressSettings {

    private String address;
    private String gateway;
    private String subnetMask;

    public AddressSettings(String address) {
        this.address = address;
        setDefaultGateway();
        setDefaultSubnetMask();
    }

    public AddressSettings(String address, String gateway) {
        this.address = address;
        this.gateway = gateway;
        setDefaultSubnetMask();
    }

    public AddressSettings(String address, String gateway, String subnetMask) {
        this.address = address;
        this.gateway = gateway;
        this.subnetMask = subnetMask;
    }

    private int[] parseIP(String ip){
        String[] bytes = ip.split("\\.");
        int[] result = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            try {
                result[i] = Integer.parseInt(bytes[i]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        return result;
    }

    private String byteToIP(int[] bytes){
        return byteToIP(bytes, '.');
    }

    private String byteToIP(int[] bytes, char separator){
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            result += result.isEmpty() ? "" : separator;
            result += bytes[i];
        }
        return result;
    }

    private void setDefaultGateway() {
        setDefaultGateway(1);
    }

    private void setDefaultGateway(int lastByte) {
        int[] gatewayBytes = parseIP(address);
        if (gatewayBytes.length > 0)
            gatewayBytes[gatewayBytes.length - 1] = lastByte;
        gateway = byteToIP(gatewayBytes);
    }

    private void setDefaultSubnetMask(){
        int[] addressBytes = parseIP(address);
        int[] gatewayBytes = parseIP(gateway);
        subnetMask = "";
        int N = addressBytes.length; // usually is 4
        for (int i = 0; i < N; i++)
            if (addressBytes[i] == gatewayBytes[i]) {
                subnetMask += subnetMask.isEmpty() ? "255" : ".255";
            } else {
                subnetMask += subnetMask.isEmpty() ? "0" : ".0";
                for (int j = i + 1; j < N; j++)
                    subnetMask += ".0";
                break;
            }
    }

    public String getAddress(){
        return address;
    }

    public String getAddress(boolean forSketch){
        return forSketch ? byteToIP(parseIP(address),',') : getAddress();
    };

    public String getGateway(){
        return gateway;
    }

    public String getGateway(boolean forSketch){
        return forSketch ? byteToIP(parseIP(gateway),',') : getGateway();
    };

    public String getSubnetMask(){
        return subnetMask;
    }

    public String getSubnetMask(boolean forSketch){
        return forSketch ? byteToIP(parseIP(subnetMask),',') : getSubnetMask();
    };

}
