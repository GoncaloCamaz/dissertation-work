import java.util.List;

public class Helper
{
    public String[] convertListToString(Configuration config, int headersLen)
    {
        String[] str = new String[headersLen];
        str[0] = String.valueOf(config.getId());
        str[1] = String.valueOf(config.getOriginNodeID());
        str[2] = String.valueOf(config.getBandwidthConsumption());
        int length = config.getServiceNodes().size();
        for(int i = 3; i < headersLen; i++)
        {
            if(i - 3 < length)
            {
                str[i] = String.valueOf(config.getServiceNodes().get(i-3).getWithservice());
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
