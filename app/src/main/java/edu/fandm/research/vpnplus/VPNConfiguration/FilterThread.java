package edu.fandm.research.vpnplus.VPNConfiguration;

import java.util.concurrent.LinkedBlockingQueue;

import edu.fandm.research.vpnplus.Plugin.IPlugin;
import edu.fandm.research.vpnplus.Plugin.LeakReport;
import edu.fandm.research.vpnplus.Plugin.TrafficRecord;
import edu.fandm.research.vpnplus.Plugin.TrafficReport;
import edu.fandm.research.vpnplus.Helpers.Logger;
import edu.fandm.research.vpnplus.VPNConfiguration.VPNservice.MyVpnService;

public class FilterThread extends Thread {
    private static final String TAG = FilterThread.class.getSimpleName();
    private static final boolean DEBUG = false;
    private LinkedBlockingQueue<FilterMsg> toFilter = new LinkedBlockingQueue<>();
    private MyVpnService vpnService;
    ConnectionMetaData metaData;

    public FilterThread(MyVpnService vpnService) {
        this.vpnService= vpnService;
    }

    public FilterThread(MyVpnService vpnService, ConnectionMetaData metaData) {
        this.vpnService = vpnService;
        this.metaData = metaData;
    }

    public void offer(String msg, ConnectionMetaData metaData) {
        FilterMsg filterData = new FilterMsg(msg, metaData);
        toFilter.offer(filterData);
    }

    public void filter(String msg) {
        filter(msg, metaData);
    }

    public void filter(String msg, ConnectionMetaData metaData) {

        TrafficReport traffic;
        TrafficRecord record = vpnService.getTrafficRecord();
        traffic = record.handle(msg);

        if(traffic != null){
            traffic.metaData = metaData;
            vpnService.addtotraffic(traffic);
        }

        if(metaData.outgoing) {

            Logger.logTraffic(metaData, msg);

            for (IPlugin plugin : vpnService.getNewPlugins()) {
                LeakReport leak = plugin.handleRequest(msg);
                if (leak != null) {
                    leak.metaData = metaData;
                    vpnService.notify(msg, leak);
                    if (DEBUG) Logger.v(TAG, metaData.appName + " is leaking " + leak.category.name());
                    Logger.logLeak(leak.category.name());
                }
            }
        }
    }

    public void run() {
        try {
            while (!interrupted()) {
                FilterMsg temp = toFilter.take();
                filter(temp.msg, temp.metaData);
            }
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    class FilterMsg {
        ConnectionMetaData metaData;
        String msg;

        FilterMsg(String msg, ConnectionMetaData metaData) {
            this.msg = msg;
            this.metaData = metaData;
        }
    }
}
