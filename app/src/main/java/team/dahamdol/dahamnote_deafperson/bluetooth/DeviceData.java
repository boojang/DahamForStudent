package team.dahamdol.dahamnote_deafperson.bluetooth;

public class DeviceData {
    private String deviceName;
    private String macAddress;

    public DeviceData(String deviceName, String macAddress){
        this.deviceName = deviceName;
        this.macAddress = macAddress;
    }

    public String getDeviceName(){
        return deviceName;
    }

    public String getMacAddress(){
        return macAddress;
    }
}
