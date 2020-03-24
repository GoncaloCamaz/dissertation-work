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
        int length = config.getServiceNodes().size();
        int val;
        for(int i = 4; i < headersLen; i++)
        {
            if(i - 4 < length)
            {
                val = config.getServiceNodes().get(i-4).getWithservice();
                str[i] = String.valueOf(val);
            }
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
