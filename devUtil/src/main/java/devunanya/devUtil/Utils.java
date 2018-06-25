package devunanya.devUtil;

import com.google.appengine.api.urlfetch.*;
import com.google.appengine.repackaged.com.google.api.client.http.HttpStatusCodes;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Logger;


/**
 * Utility Class for simple methods that does simple things
 * @author ihunanyachiamadi
 *
 */
public class Utils {
	private static Logger logger = Logger.getLogger(Utils.class.getSimpleName());
	
	
	/**
	 * This method gets the the json object in a http request body. 
	 * @param req
	 * @return
	 */
    public static JSONObject getJSONObjectFromRequest(HttpServletRequest req){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line;
            BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null){
                stringBuilder.append(line);
            }
            return new JSONObject(stringBuilder.toString());
        }catch (Exception e){//Shit! What just happened here?
//            log.warning("BufferReader failed : " + e.getLocalizedMessage());
            return null;
        }

    }

    /**
     * this method helps to format Nigerian phone number string from local 
     * format to international format
     * @param phone
     * @return
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null){
            return null;
        }
        phone = phone.trim();
        if (phone.startsWith("0")) {
            phone = "234" + phone.substring(1);
        } else if (phone.startsWith("+")) {
            phone = phone.substring(1);
        }

        return phone;
    }
    
    /**
     * This method checks if a string is a valid email format
     * @param email
     * @return
     */
    public static boolean isEmail(String email){
        if(email == null || email.isEmpty())
            return false;
        //Remove blank spaces
        email = email.replaceAll("\\s+", "").trim();
        //Apache email matcher regex
        return email.matches("^\\s*?(.+)@(.+?)\\s*$");
    }


    /**
     * This method formats Nigerian phone number string from
     * international format to local format
     * @param phone
     * @return
     */
    public static String reverseFormatPhoneNumber(String phone) {
        phone = phone.trim();
        if (phone.startsWith("234")) {
            phone = "0" + phone.substring(3);
        } else if (phone.startsWith("+")) {
            phone = phone.substring(1);
        }

        return phone;
    }
    
    /**
     * This method takes a timestamp datetime and formats
     * it to a pretty time like "2 days ago"
     * @param created
     * @return
     */
    public static String formatToPrettyTime(Timestamp created){
        Date date = new Date(created.getTime());
        return new PrettyTime().formatUnrounded(date);
    }

    /**
     * This method checks if a strind is a valid Nigerian 
     * phone number.
     * @param args
     * @return
     */
    public static boolean isPhoneNumber(String args){ //todo: count char(11 or 13), check for first char(0 or 234)
        if (args.isEmpty()){
            return false;
        }
        if (StringUtils.isNumeric(args)){
            return true;
        }
        return false;
    }

    public static boolean isRecaptchad(String recaptcha, String secretKey)
    {
        if (recaptcha.isEmpty() || recaptcha == null)
        {
            return false;
        }
        try
        {
            // Copy the current map to
            Map<String, String> req = new HashMap<>();

            // Set the API command and required fields
            req.put("secret", secretKey);
            req.put("response", recaptcha);
            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            double timeOut = 1000 * 10;
            // Generate the query string
            String post_data = Utils.urlEncodeUTF8(req);
            URL mainUrl = new URL("https://www.google.com/recaptcha/api/siteverify");
            FetchOptions options = FetchOptions.Builder
                    .doNotFollowRedirects()
                    .setDeadline(timeOut);

            HTTPRequest httpRequest = new HTTPRequest(mainUrl, HTTPMethod.POST,options);
            httpRequest.setPayload(post_data.getBytes());
            HTTPResponse httpResponse = urlFetchService.fetch(httpRequest);

            if(httpResponse.getResponseCode() == HttpStatusCodes.STATUS_CODE_OK){
                logger.info("success");
                JSONObject jsonResponse = new JSONObject(new String(httpResponse.getContent()));
                return jsonResponse.getBoolean("success");
            }else {
                int responseCode = httpResponse.getResponseCode();
                String content = new String(httpResponse.getContent());
                logger.info("Response Code :"+responseCode);
                logger.info(content.toString());
            }
        }catch (Exception e)
        {
            logger.warning(e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Builds a HTTP URL from a key-value list
     *
     * @param map
     * @return
     */
    public static String urlEncodeUTF8(Map<?, ?> map)
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s", urlEncodeUTF8(entry.getKey().toString()),
                    urlEncodeUTF8(entry.getValue().toString())));
        }
        return sb.toString();
    }

    /**
     * Encodes a single String to UTF8
     *
     * @param s the string that should be encoded
     * @return
     */
    private static String urlEncodeUTF8(String s)
    {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }
    private static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }
    
    /**
     * This methods helps to generate an MD5HEX string from 
     * String. This method is good for hashing passwords.
     * @param message
     * @return
     */
    public static String md5Hex (String message) {
        try {
            MessageDigest md =
                    MessageDigest.getInstance("MD5");
            return hex (md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }
    
    /**
     * this method helps to generate random strings useful enough for 
     * password resetting
     * @param lenght
     * @return
     */
    public static String generatePassword(int lenght)
    {
        return RandomStringUtils.randomAlphanumeric(lenght);

    }

    /**
     * This method helps to round off numbers
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
