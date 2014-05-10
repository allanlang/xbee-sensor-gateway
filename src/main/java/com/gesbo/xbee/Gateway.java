package com.gesbo.xbee;

public class Gateway {

    public static void main(String[] args) throws Exception {
        GatewayDelegate delegate = new GatewayDelegate();
        delegate.run();
    }

}
