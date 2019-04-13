package RegExModel;

/**
 * Created by sambi on 4/13/2019.
 */
public class Util {
    /**
     *
     * @param tracking_num - 14 char tracking number (acct#, svcID, pkg serial)
     * @return returns full tracking number
     * @throws BadTrackingNumberFormatException when tracking_num isn't 14 char
     * @pre tracking_num does not have a check digit already
     */
    public static String findCheckDigit(String tracking_num) throws BadTrackingNumberFormatException{
        if (tracking_num.length() != 14) {
            throw new BadTrackingNumberFormatException();
        }
        int accum = 0;
        for(int i = 0 ; i < 14; i++){
            accum += (int) tracking_num.charAt(i);
        }
        accum %= 17;

        accum += 74;
        char chk = (char) accum;
        return (tracking_num + chk);

    }

    /**
     *
     * @param tracking_num - 15 char tracking number
     * @return returns true or false as to whether the check digit is correct.
     * @throws BadTrackingNumberFormatException when you provide not 15 characters
     */
    public static boolean validateCheckDigit(String tracking_num) throws BadTrackingNumberFormatException{
        if(tracking_num.length() != 15){
            throw new BadTrackingNumberFormatException();
        }
        int accum = 0;
        for(int i = 0 ; i < 14; i++){
            accum += (int) tracking_num.charAt(i);
        }
        accum %= 17;

        accum += 74;
        char chk = (char) accum;
        return (chk == tracking_num.charAt(14));

    }

    public class Package{
        private String account_number;
        private String serial;
        private String serviceID;


        public Package(String acct_no, String serial, String serviceID){
            this.account_number = acct_no;
            this.serial = serial;
            this.serviceID = serviceID;
        }
        public Package(String tracking_num){
            String a = tracking_num.substring(0,5);
            String s = tracking_num.substring(6,7);
            String n = tracking_num.substring(8,13);
            this.account_number = a;
            this.serial = n;
            this.serviceID = s;

        }
        public String getAccount_number(){
            return account_number;
        }
        public String getSerial(){
            return serial;
        }
        public String getServiceID(){
            return serviceID;
        }
        public char getCheckDigit(){
            String trackingnum = account_number + serviceID + serial;
            try {
                trackingnum = findCheckDigit(trackingnum);
            } catch (BadTrackingNumberFormatException e){
                e.printStackTrace();
            }
            return trackingnum.charAt(14);
        }
    }
}