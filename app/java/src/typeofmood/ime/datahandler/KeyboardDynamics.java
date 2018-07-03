package typeofmood.ime.datahandler;


import java.util.ArrayList;
import java.util.Date;


public class KeyboardDynamics {

    public ArrayList<Long> DownTime;
    public ArrayList<Long> UpTime;
    public ArrayList<Float> PressureValue;
    public ArrayList<Integer> IsLongPress;
    public int NumDels;
    public boolean IsSoundOn;
    public boolean IsVibrationOn;
    public boolean IsShowPopupOn;
    public Date StartDateTime;
    public Date StopDateTime;
    public String CurrentAppName;
    public String CurrentMood;
    public String CurrentPhysicalState;

    public KeyboardDynamics()
    {
        DownTime = new ArrayList<Long>();           //done
        UpTime = new ArrayList<Long>();             //done
        PressureValue = new ArrayList<Float>();     //done
        IsLongPress = new ArrayList<Integer>();     //done
        NumDels = 0;                                //done
        IsSoundOn = false;
        IsVibrationOn = false;
        IsShowPopupOn= false;
        StartDateTime = new Date();                 //done
        StopDateTime = new Date();                  //done
        CurrentAppName="undefined";
        CurrentMood="undefined";
        CurrentPhysicalState="undefined";
    }
}
