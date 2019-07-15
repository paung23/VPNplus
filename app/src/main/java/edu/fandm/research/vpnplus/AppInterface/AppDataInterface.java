package edu.fandm.research.vpnplus.AppInterface;

import java.util.List;

import edu.fandm.research.vpnplus.Database.DataLeak;
import edu.fandm.research.vpnplus.Database.Traffic;
import edu.fandm.research.vpnplus.Plugin.LeakReport;

public interface AppDataInterface {

    String getAppName();

    String getAppPackageName();

    List<DataLeak> getLeaks(LeakReport.LeakCategory category);

    List<Traffic> getTraffics(boolean encrypted, boolean outgoing);
}
