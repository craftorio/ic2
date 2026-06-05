package ic2.core.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class XmlUtil {
   public static String getAttr(Attributes attributes, String name) throws SAXException {
      String val = attributes.getValue(name);
      if (val == null) {
         throw new SAXException("missing attribute: " + name);
      } else {
         return val;
      }
   }

   public static String getAttr(Attributes attributes, String name, String defValue) {
      String val = attributes.getValue(name);
      return val == null ? defValue : val;
   }

   public static boolean getBoolAttr(Attributes attributes, String name) throws SAXException {
      String val = attributes.getValue(name);
      if (val == null) {
         throw new SAXException("missing attribute: " + name);
      } else {
         return parseBool(val);
      }
   }

   public static boolean getBoolAttr(Attributes attributes, String name, boolean defValue) throws SAXException {
      String val = attributes.getValue(name);
      return val == null ? defValue : parseBool(val);
   }

   public static boolean parseBool(String str) throws SAXException {
      if (str.equals("true")) {
         return true;
      } else if (str.equals("false")) {
         return false;
      } else {
         throw new SAXException("invalid bool value: " + str);
      }
   }

   public static int getIntAttr(Attributes attributes, String name) throws SAXException {
      String val = attributes.getValue(name);
      if (val == null) {
         throw new SAXException("missing attribute: " + name);
      } else {
         return parseInt(val);
      }
   }

   public static int getIntAttr(Attributes attributes, String name, int defValue) {
      String val = attributes.getValue(name);
      return val == null ? defValue : parseInt(val);
   }

   public static int getIntAttr(Attributes attributes, String nameA, String nameB, int defValue) {
      String val = attributes.getValue(nameA);
      if (val == null) {
         val = attributes.getValue(nameB);
         if (val == null) {
            return defValue;
         }
      }

      return parseInt(val);
   }

   public static int parseInt(String str) {
      if (str.startsWith("#")) {
         return Integer.parseInt(str.substring(1), 16);
      } else {
         return str.startsWith("0x") ? Integer.parseInt(str.substring(2), 16) : Integer.parseInt(str);
      }
   }
}
