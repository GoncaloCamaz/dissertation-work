import java.util.List;

public class Helper
{
    public String[] convertListToString(RequestConfig config, int headersLen)
    {
        String[] str = new String[headersLen];
        str[0] = String.valueOf(config.getId());
        str[1] = String.valueOf(config.getOriginNodeID());
        str[2] = String.valueOf(config.getDestinationNodeID());
        str[3] = String.valueOf(config.getBandwidthConsumption());
        int length = config.getServicesRequested().size();
        int val;

        for(int i = 0; i < length; i++)
        {
            val = config.getServicesRequested().get(i);
            str[i+4] = String.valueOf(val);
        }

        return str;
    }

    public String[] genHeaders(List<String> headers)
    {
        String[] str = new String[headers.size()];
        int i = 0;

        for(String obj : headers)
        {
            str[i] = obj;
            i++;
        }

        return str;
    }
}
