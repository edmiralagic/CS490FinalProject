package edu.ccsu.networking.main;

/**
 * Created by edmiralagic on 4/27/16.
 */
public interface CanReceiveMessage {

    public void filterMessage(String method, String data, String ip, String port) throws Exception;

}
