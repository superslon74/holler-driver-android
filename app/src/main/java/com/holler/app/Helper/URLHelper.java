package com.holler.app.Helper;


public class URLHelper {
    public static final String base = "https://api.holler.taxi/";
    //public static final String base = "http://6586b14b.ngrok.io/";
    public static final String HELP_URL = base+"";
    public static final String CALL_PHONE = "1";
    public static final String APP_URL = "https://play.google.com/store/apps/details?id=";
    public static final String login = "/api/provider/oauth/token";
    public static final String register = "/api/provider/register";
    public static final String CHECK_MAIL_ALREADY_REGISTERED = "/api/provider/verify";
    public static final String USER_PROFILE_API = "/api/provider/profile";
    public static final String UPDATE_AVAILABILITY_API = "/api/provider/profile/available";
    public static final String GET_HISTORY_API = "/api/provider/requests/history";
    public static final String GET_HISTORY_DETAILS_API = "/api/provider/requests/history/details";
    public static final String CHANGE_PASSWORD_API = "/api/provider/profile/password";
    public static final String UPCOMING_TRIP_DETAILS = "/api/provider/requests/upcoming/details";
    public static final String UPCOMING_TRIPS = "/api/provider/requests/upcoming";
    public static final String CANCEL_REQUEST_API = "/api/provider/cancel";
    public static final String TARGET_API = "/api/provider/target";
    public static final String RESET_PASSWORD = "/api/provider/reset/password";
    public static final String FORGET_PASSWORD = "/api/provider/forgot/password";
    public static final String FACEBOOK_LOGIN = "/api/provider/auth/facebook";
    public static final String GOOGLE_LOGIN =  "/api/provider/auth/google";
    public static final String LOGOUT = "/api/provider/logout";
    public static final String SUMMARY = "/api/provider/summary";
    public static final String HELP = "/api/provider/help";
    public static final String SEND_REQUEST_API = "/api/user/send/request";
}

