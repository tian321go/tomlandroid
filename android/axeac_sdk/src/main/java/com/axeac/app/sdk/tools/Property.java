package com.axeac.app.sdk.tools;

import android.util.Log;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class Property implements Serializable {

    protected LinkedHashtable properties;
    protected String split = "\r\n";
    private static LinkedHashtable code;
    private HashMap<String,String> map;

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

    public void loadToml(String[] config) {
        if (properties == null) {
            properties = new LinkedHashtable();
        } else
            properties.clear();
        if (config == null || config.length == 0)
            return;
        int idx = 0;
        String lastKey = null;
        String var = "var.";
        String key = null;
        String value = null;
        String isTab = null;
        List<String> formAddList = new ArrayList<>();
        List<HashMap<String, String>> containerAddList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> tabIndex = new HashMap<String,String>();
        HashMap<String, String> addUI = new HashMap<String,String>();
        for (int i = 0; i < config.length; i++) {
            String s = decoding(StringUtil.toGB2312(config[i].trim()));
            if (s.length() == 0 || s.startsWith("#")){
                continue;
            }
            idx = s.indexOf("=");

            //解析[Form]标签
            if(s.startsWith("[Form")&&s.indexOf(".")==-1) {
                lastKey = "Form";
                properties.put(lastKey, lastKey);
                properties.put(lastKey + "." + "id", s.substring(s.indexOf("[") + 1,s.indexOf("]")));
                continue;
            }
            //解析不是 [Form 开头的控件且存在=但不存在 . 属性
            else if(!s.startsWith("[Form.")&&idx!=-1&&s.indexOf(".")==-1){
                key = s.substring(0, idx).trim();

                if (key.contains("<p>")||key.contains("<p")||key.contains("<center>")||key.contains("</p>")
                        ||key.contains("<span>")||key.contains("<span")||key.contains("<center>")||key.contains("</span>")
                        ||key.contains("<div>")||key.contains("<div")||key.contains("<center>")||key.contains("</div>")
                        ||key.contains("<img>")||key.contains("<img")||key.contains("<center>")||key.contains("</img>")) {
                    if (lastKey != null&&key != null) {
                        String keys = lastKey + "." + key;
                        Object o = properties.get(keys);
                        properties.put(keys, (o == null ? "" : (String) o) + ("\r\n" + s.trim()));
                        continue;
                    }
                }

                if(s.indexOf("\"")!=-1) {
                    value = s.substring(s.indexOf("\"") + 1,s.lastIndexOf("\"")).trim();
                }else {
                    value = s.substring(idx+1).trim();
                }

                String add = "false";
                if(key.equals("add")) {
                    add = value;
                    addUI.put(lastKey, add);
                    continue;
                }
                if(isTab!=null&&isTab.startsWith("TabContainer")&&key.equals("index")) {
                    tabIndex.put(key, value);
                    isTab = null;
                }else {
                    properties.put(lastKey + "." + key, value);
                }
                continue;
            }
            //解析 [Form. 开头且只有一个.的标签
            else if(s.startsWith("[Form.")&&s.indexOf(".")==s.lastIndexOf(".")&&s.indexOf(".")!=-1) {
                lastKey = s.substring(s.indexOf(".") + 1,s.indexOf("]")).trim();
                value = lastKey.substring(0,lastKey.indexOf("_"));
                formAddList.add(lastKey);
                properties.put(var + lastKey, value);
                continue;
            }
            //解析 [Form. 开头且有多个.的标签，此时标签为容器控件
            else if(s.startsWith("[Form.")&&s.indexOf(".")!=-1&&s.indexOf(".")!=s.lastIndexOf(".")) {
                String s1 = s.substring(s.indexOf("[Form.")+1,s.indexOf("]"));
                String [] values = s1.split("\\.");
                value = s1.substring(s1.lastIndexOf(".")+1,s1.length()).trim();
                HashMap<String,String> map = new HashMap<>();
                map.put(values[values.length-2], value);
                properties.put(var + values[values.length-1], value.substring(0, value.lastIndexOf("_")));
                containerAddList.add(map);
                lastKey = value;
                isTab = s1;
                continue;
            }
            //不以[From.开头且没有=
            else if (!s.startsWith("[Form.")&&idx==-1) {
                if (lastKey != null&&key != null) {
                    String keys = lastKey + "." + key;
                    Object o = properties.get(keys);
                    properties.put(keys, (o == null ? "" : (String) o) + ("\r\n" + s.trim()));
                    continue;
                }
            }
            else {
                if(s.indexOf("=")!=-1) {
                    key = s.substring(0, idx).trim();
                    value = s.substring(idx+1).trim();
                }else {
                    key = s.trim();
                    value = "";
                }
                properties.put(key, value);
            }

        }
        //调整容器中组件添加顺序
        for(int i=0;i<containerAddList.size()-1;i++) {
            for(int j=0;j<containerAddList.size()-1-i;j++) {
                String value1 = "";
                String value2 = "";
                for(String mapKey:containerAddList.get(j).keySet()) {
                    value1 = containerAddList.get(j).get(mapKey);
                }
                for(String mapKey:containerAddList.get(j+1).keySet()) {
                    value2 = mapKey;
                }
                if(!"".equals(value1)&&!"".equals(value2)) {
                    if(value1.equals(value2)) {
                        map = containerAddList.get(j);
                        containerAddList.set(j, containerAddList.get(j+1));
                        containerAddList.set(j+1, map);
                    }
                }
            }
        }

        //添加组件到容器
        for(int i=0;i<containerAddList.size();i++) {
            for(String mapKey:containerAddList.get(i).keySet()) {
                if(mapKey.startsWith("TabContainer")) {
                    if(addUI.containsKey(containerAddList.get(i).get(mapKey))) {
                        String index = tabIndex.containsKey(containerAddList.get(i).get(mapKey))
                                ?tabIndex.get(containerAddList.get(i).get(mapKey)):"1";
                        properties.put(mapKey + ".add." + containerAddList.get(i).get(mapKey),
                                addUI.get(containerAddList.get(i).get(mapKey)) + "," + index);
                    }else {
                        String index = tabIndex.containsKey(containerAddList.get(i).get(mapKey))
                                ?tabIndex.get(containerAddList.get(i).get(mapKey)):"1";
                        properties.put(mapKey + ".add." + containerAddList.get(i).get(mapKey),
                                "false," + index);
                    }
                }else {
                    if(addUI.containsKey(containerAddList.get(i).get(mapKey))) {
                        properties.put(mapKey + ".add." + containerAddList.get(i).get(mapKey),
                                addUI.get(containerAddList.get(i).get(mapKey)));
                    }else {
                        properties.put(mapKey + ".add." + containerAddList.get(i).get(mapKey), "false");
                    }
                }
            }
        }
        //添加组件到Form
        for(int i=0;i<formAddList.size();i++) {
            if(addUI.containsKey(formAddList.get(i))) {
                properties.put("Form.add." + formAddList.get(i), addUI.get(formAddList.get(i)));
            }else {
                properties.put("Form.add." + formAddList.get(i), "false");
            }
        }
    }

    public void loadToml1(String[] config) {
        if (properties == null) {
            properties = new LinkedHashtable();
        } else
            properties.clear();
        if (config == null || config.length == 0)
            return;
        int idx = 0;
        String lastKey = null;
        String var = "var.";
        String key = null;
        String value = null;
        String isTab = null;
        List<String> formAddList = new ArrayList<>();
        List<HashMap<String, String>> containerAddList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> tabIndex = new HashMap<String,String>();
        HashMap<String, String> addUI = new HashMap<String,String>();
        for (int i = 0; i < config.length; i++) {
            String s = decoding(StringUtil.toGB2312(config[i].trim()));
            if (s.length() == 0 || s.startsWith("#")){
                continue;
            }
            idx = s.indexOf("=");

            //解析[Form]标签
            if(s.startsWith("[Form")&&s.indexOf(".")==-1) {
                lastKey = "Form";
                properties.put(lastKey, lastKey);
                properties.put(lastKey + "." + "id", s.substring(s.indexOf("[") + 1,s.indexOf("]")));
                continue;
            }
            //解析不是 [Form 开头的控件且存在=但不存在 . 属性
            else if(!s.startsWith("[")&&idx!=-1){
                key = s.substring(0, idx).trim();
                if(key.indexOf(".")==-1){
                    if (key.contains("<p>")||key.contains("<p")||key.contains("<center>")||key.contains("</p>")
                            ||key.contains("<span>")||key.contains("<span")||key.contains("<center>")||key.contains("</span>")
                            ||key.contains("<div>")||key.contains("<div")||key.contains("<center>")||key.contains("</div>")
                            ||key.contains("<img>")||key.contains("<img")||key.contains("<center>")||key.contains("</img>")) {
                        if (lastKey != null&&key != null) {
                            String keys = lastKey + "." + key;
                            Object o = properties.get(keys);
                            properties.put(keys, (o == null ? "" : (String) o) + ("\r\n" + s.trim()));
                            continue;
                        }
                    }

                    if(s.indexOf("\"")!=-1) {
                        value = s.substring(s.indexOf("\"") + 1,s.lastIndexOf("\"")).trim();
                    }else {
                        value = s.substring(idx+1).trim();
                    }

                    if(lastKey==null){
                        properties.put(key,value);
                    }

                    String add = "false";
                    if(key.equals("add")) {
                        add = value;
                        addUI.put(lastKey, add);
                        continue;
                    }
                    if(isTab!=null&&isTab.startsWith("TabContainer")&&key.equals("index")) {
                        tabIndex.put(lastKey, value);
                        isTab = null;
                    }else {
                        properties.put(lastKey + "." + key, value);
                    }

                }else{
                    if(s.indexOf("=")!=-1) {
                        key = s.substring(0, idx).trim();
                        value = s.substring(idx+1).trim();
                    }else {
                        key = s.trim();
                        value = "";
                    }
                    properties.put(key, value);
                }
                continue;
            }
            //解析 [Form. 开头且只有一个.的标签
            else if(s.startsWith("[Form.")&&s.indexOf(".")!=-1) {
                lastKey = s.substring(s.indexOf(".") + 1,s.indexOf("]")).trim();
                value = lastKey.substring(0,lastKey.indexOf("_"));
                formAddList.add(lastKey);
                properties.put(var + lastKey, value);
                continue;
            }
            //解析 [Form. 开头且有多个.的标签，此时标签为容器控件
            else if(s.startsWith("[")&&s.indexOf(".")!=-1) {
                String s1 = s.substring(s.indexOf("[")+1,s.indexOf("]"));
                String [] values = s1.split("\\.");
                value = s1.substring(s1.lastIndexOf(".")+1,s1.length()).trim();
                HashMap<String,String> map = new HashMap<>();
                map.put(values[values.length-2], value);
                properties.put(var + values[values.length-1], value.substring(0, value.lastIndexOf("_")));
                containerAddList.add(map);
                lastKey = value;
                isTab = s1;
                continue;
            }
            //不以[From.开头且没有=
            else if (!s.startsWith("[")&&idx==-1) {
                if (lastKey != null&&key != null) {
                    String keys = lastKey + "." + key;
                    Object o = properties.get(keys);
                    properties.put(keys, (o == null ? "" : (String) o) + ("\r\n" + s.trim()));
                    continue;
                }
            }
            else {
                if(s.indexOf("=")!=-1) {
                    key = s.substring(0, idx).trim();
                    value = s.substring(idx+1).trim();
                }else {
                    key = s.trim();
                    value = "";
                }
                properties.put(key, value);
            }

        }
        //调整容器中组件添加顺序
        for(int i=0;i<containerAddList.size()-1;i++) {
            for(int j=0;j<containerAddList.size()-1-i;j++) {
                String value1 = "";
                String value2 = "";
                for(String mapKey:containerAddList.get(j).keySet()) {
                    value1 = containerAddList.get(j).get(mapKey);
                }
                for(String mapKey:containerAddList.get(j+1).keySet()) {
                    value2 = mapKey;
                }
                if(!"".equals(value1)&&!"".equals(value2)) {
                    if(value1.equals(value2)) {
                        map = containerAddList.get(j);
                        containerAddList.set(j, containerAddList.get(j+1));
                        containerAddList.set(j+1, map);
                    }
                }
            }
        }

        //添加组件到容器
        for(int i=0;i<containerAddList.size();i++) {
            for(String mapKey:containerAddList.get(i).keySet()) {
                if(mapKey.startsWith("TabContainer")) {
                    if(addUI.containsKey(containerAddList.get(i).get(mapKey))) {
                        String index = tabIndex.containsKey(containerAddList.get(i).get(mapKey))
                                ?tabIndex.get(containerAddList.get(i).get(mapKey)):"1";
                        properties.put(mapKey + ".add." + containerAddList.get(i).get(mapKey),
                                addUI.get(containerAddList.get(i).get(mapKey)) + "," + index);
                    }else {
                        String index = tabIndex.containsKey(containerAddList.get(i).get(mapKey))
                                ?tabIndex.get(containerAddList.get(i).get(mapKey)):"1";
                        properties.put(mapKey + ".add." + containerAddList.get(i).get(mapKey),
                                "false," + index);
                    }
                }else {
                    if(addUI.containsKey(containerAddList.get(i).get(mapKey))) {
                        properties.put(mapKey + ".add." + containerAddList.get(i).get(mapKey),
                                addUI.get(containerAddList.get(i).get(mapKey)));
                    }else {
                        properties.put(mapKey + ".add." + containerAddList.get(i).get(mapKey), "false");
                    }
                }
            }
        }
        //添加组件到Form
        for(int i=0;i<formAddList.size();i++) {
            if(addUI.containsKey(formAddList.get(i))) {
                properties.put("Form.add." + formAddList.get(i), addUI.get(formAddList.get(i)));
            }else {
                properties.put("Form.add." + formAddList.get(i), "false");
            }
        }
    }

    public void load(String config) {
        if (config == null)
            return;
        loadToml1(StringUtil.split(config, split));
//        load(StringUtil.split(config, split));
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