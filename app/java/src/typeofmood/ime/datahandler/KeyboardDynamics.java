package typeofmood.ime.datahandler;


import java.util.ArrayList;
import java.util.Date;


public class KeyboardDynamics {

    public ArrayList<Long> DownTime;
    public ArrayList<Long> UpTime;
    public ArrayList<Float> PressureValue;
    public ArrayList<Integer> IsLongPress;
    public ArrayList<Double> Distance;
    public int NumDels;
    public boolean IsSoundOn;
    public boolean IsVibrationOn;
    public boolean IsShowPopupOn;
    public long StartDateTime;
    public long StopDateTime;
    public String CurrentAppName;
    public String CurrentMood;
    public String CurrentPhysicalState;
    public long LatestNotification;

    public KeyboardDynamics()
    {
        DownTime = new ArrayList<Long>();           //done
        UpTime = new ArrayList<Long>();             //done
        PressureValue = new ArrayList<Float>();     //done
        IsLongPress = new ArrayList<Integer>();     //done
        Distance = new ArrayList<Double>();
        NumDels = 0;                                //done
        IsSoundOn = false;
        IsVibrationOn = false;
        IsShowPopupOn= false;
        StartDateTime = 0;                 //done
        StopDateTime = 0;                  //done
        LatestNotification= 0;
        CurrentAppName="undefined";
        CurrentMood="undefined";
        CurrentPhysicalState="undefined";
    }
}
