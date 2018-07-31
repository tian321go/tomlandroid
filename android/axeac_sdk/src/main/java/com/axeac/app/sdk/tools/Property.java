package com.axeac.app.sdk.tools;

import android.util.Log;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

public class Property implements Serializable {

    protected LinkedHashtable properties;
    protected String split = "\r\n";
    private static LinkedHashtable code;

    static {
        code = new LinkedHashtable();
        code.put("\r\n", "<br>");
        code.put(" ", "<spa>");
        code.put("=", "<equ>");
        code.put("\\+", "<add>");
        code.put("\\", "<slash>");
    }

    // describe:Decode
    /**
     * 解码
     * */
    @SuppressWarnings("rawtypes")
    public static String decoding(String data) {
        if (data == null || data.length() == 0)
            return data;
        Vector v = code.linkedKeys();
        for (int i = 0; i < v.size(); i++) {
            data = StringUtil.replaceAll(data, code.get(v.elementAt(i)).toString(), v.elementAt(i).toString());
        }
        return data;
    }

    // describe:Encode
    /**
     * 编码
     * */
    @SuppressWarnings("rawtypes")
    public static String encoding(String data) {
        if (data == null || data.length() == 0)
            return data;
        Vector v = code.linkedKeys();
        for (int i = 0; i < v.size(); i++)
            data = StringUtil.replaceAll(data, v.elementAt(i).toString(), code.get(v.elementAt(i)).toString());
        return data;
    }

    public Property() {
        properties = new LinkedHashtable();
    }

    public Property(byte[] prop) {
        load(prop);
    }

    public Property(InputStream in) {
        load(in);
    }

    public Property(String prop) {
        load(prop);
    }

    public Property(String data, String split) {
        properties = new LinkedHashtable();
        this.split = split;
        load(data);
    }

    @SuppressWarnings("rawtypes")
    public void add(LinkedHashtable table) {
        Vector v = table.linkedKeys();
        for (int i = 0; i < v.size(); i++) {
            String key = (String) v.elementAt(i);
            if (!properties.containsKey(key))
                properties.put(key, table.get(key));
        }
    }

    @SuppressWarnings("rawtypes")
    public void add(Property p) {
        Vector v = p.keys();
        for (int i = 0; i < v.size(); i++) {
            String key = (String) v.elementAt(i);
            if (!properties.containsKey(key))
                properties.put(key, p.getProperty(key));
        }
    }

    @SuppressWarnings("rawtypes")
    public void addAll(LinkedHashtable table) {
        Vector v = table.linkedKeys();
        for (int i = 0; i < v.size(); i++) {
            String key = (String) v.elementAt(i);
            properties.put(key, table.get(key));
        }
    }

    @SuppressWarnings("rawtypes")
    public void addAll(Property p) {
        Vector v = p.keys();
        for (int i = 0; i < v.size(); i++) {
            String key = (String) v.elementAt(i);
            properties.put(key, p.getProperty(key));
        }
    }

    public void clear() {
        properties.clear();
    }

    public boolean getBoolean(String key) {
        String tmp = (String) properties.get(key);
        if (tmp == null)
            return false;
        return tmp.equals("True") || tmp.equals("true") || tmp.equals("1");
    }

    public Date getDate(String key) {
        return getDate(key, "yyyy-MM-dd HH:Mi:ss");
    }

    public Date getDate(String key, String format) {
        String tmp = (String) properties.get(key);
        if (tmp == null)
            return null;
        try {
            return DateFunction.parse(tmp, format);
        } catch (Exception e) {

        }
        return null;
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double def) {
        String tmp = (String) properties.get(key);
        if (tmp == null)
            return def;
        try {
            return Double.parseDouble(tmp);
        } catch (Exception e) {
        }
        return def;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public byte[] getBytes(String key) {
        Object o = properties.get(key);
        if (o == null)
            return null;
        if (o instanceof String)
            return ((String) o).getBytes();
        else if (o instanceof byte[])
            return (byte[]) o;
        else
            return null;
    }

    public int getInt(String key, int def) {
        String tmp = (String) properties.get(key);
        if (tmp == null)
            return def;
        try {
            return Integer.parseInt(tmp);
        } catch (Exception e) {
        }
        return 0;
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public byte[] getStream(String key) {
        String tmp = getProperty(key);
        if (tmp == null)
            return null;
        try {
            return StringUtil.fromBase64(tmp);
        } catch (Exception e) {
            return null;
        }
    }

    public long getLong(String key, long def) {
        String tmp = (String) properties.get(key);
        if (tmp == null)
            return def;
        try {
            return Long.parseLong(tmp);
        } catch (Exception e) {
        }
        return 0;
    }

    public String getProperty(String key) {
        return decoding((String) properties.get(key));
    }

    public String getProperty(String key, String def) {
        String re = decoding((String) properties.get(key));
        if (re == null)
            return decoding(def);
        return re;
    }

    public String getSplit() {
        return split;
    }

    @SuppressWarnings("rawtypes")
    public Vector keys() {
        return properties.linkedKeys();
    }

    public void load(byte[] data) {
        load(new String(data));
    }

    public void load(InputStream in) {
        load(StringUtil.load(in));
    }

    public void load(String config) {
        if (config == null)
            return;
        load(StringUtil.split(config, split));
    }

    public void load(String[] config) {
        if (properties == null) {
            properties = new LinkedHashtable();
        } else
            properties.clear();
        if (config == null || config.length == 0)
            return;
        int idx = 0;
        String lastKey = null;
        for (int i = 0; i < config.length; i++) {
            String s = decoding(StringUtil.toGB2312(config[i].trim()));
            if (s.length() == 0 || s.startsWith("#")) {
                continue;
            }
            idx = s.indexOf("=");
            /*
                Finally there is only one '='
                e.g: RDGFJHJHJHBJHJJBKUGYFUYVJHBJ=

                最后只有一个=
                例：RDGFJHJHJHBJHJJBKUGYFUYVJHBJ=
             */

            if (s.endsWith("=") && !s.startsWith("Form") && s.indexOf(".") == -1) {
                if (lastKey != null) {
                    Object o = properties.get(lastKey);
                    properties.put(lastKey, (o == null ? "" : (String) o) + ("\r\n" + s.trim()));
                }
            }
             /*
                Finally there is '=' and '.'
                e.g: OIUOIUOIUOIUC1.text=

                最后有=也有.
                例：OIUOIUOIUOIUC1.text=
             */
            else if (s.endsWith("=") && !s.startsWith("Form") && s.indexOf(".") != -1) {
                lastKey = s.substring(0, idx).trim();
                properties.put(lastKey, s.substring(idx + 1).trim());
                continue;
            }
            /*
                Finally there is '=' and '.'but also from the beginning
                e.g: From.icon.stream=HKJLK=

                最后有=也是from打头并且有.
                例：From.icon.stream=HKJLK=
             */
            else if (s.endsWith("=") && s.startsWith("Form") && idx != -1) {
                lastKey = s.substring(0, idx).trim();
                properties.put(lastKey, s.substring(idx + 1).trim());
                continue;
            }
             /*
                '=' in the middle
                e.g: GJHJHKJHKJC2.text = 测试

                =号在中间
                例：GJHJHKJHKJC2.text = 测试
             */
            else if (!s.endsWith("=") && !s.startsWith("Form") && idx != -1) {
                String tempKey = s.substring(0, idx).trim();
                if (tempKey.contains("<p>")||tempKey.contains("<p")||tempKey.contains("<center>")||tempKey.contains("</p>")
                        ||tempKey.contains("<span>")||tempKey.contains("<span")||tempKey.contains("<center>")||tempKey.contains("</span>")
                        ||tempKey.contains("<div>")||tempKey.contains("<div")||tempKey.contains("<center>")||tempKey.contains("</div>")
                        ||tempKey.contains("<img>")||tempKey.contains("<img")||tempKey.contains("<center>")||tempKey.contains("</img>")) {
                    if (lastKey != null) {
                        Object o = properties.get(lastKey);
                        properties.put(lastKey, (o == null ? "" : (String) o) + ("\r\n" + s.trim()));
                        continue;
                    }
                }

                lastKey = s.substring(0, idx).trim();
                properties.put(lastKey, s.substring(idx + 1).trim());
                continue;
            }
            // Wang has been approved 11.16
            //王总已经审批 11.16
            else if (!s.endsWith("=") && !s.startsWith("Form")) {
                if (lastKey != null) {
                    Object o = properties.get(lastKey);
                    properties.put(lastKey, (o == null ? "" : (String) o) + ("\r\n" + s.trim()));
                    continue;
                }
            }else{
                lastKey = s.substring(0, idx).trim();
                properties.put(lastKey, s.substring(idx + 1).trim());
            }
        }
    }

    public void removeKey(String key) {
        properties.remove(key);
    }

    @SuppressWarnings("rawtypes")
    public LinkedHashtable search(String key) {
        LinkedHashtable re = new LinkedHashtable();
        Vector v = keys();
        for (int i = 0; i < v.size(); i++) {
            String tmp = (String) v.elementAt(i);
            if (tmp.startsWith(key))
                re.put(tmp, getProperty(tmp));
        }
        return re;
    }

    @SuppressWarnings("rawtypes")
    public LinkedHashtable searchOne(String key) {
        LinkedHashtable re = new LinkedHashtable();
        Vector v = keys();
        for (int i = 0; i < v.size(); i++) {
            String tmp = (String) v.elementAt(i);
            if (tmp.equals(key))
                re.put(tmp, getProperty(tmp));
        }
        return re;
    }

    public LinkedHashtable searchDeep(String key, int deep) {
        return searchDeep(key, deep, ".");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public LinkedHashtable searchDeep(String key, int deep, String split) {
        LinkedHashtable re = search(key);
        Vector v = re.linkedKeys();
        Vector remove = new Vector();
        for (int i = 0; i < v.size(); i++) {
            String tmp = (String) v.elementAt(i);
            if (tmp.length() == key.length()) {
                continue;
            }
            String[] ar = StringUtil.split(tmp.substring(key.length()), split);
            if (ar.length != deep) {
                remove.addElement(tmp);
            }
        }
        for (int i = 0; i < remove.size(); i++) {
            re.remove(remove.elementAt(i));
        }
        return re;
    }

    @SuppressWarnings("rawtypes")
    public LinkedHashtable searchSubKey(String key) {
        LinkedHashtable re = new LinkedHashtable();
        Vector v = keys();
        System.out.print("");
        for (int i = 0; i < v.size(); i++) {
            String tmp = (String) v.elementAt(i);
            if (tmp.startsWith(key))
                re.put(tmp.substring(key.length()), getProperty(tmp));
        }
        return re;
    }

    @SuppressWarnings("rawtypes")
    public LinkedHashtable searchKey(String key[]) {
        LinkedHashtable re = new LinkedHashtable();
        Vector v = keys();
        for (int i = 0; i < v.size(); i++) {
            String tmp = (String) v.elementAt(i);
            for (int j = 0; j < key.length; j++) {
                if (tmp.startsWith(key[j]))
                    re.put(tmp, getProperty(tmp));
            }
        }
        return re;
    }

    public Property split(String key) {
        Property p = new Property();
        LinkedHashtable lh = search(key);
        p.add(lh);
        return p;
    }

    public Property splitSubKey(String key) {
        Property p = new Property();
        LinkedHashtable lh = searchSubKey(key);
        p.add(lh);
        return p;
    }

    public LinkedHashtable searchSubKeyDeep(String key, int deep) {
        return searchSubKeyDeep(key, deep, ".");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public LinkedHashtable searchSubKeyDeep(String key, int deep, String split) {
        LinkedHashtable re = searchSubKey(key);
        Property pp = new Property();
        pp.add(re);
        Vector v = re.linkedKeys();
        Vector remove = new Vector();
        for (int i = 0; i < v.size(); i++) {
            String tmp = (String) v.elementAt(i);
            if (key.equals(tmp)) {
                continue;
            }
            String[] ar = StringUtil.split(tmp, split);
            if (ar.length != deep) {
                remove.addElement(tmp);
            }
        }
        for (int i = 0; i < remove.size(); i++) {
            re.remove(remove.elementAt(i));
        }
        return re;
    }

    public void setProperite(String key, String value) {
        properties.put(key, decoding(value));
    }

    public void setProperite(String key, byte[] value) {
        if (value == null)
            return;
        String[] t = StringUtil.split(new String(value), "\r\n");
        String r = StringUtil.toBase64(value, t[0].length(), 100 - t[t.length - 1].length());
        properties.put(key, decoding(r));
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public LinkedHashtable toHashtable() {
        return properties;
    }

    @SuppressWarnings("rawtypes")
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Vector v = keys();
        for (int i = 0; i < v.size(); i++) {
            String key = (String) v.elementAt(i);
            sb.append(key);
            sb.append(" = ");
            sb.append(decoding(getProperty(key)));
            sb.append(split);
        }
        return new String(sb);
    }
}