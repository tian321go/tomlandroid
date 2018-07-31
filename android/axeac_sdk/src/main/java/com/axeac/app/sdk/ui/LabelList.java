package com.axeac.app.sdk.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.axeac.app.sdk.R;
import com.axeac.app.sdk.adapters.ChooseGridAdapter;
import com.axeac.app.sdk.analysis.ComponentAnalysis;
import com.axeac.app.sdk.dialog.CustomDialog;
import com.axeac.app.sdk.jhsp.JHSPResponse;
import com.axeac.app.sdk.retrofit.OnRequestCallBack;
import com.axeac.app.sdk.retrofit.UIHelper;
import com.axeac.app.sdk.tools.LinkedHashtable;
import com.axeac.app.sdk.tools.Property;
import com.axeac.app.sdk.tools.StringUtil;
import com.axeac.app.sdk.ui.base.Component;
import com.axeac.app.sdk.ui.base.LabelComponent;
import com.axeac.app.sdk.utils.CommonUtil;
import com.axeac.app.sdk.utils.DensityUtil;
import com.axeac.app.sdk.utils.StaticObject;

/**
 * describe:List radio or multiple choice
 * 列表单选或多选
 * @author axeac
 * @version 1.0.0
 */
public class LabelList extends LabelComponent {

    private LinearLayout valLayout;
    private TextView textField;

    /**
     * 存储列表项数据id的list集合
     * */
    private List<String> ids = new ArrayList<String>();
    /**
     * 存储列表项数据id对应值的list集合
     * */
    private List<String> values = new ArrayList<String>();

    /**
     * 存储列表项数据选中项id的list集合
     * */
    private List<String> selectedIds = new ArrayList<String>();

    /**
     * 默认显示文本
     * <br>默认值为空
     * */
    private String text = "";
    /**
     * 列表最大选择数
     * <br>默认值为1，即单选
     * */
    private int max = 1;
    /**
     * 设置显示类型，List\Dialog\个数，当类型为个数时，代表超过这个个数，
     * <br>以Dialog显示，并附带搜索框。不超过则使用list形式。非Dialog形式时，
     * <br>所有选项都采用以下形式：每个长度都是相同，并且根据显示区域的大小，
     * <br>自动等分，规则从每行两个内容开始，如果大小满足所有文本长度，
     * <br>则进行每行3个测试，不满足，就每行显示一个。
     * <br>默认值为List
     * */
    private String type = "List";
    /**
     * 设置选择模式
     * 默认值为1
     * */
    private int style = 1;
    /**
     * COMPONENT:PAGEID::org=@labellist1,jmis_pkId="+pkId+",jmis_yj="+idea+",opparam1="+opParam1+"
     * <br>默认值为空
     * */
    private String onchange = "";

    // Increase the id of the labelText
    /**
     * 增加影响的labelText的id
     * <br>默认值为空
     * */
    private String changeui = "";

    // Read-only attribute, return format ID, text || ID, text
    /**
     * 只读属性，返回格式为ID,文本||ID,文本
     * <br>默认值为空
     **/
    private String items = "";

    // Read-only attribute, returns the total number of current list items
    /**
     * 只读属性，返回当前列表项总个数
     * <br>默认值为0
     **/
    private int count = 0;

    // Returns the index of the radio and multi-select, in the format 1, 2
    /**
     * 返回单选和多选时的索引，格式为1,2
     * <br>默认值为-1
     * */
    private String selectedIndex = "-1";

    // Returns the ID of the radio and multi-selection, in the format ID, ID
    /**
     * 返回单选和多选时的ID，格式为ID,ID
     * <br>默认值为空
     * */
    private String selectedValue = "";

    /**
     * 加载线程Thread对象
     * */
    private Thread loadingThread;
    /**
     * 加载提示框ProgressDialog对象
     * */
    private ProgressDialog loadingDialog;
    /**
     * ComponentAnalysis对象
     * */
    private ComponentAnalysis analysis;

    public LabelList(Activity ctx) {
        super(ctx);
        this.returnable = true;
        valLayout = new LinearLayout(ctx);
        valLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        valLayout.setOrientation(LinearLayout.VERTICAL);
        this.view = valLayout;
        initLoadingDialog();
    }

    /**
     * 初始化加载提示框
     * */
    private void initLoadingDialog() {
        loadingDialog = new ProgressDialog(ctx);
        loadingDialog.setIndeterminate(true);
        loadingDialog.setMessage(ctx.getString(R.string.axeac_login_loading));
        loadingDialog.setCancelable(true);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (loadingThread != null && !loadingThread.isInterrupted()) {
                    loadingThread.interrupt();
                }
            }
        });
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    /**
     * 设置显示文本
     * @param text
     * 显示文本
     * */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * 设置列表最大选择数
     * @param max
     * 最大选择数
     * */
    public void setMax(String max) {
        this.max = Integer.parseInt(max);
    }

    /**
     * 设置显示类型
     * @param type
     * List/Dialog/个数
     * */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 设置选择模式
     * @param style
     * 可选值 1、2
     * */
    public void setStyle(String style) {
        this.style = Integer.parseInt(style);
    }

    /**
     * 设置改变后的数据
     * @param onchange
     * 改变后的数据
     * */
    public void setOnChange(String onchange) {
        this.onchange = onchange;
    }

    /**
     * 为列表项添加数据
     * @param data
     * 列表项数据
     * */
    public void addData(String data) { // ID||名称 //ID||name
        try {
            if (data == null || "".equals(data))
                return;
            String[] item = StringUtil.split(data, "||");
            if (item.length >= 2) {
                ids.add(item[0]);
                values.add(item[1]);
            }
        } catch (Throwable e) {
            String clsName = this.getClass().getName();
            clsName = clsName.substring(clsName.lastIndexOf(".") + 1);
            String info = ctx.getString(R.string.axeac_toast_exp_funexp);
            info = StringUtil.replace(info, "@@TT@@", "addData");
            Toast.makeText(ctx, clsName + info, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置默认选中value（多选）
     * @param selected
     * 默认选中value
     * */
    public void addSelected(String selected) {
        String[] sels = StringUtil.split(selected, ",");
        for (String s : sels) {
            selectedIds.add(s);
        }
    }

    /**
     * 初始化ComponentAnalysis对象
     * @param analysis
     * ComponentAnalysis对象
     * */
    public void setAnalysis(ComponentAnalysis analysis) {
        this.analysis = analysis;
    }

    /**
     * 获取ComponentAnalysis对象
     * @return
     * ComponentAnalysis对象
     * */
    public ComponentAnalysis getAnalysis() {
        return this.analysis;
    }

    /**
     * 执行方法
     * */
    @Override
    public void execute() {
        if (!this.visiable) return;
        try {
            count = ids.size();
            if (count > 0) {
                if (type.trim().toLowerCase().equals("list")) {
                    showList();
                } else if (type.trim().toLowerCase().equals("dialog")) {
                    showDialog(false);
                } else if (CommonUtil.isNumeric(type.trim())) {
                    if (Integer.parseInt(type.trim()) < count) {
                        showDialog(true);
                    } else {
                        showList();
                    }
                }
            }
        } catch (Throwable e) {
            String clsName = this.getClass().getName();
            clsName = clsName.substring(clsName.lastIndexOf(".") + 1);
            String info = ctx.getString(R.string.axeac_toast_exp_create);
            info = StringUtil.replace(info, "@@TT@@", clsName);
            Toast.makeText(ctx, info, Toast.LENGTH_SHORT).show();
        }
        if (textField != null) {
            String familyName = null;
            int style = Typeface.NORMAL;
            if (this.font != null && !"".equals(this.font)) {
                if (this.font.indexOf(";") != -1) {
                    String[] strs = this.font.split(";");
                    for (String str : strs) {
                        if (str.startsWith("size")) {
                            int index = str.indexOf(":");
                            if (index == -1)
                                continue;
                            String s = str.substring(index + 1).trim();
                            textField.setTextSize(Float.parseFloat(s.replace("px", "").trim()));
                        } else if (str.startsWith("family")) {
                            int index = str.indexOf(":");
                            if (index == -1)
                                continue;
                            familyName = str.substring(index + 1).trim();
                        } else if (str.startsWith("style")) {
                            int index = str.indexOf(":");
                            if (index == -1)
                                continue;
                            String s = str.substring(index + 1).trim();
                            if ("bold".equals(s)) {
                                style = Typeface.BOLD;
                            } else if ("italic".equals(s)) {
                                style = Typeface.ITALIC;
                            } else {
                                if (s.indexOf(",") != -1) {
                                    if ("bold".equals(s.split(",")[0]) && "italic".equals(s.split(",")[1])) {
                                        style = Typeface.BOLD_ITALIC;
                                    }
                                    if ("bold".equals(s.split(",")[1]) && "italic".equals(s.split(",")[0])) {
                                        style = Typeface.BOLD_ITALIC;
                                    }
                                }
                            }
                        } else if (str.startsWith("color")) {
                            int index = str.indexOf(":");
                            if (index == -1)
                                continue;
                            String s = str.substring(index + 1).trim();
                            if (CommonUtil.validRGBColor(s)) {
                                int r = Integer.parseInt(s.substring(0, 3));
                                int g = Integer.parseInt(s.substring(3, 6));
                                int b = Integer.parseInt(s.substring(6, 9));
                                textField.setTextColor(Color.rgb(r, g, b));
                            }
                        }
                    }
                }
            }
            if (familyName == null || "".equals(familyName)) {
                textField.setTypeface(Typeface.defaultFromStyle(style));
            } else {
                textField.setTypeface(Typeface.create(familyName, style));
            }
        }
    }

    /**
     * 显示列表项
     * */
    private void showList() {
        try {
            boolean[] selectedItems = new boolean[count];
            if (max == 1) {
                String textIndex = "";
                for (int i = 0; i < values.size(); i++) {
                    if (text.equals(values.get(i))) {
                        textIndex = ids.get(i);
                    }
                }
                if (!"".equals(textIndex)) {
                    selectedIds.clear();
                    selectedIds.add(textIndex);
                }
                if (selectedIds.size() == 0) {
                } else {
                    String id = selectedIds.get(0);
                    selectedIds.clear();
                    selectedIds.add(id);
                }
            }
            List<String> sids = selectedIds;
            selectedIds = new ArrayList<String>();
            for (int i = 0; i < sids.size(); i++) {
                if (ids.contains(sids.get(i))) {
                    selectedIds.add(sids.get(i));
                }
            }
            if (selectedIds.size() > max) {
                for (int i = max; i < selectedIds.size(); i++) {
                    selectedIds.remove(i);
                }
            }
            for (int i = 0; i < count; i++) {
                if (selectedIds.contains(ids.get(i))) {
                    selectedItems[i] = true;
                } else {
                    selectedItems[i] = false;
                }
            }
            if (style != 2) {
                GridView view = new GridView(ctx);
                int totalHeight = 0;
                int line = count;
                for (int i = 0; i < line; i++) {
                    totalHeight += DensityUtil.dip2px(ctx, 35);
                }
                view.setLayoutParams(new GridView.LayoutParams(
                        GridView.LayoutParams.FILL_PARENT, totalHeight + 5 * (line - 1)));
                view.setHorizontalSpacing(5);
                view.setVerticalSpacing(5);
                view.setNumColumns(1);
                view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                view.setSelector(android.R.color.transparent);
                ChooseGridAdapter adapter = new ChooseGridAdapter(ctx, this.values, selectedItems, font);
                view.setAdapter(adapter);
                valLayout.addView(view);
                returnVals(selectedItems);
                if (!readOnly) {
                    view.setOnItemClickListener(mGridOnItemClickListener(adapter));
                }
            } else {
                List<LinearLayout> layoutList = new ArrayList<LinearLayout>();
                for (int i = 0; i < count; i++) {
                    LinearLayout layout = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.axeac_label_choice_2, null);
                    ImageView img = (ImageView) layout.findViewById(R.id.label_choice_check_2);
                    TextView text = (TextView) layout.findViewById(R.id.label_choice_text_2);
                    if (max == 1) {
                        if (selectedItems[i]) {
                            img.setImageResource(R.drawable.axeac_label_singlechoose_enable);
                        } else {
                            img.setImageResource(R.drawable.axeac_label_singlechoose_disable);
                        }
                    } else {
                        if (selectedItems[i]) {
                            img.setImageResource(R.drawable.axeac_label_mutichoose_enable);
                        } else {
                            img.setImageResource(R.drawable.axeac_label_mutichoose_disable);
                        }
                    }
                    text.setText(this.values.get(i));
                    layout.setTag(R.string.axeac_key0, selectedItems[i]);
                    layout.setTag(R.string.axeac_key1, img);
                    layoutList.add(layout);
                    valLayout.addView(layout);
                }
                returnVals(selectedItems);
                for (int i = 0; i < count; i++) {
                    if (!readOnly) {
                        layoutList.get(i).setOnClickListener(mLayoutOnItemClickListener(i, layoutList));
                    }
                }
            }
        } catch (Throwable e) {
            String clsName = this.getClass().getName();
            clsName = clsName.substring(clsName.lastIndexOf(".") + 1);
            String info = ctx.getString(R.string.axeac_toast_exp_create);
            info = StringUtil.replace(info, "@@TT@@", clsName);
            Toast.makeText(ctx, info, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 列表项item监听
     * @param adapter
     * ChooseGridAdapter对象
     * */
    private AdapterView.OnItemClickListener mGridOnItemClickListener(final ChooseGridAdapter adapter) {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (adapter.getIsCheckSelected().get(position)) {
                    adapter.getItemViewMap().get(position).text.setBackgroundResource(R.drawable.axeac_label_choice_disable);
                    adapter.getItemViewMap().get(position).img.setVisibility(View.INVISIBLE);
                    adapter.getIsCheckSelected().put(position, false);
                } else {
                    if (max == 1) {
                        for (int i = 0; i < count; i++) {
                            if (i == position) {
                                adapter.getItemViewMap().get(position).text.setBackgroundResource(R.drawable.axeac_label_choice_enable);
                                adapter.getItemViewMap().get(position).img.setVisibility(View.VISIBLE);
                                adapter.getIsCheckSelected().put(position, true);
                            } else {
                                adapter.getItemViewMap().get(i).text.setBackgroundResource(R.drawable.axeac_label_choice_disable);
                                adapter.getItemViewMap().get(i).img.setVisibility(View.INVISIBLE);
                                adapter.getIsCheckSelected().put(i, false);
                            }
                        }
                    } else {
                        int c = 0;
                        for (int k = 0; k < count; k++) {
                            if (adapter.getIsCheckSelected().get(k)) {
                                c += 1;
                            }
                        }
                        if (c < max) {
                            adapter.getItemViewMap().get(position).text.setBackgroundResource(R.drawable.axeac_label_choice_enable);
                            adapter.getItemViewMap().get(position).img.setVisibility(View.VISIBLE);
                            adapter.getIsCheckSelected().put(position, true);
                        } else {
                            Toast.makeText(ctx, R.string.axeac_msg_choice_ex, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                boolean[] selectedItems = new boolean[count];
                for (int i = 0; i < selectedItems.length; i++) {
                    selectedItems[i] = adapter.getIsCheckSelected().get(i);
                }
                returnVals(selectedItems);
                adapter.notifyDataSetChanged();
                executeOnChange(selectedItems[position], position);
            }
        };
    }

    /**
     * 列表项监听
     * @param pos
     * 位置
     * @param layoutList
     * 存储LinearLayout对象的list集合
     * */
    private View.OnClickListener mLayoutOnItemClickListener(final int pos, final List<LinearLayout> layoutList) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = (Boolean) layoutList.get(pos).getTag(R.string.axeac_key0);
                ImageView img = (ImageView) layoutList.get(pos).getTag(R.string.axeac_key1);
                if (flag) {
                    layoutList.get(pos).setTag(R.string.axeac_key0, false);
                    if (max == 1) {
                        img.setImageResource(R.drawable.axeac_label_singlechoose_disable);
                    } else {
                        img.setImageResource(R.drawable.axeac_label_mutichoose_disable);
                    }
                } else {
                    if (max == 1) {
                        for (int i = 0; i < count; i++) {
                            ImageView imgs = (ImageView) layoutList.get(i).getTag(R.string.axeac_key1);
                            if (i == pos) {
                                layoutList.get(i).setTag(R.string.axeac_key0, true);
                                imgs.setImageResource(R.drawable.axeac_label_singlechoose_enable);
                            } else {
                                layoutList.get(i).setTag(R.string.axeac_key0, false);
                                imgs.setImageResource(R.drawable.axeac_label_singlechoose_disable);
                            }
                        }
                    } else {
                        int c = 0;
                        for (int k = 0; k < count; k++) {
                            boolean flags = (Boolean) layoutList.get(k).getTag(R.string.axeac_key0);
                            if (flags) {
                                c += 1;
                            }
                        }
                        if (c < max) {
                            layoutList.get(pos).setTag(R.string.axeac_key0, true);
                            img.setImageResource(R.drawable.axeac_label_mutichoose_enable);
                        } else {
                            Toast.makeText(ctx, R.string.axeac_msg_choice_ex, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if (type.trim().toLowerCase().equals("list")
                        || (CommonUtil.isNumeric(type.trim()) && Integer.parseInt(type.trim()) >= count)) {
                    boolean[] selectedItems = new boolean[count];
                    for (int i = 0; i < selectedItems.length; i++) {
                        if ((Boolean) layoutList.get(i).getTag(R.string.axeac_key0)) {
                            selectedItems[i] = true;
                        } else {
                            selectedItems[i] = false;
                        }
                    }
                    returnVals(selectedItems);
                    executeOnChange(selectedItems[pos], pos);
                }
            }
        };
    }

    /**
     * 显示提示框
     * @param hasSearchBox
     * 是否显示搜索框标志
     * */
    private void showDialog(boolean hasSearchBox) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(ctx).inflate(R.layout.axeac_label_click, null);
        textField = (TextView) layout.findViewById(R.id.label_click_text);
        if (max == 1) {
            String textIndex = "";
            for (int i = 0; i < values.size(); i++) {
                if (text.equals(values.get(i))) {
                    textIndex = ids.get(i);
                }
            }
            if (!"".equals(textIndex)) {
                selectedIds.clear();
                selectedIds.add(textIndex);
            }
            if (selectedIds.size() == 0) {

            } else {
                String id = selectedIds.get(0);
                selectedIds.clear();
                selectedIds.add(id);
            }
        }
        List<String> sids = selectedIds;
        selectedIds = new ArrayList<String>();
        for (int i = 0; i < sids.size(); i++) {
            if (ids.contains(sids.get(i))) {
                selectedIds.add(sids.get(i));
            }
        }
        if (selectedIds.size() > max) {
            for (int i = max; i < selectedIds.size(); i++) {
                selectedIds.remove(i);
            }
        }
        String id = "";
        String val = "";
        for (int i = 0; i < selectedIds.size(); i++) {
            for (int j = 0; j < ids.size(); j++) {
                if (ids.get(j).equals(selectedIds.get(i))) {
                    id += ids.get(j) + ",";
                    val += values.get(j) + ",";
                    break;
                }
            }
        }
        if (!id.equals("")) {
            if (id.endsWith(",")) {
                id = id.substring(0, id.length() - 1);
                val = val.substring(0, val.length() - 1);
            }
            items = id;
            textField.setText(val);
        }
        if (!readOnly) {
            textField.setOnClickListener(mDialogOnClickListener(hasSearchBox));
            layout.findViewById(R.id.label_click_btn).setOnClickListener(mDialogOnClickListener(hasSearchBox));
        }
        valLayout.addView(layout);
    }

    /**
     * 提示框点击监听
     * @param hasSearchBox
     * 是否显示搜索框标志
     * */
    private View.OnClickListener mDialogOnClickListener(final boolean hasSearchBox) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String value = textField.getText().toString();
                    String[] texts = StringUtil.split(value, ",");
                    if (texts.length > 0) {
                        selectedIds.clear();
                        for (String t : texts) {
                            String textId = "";
                            for (int i = 0; i < values.size(); i++) {
                                if (t.equals(values.get(i))) {
                                    textId = ids.get(i);
                                }
                            }
                            selectedIds.add(textId);
                        }
                    }
                    boolean[] selectedItems = new boolean[count];
                    if (selectedIds != null && selectedIds.size() > 0) {
                        for (int i = 0; i < count; i++) {
                            if (selectedIds.contains(ids.get(i))) {
                                selectedItems[i] = true;
                            } else {
                                selectedItems[i] = false;
                            }
                        }
                    }
                    CustomDialog.Builder builder = new CustomDialog.Builder(ctx);
                    builder.setTitle(R.string.axeac_msg_choice);
                    ScrollView valLayout = new ScrollView(ctx);
                    valLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.FILL_PARENT,
                            (int) (StaticObject.deviceHeight * 0.6)));
                    LinearLayout linLayout = new LinearLayout(ctx);
                    linLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.FILL_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    linLayout.setOrientation(LinearLayout.VERTICAL);
                    final List<LinearLayout> layoutList = new ArrayList<LinearLayout>();
                    if (hasSearchBox) {
                        RelativeLayout searchBox = (RelativeLayout) LayoutInflater.from(ctx).inflate(R.layout.axeac_label_text, null);
                        EditText searchInput = (EditText) searchBox.findViewById(R.id.label_text_single);
                        searchInput.setHint("Search...");
                        searchInput.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int c) {
                                for (int i = 0; i < count; i++) {
                                    layoutList.get(i).setVisibility(View.GONE);
                                }
                                for (int i = 0; i < count; i++) {
                                    if (values.get(i).contains(s)) {
                                        layoutList.get(i).setVisibility(View.VISIBLE);
                                    }
                                }
                                if (s.equals("")) {
                                    for (int i = 0; i < count; i++) {
                                        layoutList.get(i).setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });
                        linLayout.addView(searchBox);
                    }
                    for (int i = 0; i < count; i++) {
                        LinearLayout layout = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.axeac_label_choice_2, null);
                        ImageView img = (ImageView) layout.findViewById(R.id.label_choice_check_2);
                        TextView text = (TextView) layout.findViewById(R.id.label_choice_text_2);
                        if (max == 1) {
                            if (selectedItems[i]) {
                                img.setImageResource(R.drawable.axeac_label_singlechoose_enable);
                            } else {
                                img.setImageResource(R.drawable.axeac_label_singlechoose_disable);
                            }
                        } else {
                            if (selectedItems[i]) {
                                img.setImageResource(R.drawable.axeac_label_mutichoose_enable);
                            } else {
                                img.setImageResource(R.drawable.axeac_label_mutichoose_disable);
                            }
                        }
                        text.setText(values.get(i));
                        layout.setTag(R.string.axeac_key0, selectedItems[i]);
                        layout.setTag(R.string.axeac_key1, img);
                        layoutList.add(layout);
                        linLayout.addView(layout);
                    }
                    for (int i = 0; i < count; i++) {
                        layoutList.get(i).setOnClickListener(mLayoutOnItemClickListener(i, layoutList));
                    }
                    valLayout.addView(linLayout);
                    builder.setContentView(valLayout);
                    builder.setPositiveButton(R.string.axeac_msg_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean[] selectedItems = new boolean[count];
                            for (int i = 0; i < selectedItems.length; i++) {
                                if ((Boolean) layoutList.get(i).getTag(R.string.axeac_key0)) {
                                    selectedItems[i] = true;
                                } else {
                                    selectedItems[i] = false;
                                }
                            }
                            textField.setText(returnVals(selectedItems));
                            if (max == 1) {
                                int index = 0;
                                for (int i = 0; i < selectedItems.length; i++) {
                                    if (selectedItems[i]) {
                                        index = i;
                                    }
                                }
                                executeOnChange(selectedItems[index], index);
                            }
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(R.string.axeac_msg_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            return;
                        }
                    });
                    builder.create().show();
                } catch (Throwable e) {
                    String clsName = this.getClass().getName();
                    clsName = clsName.substring(clsName.lastIndexOf(".") + 1);
                    String info = ctx.getString(R.string.axeac_toast_exp_click);
                    Toast.makeText(ctx, clsName + info, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    /**
     * 返回存储有列表项选中数据id的list集合
     * @param selectedItems
     * 是否选中标志
     * @return
     * 存储有列表项选中数据id的list集合
     * */
    private String returnVals(boolean[] selectedItems) {
        selectedIndex = "";
        selectedValue = "";
        for (int i = 0; i < count; i++) {
            if (selectedItems[i]) {
                selectedIndex += this.ids.get(i);
                selectedIndex += ",";
                selectedValue += this.values.get(i);
                selectedValue += ",";
            }
        }
        if (selectedIndex.length() > 0 && selectedIndex.endsWith(",")) {
            selectedIndex = selectedIndex.substring(0, selectedIndex.length() - 1);
        } else {
            selectedIndex = "-1";
        }
        if (selectedValue.length() > 0 && selectedValue.endsWith(",")) {
            selectedValue = selectedValue.substring(0, selectedValue.length() - 1);
        }
        items = selectedIndex;
        return selectedValue;
    }

    /**
     * 列表项改变后执行的方法
     * @param flag
     * 是否可改变标志
     * @param position
     * 位置
     * */
    private void executeOnChange(boolean flag, int position) {
        if (flag) {
            if (this.getChangeui() != null && !"".equals(this.getChangeui())) {
                Object obj = StaticObject.ComponentMap.get(this.getChangeui());
                if (obj != null && "LabelText".equals(obj.getClass().getSimpleName())) {
                    LabelText labelText = (LabelText) obj;
                    labelText.setValue(values.get(position));
                    this.layoutView.invalidate();
                }
            }
            String[] parms = StringUtil.split(onchange, ":");
            if (parms.length != 4) {
                return;
            }
            if (!"COMPONENT".equals(parms[0])) {
                return;
            }
            String meip = "MEIP_PAGE=" + parms[1] + "\r\n";
            if (parms[2] != null && !"".equals(parms[2])) {
                meip += "MEIP_PAGE_COMPONENT=" + parms[2] + "\r\n";
            }
            String[] args = StringUtil.split(parms[3], ",");
            for (String arg : args) {
                int argIndex = arg.indexOf("=");
                if (argIndex > 0) {
                    String argId = arg.substring(0, argIndex);
                    String argVal = arg.substring(argIndex + 1, arg.length());
                    if (argVal.startsWith("@")) {
                        argVal = argVal.substring(1);
                        Component comp = null;
                        if (StaticObject.ComponentMap.containsKey(argVal)) {
                            comp = StaticObject.ComponentMap.get(argVal);
                        }
                        if (comp != null && comp.returnable == true && comp.getValue() != null) {
                            meip += argId + "=" + comp.getValue() + "\r\n";
                        }
                    } else {
                        meip += argId + "=" + argVal + "\r\n";
                    }
                }
            }
            doLoading(meip);
        }
    }

    /**
     * 请求数据
     * @param parm
     * 请求字段
     * */
    private void doLoading(final String parm) {

        UIHelper.send(ctx, parm, new OnRequestCallBack() {
            @Override
            public void onStart() {
                if (loadingDialog != null && !loadingDialog.isShowing()) {
                    loadingDialog.show();
                }
            }

            @Override
            public void onCompleted() {
                if (loadingDialog != null && !loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onSuccesed(JHSPResponse response) {
                if (response.getCode() == 0) {
                    if (!CommonUtil.isResponseNoToast(response.getMessage())) {
                        Toast.makeText(ctx, response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Property items = new Property();
                    items.setSplit("\n");
                    items.load(response.getData());
                    LinkedHashtable vars = items.searchSubKey("var.");
                    Vector<?> compIds = vars.linkedKeys();
                    for (int i = 0; i < compIds.size(); i++) {
                        String compId = (String) compIds.elementAt(i);
                        if (items.getProperty("var." + compId, "").equals("LabelList")) {
                            String clearStr = items.getProperty(compId + ".clear", "false");
                            if (Boolean.parseBoolean(clearStr)) {
                                ((LabelList) StaticObject.ComponentMap.get(compId)).clear();
                            }
                        }
                    }
                    analysis.analysis(items);
                } else {
                    Toast.makeText(ctx, response.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onfailed(Throwable e) {
                if (loadingDialog != null && !loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }
        });
    }

    /**
     * 更新操作的Handler对象
     * */
    private final Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            JHSPResponse response = (JHSPResponse) msg.obj;
            if (msg.what == 0) {
                if (!CommonUtil.isResponseNoToast(response.getMessage())) {
                    Toast.makeText(ctx, response.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Property items = new Property();
                items.setSplit("\n");
                items.load(response.getData());
                LinkedHashtable vars = items.searchSubKey("var.");
                Vector<?> compIds = vars.linkedKeys();
                for (int i = 0; i < compIds.size(); i++) {
                    String compId = (String) compIds.elementAt(i);
                    if (items.getProperty("var." + compId, "").equals("LabelList")) {
                        String clearStr = items.getProperty(compId + ".clear", "false");
                        if (Boolean.parseBoolean(clearStr)) {
                            ((LabelList) StaticObject.ComponentMap.get(compId)).clear();
                        }
                    }
                }
                analysis.analysis(items);
            } else {
                Toast.makeText(ctx, response.getMessage(), Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 清空list集合，移除所有组件
     * */
    public void clear() {
        ids.clear();
        values.clear();
        selectedIds.clear();
        valLayout.removeAllViews();
    }

    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public String getValue() {
        return "-1".equals(items) ? "" : items;
    }

    @Override
    public void repaint() {

    }

    @Override
    public void starting() {
        this.buildable = false;
    }

    @Override
    public void end() {
        this.buildable = true;
    }

    /**
     * 获取增加影响的labelText的id
     * @return
     * 增加影响的labelText的id
     * */
    public String getChangeui() {
        return changeui;
    }

    /**
     * 设置增加影响的labelText的id
     * @param changeui
     * 增加影响的labelText的id
     * */
    public void setChangeui(String changeui) {
        this.changeui = changeui;
    }
}