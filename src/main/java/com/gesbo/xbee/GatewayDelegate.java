package com.gesbo.xbee;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
import com.rapplogic.xbee.jssc.JSSCXBeeConnection;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayDelegate.class);

    private String portName;

    private XBee xbee;

    public GatewayDelegate() {
        String[] portNames = SerialPortList.getPortNames();
        if (portNames.length != 1) {
            throw new RuntimeException(String.format("%d ports found but one was expected", portNames.length));
        }
        portName = portNames[0];
        LOG.info("Will use port {}", portName);
    }

    public void run() {
        try {
            xbee = new XBee();
            xbee.initProviderConnection(new JSSCXBeeConnection(portName));

            int maxPackets = 10;
            int processedPackets = 0;

            PacketListener listener = new PacketListener() {

                @Override
                public void processResponse(XBeeResponse response) {
                    if(response instanceof ZNetRxIoSampleResponse) {
                        LOG.info("Received analog reading {}", ((ZNetRxIoSampleResponse)response).getAnalog0());
                    } else {
                        LOG.warn("Unexpected response type {}, ignoring", response.getClass().getName());
                    }

                    synchronized (this) {
                        notify();
                    }
                }

            };

            xbee.addPacketListener(listener);

            while (processedPackets < maxPackets) {
                synchronized (listener) {
                    listener.wait();
                    processedPackets++;
                }
            }

        } catch (XBeeException e) {
            LOG.error("Error initialising XBee connection", e);
        } catch (SerialPortException e) {
            LOG.error("Error initialising XBee connection", e);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            LOG.info("Closing XBee connection");
            xbee.close();
        }
    }

}
