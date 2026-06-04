// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

public final class XmlUtil
{
    public static String getAttr(final Attributes attributes, final String name) throws SAXException {
        final String val = attributes.getValue(name);
        if (val == null) {
            throw new SAXException("missing attribute: " + name);
        }
        return val;
    }
    
    public static String getAttr(final Attributes attributes, final String name, final String defValue) {
        final String val = attributes.getValue(name);
        if (val == null) {
            return defValue;
        }
        return val;
    }
    
    public static boolean getBoolAttr(final Attributes attributes, final String name) throws SAXException {
        final String val = attributes.getValue(name);
        if (val == null) {
            throw new SAXException("missing attribute: " + name);
        }
        return parseBool(val);
    }
    
    public static boolean getBoolAttr(final Attributes attributes, final String name, final boolean defValue) throws SAXException {
        final String val = attributes.getValue(name);
        if (val == null) {
            return defValue;
        }
        return parseBool(val);
    }
    
    public static boolean parseBool(final String str) throws SAXException {
        if (str.equals("true")) {
            return true;
        }
        if (str.equals("false")) {
            return false;
        }
        throw new SAXException("invalid bool value: " + str);
    }
    
    public static int getIntAttr(final Attributes attributes, final String name) throws SAXException {
        final String val = attributes.getValue(name);
        if (val == null) {
            throw new SAXException("missing attribute: " + name);
        }
        return parseInt(val);
    }
    
    public static int getIntAttr(final Attributes attributes, final String name, final int defValue) {
        final String val = attributes.getValue(name);
        if (val == null) {
            return defValue;
        }
        return parseInt(val);
    }
    
    public static int getIntAttr(final Attributes attributes, final String nameA, final String nameB, final int defValue) {
        String val = attributes.getValue(nameA);
        if (val == null) {
            val = attributes.getValue(nameB);
            if (val == null) {
                return defValue;
            }
        }
        return parseInt(val);
    }
    
    public static int parseInt(final String str) {
        if (str.startsWith("#")) {
            return Integer.parseInt(str.substring(1), 16);
        }
        if (str.startsWith("0x")) {
            return Integer.parseInt(str.substring(2), 16);
        }
        return Integer.parseInt(str);
    }
}
