package com.mentalsnapp.com.mentalsnapp.utils;

import java.util.regex.Pattern;

public class EmailValidator {
    public static Pattern EMAIL_ADDRESS_PATTERN = Pattern
            .compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
                    + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
                    + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

    public static boolean checkEmail(String email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    public static boolean hasText(String text) {
        if (text.length() > 0)
            return true;
        return false;
    }
}