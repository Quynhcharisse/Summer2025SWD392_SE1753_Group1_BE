package com.swd392.group1.pes.enums;

public class Action {
    private static final String BAN = "ban";
    private static final String UNBAN = "unban";


    public static String getNewStatus(String action) {
        if(action != null) {
            if(action.equalsIgnoreCase(BAN)) {
                return Status.ACCOUNT_BAN.getValue(); // deactive
            }
            else if(action.equalsIgnoreCase(UNBAN)) {
                return Status.ACCOUNT_UNBAN.getValue(); // active
            }
        }
        return null;
    }

}
