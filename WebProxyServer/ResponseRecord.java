import java.io.File;
import java.time.LocalDate;
import java.time.Period;

public class ResponseRecord {
    String name;
    LocalDate TTL;
    String type;
    String data;
    File site;

    public ResponseRecord(String name, String type, String data, File site){
        this.name = name;
        this.type = type;
        this.data = data;
        this.site = site;

        LocalDate today = LocalDate.now();
        this.TTL = today.plusDays(3);
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public void setFile(File site)
    {
        this.site = site;
    }

    public boolean checkTTL(){
        LocalDate today = LocalDate.now();
        Period difference = Period.between(TTL, today);

        //if this record has existed for more than 3 days, it is expired
        if(difference.getDays() > 3)
        {
            return false;
        }
        return true;
    }


}
