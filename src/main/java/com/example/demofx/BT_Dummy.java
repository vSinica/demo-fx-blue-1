package com.example.demofx;


import javax.bluetooth.*;
import javax.microedition.io.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * Class responsible for the configuration/usage of bluetooth.
 *
 * As a client it will try to connect to all devices with the name prefix "BT_"
 * and send "Hello world" to them.
 *
 * @author Fernando Alexandre
 */
public class BT_Dummy extends Thread implements DiscoveryListener {

    /**
     * Service serial-port UUID
     */
    protected UUID defaultUUID = new UUID(0x1101);

    /**
     * Local bluetooth device.
     */
    private LocalDevice local;

    /**
     * Agent responsible for the discovery of bluetooth devices.
     */
    private DiscoveryAgent agent;

    /**
     * Output stream used to send information to the bluetooth.
     */
    private DataOutputStream dout;

    /**
     * Bluetooth Connection.
     */
    private StreamConnection conn;

    /**
     * List of bluetooth devices of interest. (name starting with the defined token)
     */
    private Vector<RemoteDevice> devices;

    /**
     * Services of interest (defined in UUID) of each device.
     */
    private Vector<ServiceRecord> services;

    public BT_Dummy() {
        services = new Vector<ServiceRecord>();
    }

    @Override
    public void run() {
        findDevices();
    }

    /**
     * Find all the discoverable devices in range.
     */
    protected void findDevices(){
        try{
            devices              = new Vector<RemoteDevice>();
            LocalDevice local    = LocalDevice.getLocalDevice();
            DiscoveryAgent agent = local.getDiscoveryAgent();

            agent.startInquiry(DiscoveryAgent.GIAC, this);
            debugString("Starting device discovery...");
        }catch(Exception e) {
            debugString("Error initiating discovery.");
        }
    }

    /**
     * Obtains a list of services with the UUID defined from a device.
     *
     * @param device
     * 		Device to obtain the service list.
     */
    protected void findServices(RemoteDevice device){
        try{
            UUID[] uuids  = new UUID[1];
            uuids[0]      = defaultUUID;    //The UUID of the each service
            local         = LocalDevice.getLocalDevice();
            agent         = local.getDiscoveryAgent();

            agent.searchServices(null, uuids, device, this);
            debugString("Starting Service Discovery...");
        }catch(Exception e){
            debugString("Error finding services.");
        }
    }

    /**
     * Sends a message to all the devices. (using the service)
     *
     * @param str
     * 		Byte array which represents a string.
     */
    public void broadcastCommand(String str) {
        for(ServiceRecord sr : services) {
            String url = sr.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);

            conn = null;

            try {
                debugString("Sending command to " + url);

                conn = (StreamConnection) Connector.open(url);
                dout = new DataOutputStream(conn.openOutputStream());

                dout.writeUTF(str);
                debugString(String.format("Sending %s", str));

                dout.flush();
                dout.close();
                conn.close();

                debugString("Sent. Connection Closed.");

            } catch (Exception e) {
                debugString("Failed to connect to " + url);
                e.printStackTrace();
            }
        }
    }


    @Override
    public void deviceDiscovered(RemoteDevice arg0, DeviceClass arg1) {
        try {
            String name = arg0.getFriendlyName(true);

            debugString("Found device: " + name);

            if(name.startsWith("BT_")) {
                devices.add(arg0);
            }
        } catch (IOException e) {
            debugString("Failed to get remoteDevice Name.");
        }
    }

    @Override
    public void inquiryCompleted(int arg0) {
        debugString("Inquiry Completed.");

        // Start service probing
        for(RemoteDevice d :devices) {
            findServices(d);
        }
    }

    @Override
    public void serviceSearchCompleted(int arg0, int arg1) {
        debugString("Service search completed.");

        broadcastCommand(new String("Hello world!"));
    }

    @Override
    public void servicesDiscovered(int arg0, ServiceRecord[] arg1) {
        for(ServiceRecord x : arg1) {
            services.add(x);
        }
    }

    /**
     * Helper to format a debug string for output.
     *
     * @param str
     * 		Debug Message
     */
    protected static void debugString(String str) {
        System.out.println(String.format("%s :: %s", BT_Dummy.class.getName(), str));
    }
}