package com.hxh.component.ui.alertview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Sai on 15/8/9.
 * 精仿iOSAlertViewController控件
 * 点击取消按钮返回 －1，其他按钮从0开始算
 */
public class AlertView {
    public enum Style {
        ActionSheet,
        Alert
    }


    private final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
    );
    public static final int HORIZONTAL_BUTTONS_MAXCOUNT = 2;
    public static final int CANCELPOSITION = -1;//点击取消按钮返回 －1，
    public static final int CONFIRMPOSITION = 0;//点击确定按钮返回 0，其他按钮从0开始算


    private String title, msg;

    private String confirmTitle, cancelTitle;
    private int cancelColor, confirmColor;

    private String[] others;
    private List<String> mOthers;

    private ArrayList<String> mDatas = new ArrayList<String>();

    private WeakReference<Context> contextWeak;
    private ViewGroup contentContainer;//AlertView的内容ViewFrameLayout
    private ViewGroup decorView;//activity的根View,我要把我的RootView放入进去
    private ViewGroup rootView;//AlertView 的 根View
    private ViewGroup loAlertHeader;//窗口headerView

    private Style style = Style.Alert;

    private OnDismissListener onDismissListener;
    private OnItemClickListener OnItemClickListenerTest;
    private boolean isShowing;

    private Animation outAnim;
    private Animation inAnim;
    private int gravity = Gravity.CENTER;
    private int mOtherColor;
    private int[] mOtherColors;

    public AlertView(Builder builder) {
        this.contextWeak = new WeakReference<>(builder.context);
        initData(builder.title, builder.msgContent, builder.cancelTitle, builder.cancelColor, builder.confirmTitle, builder.confirmColor, builder.others, builder.style, builder.OnItemClickListenerTest);
        mOtherColor = builder.othersColor;
        mOtherColors = builder.othersColors;
        initViews();
        init();

    }

    public AlertView(Context context, String title, String msg, String cancel, int cancelColor, String confirm, int confirmColor, String[] others, Style style, OnItemClickListener OnItemClickListenerTest) {
        this.contextWeak = new WeakReference<>(context);
        initData(title, msg, cancel, cancelColor, confirmTitle, confirmColor, others, style, OnItemClickListenerTest);
        initViews();
        init();

    }

    /**
     * 获取数据
     */
    protected void initData(String title, String msg, String cancel, int cancelColor, String confirm, int confirmColor, String[] others, Style style, OnItemClickListener OnItemClickListenerTest) {
        this.OnItemClickListenerTest = OnItemClickListenerTest;
        this.title = title;
        this.msg = msg;
        if (style != null) this.style = style;
        this.cancelTitle = cancel;
        this.confirmTitle = confirm;
        this.cancelColor = cancelColor;
        this.confirmColor = confirmColor;
        this.others = others;

        if (cancel != null) {
            this.others = null;
            if (style == Style.Alert && mDatas.size() < HORIZONTAL_BUTTONS_MAXCOUNT) {
                this.mDatas.add(0, cancel);
            }
        }
        if (confirm != null) {
            this.others = null;
            if (style == Style.Alert && mDatas.size() < HORIZONTAL_BUTTONS_MAXCOUNT) {
                if(this.mDatas.size()==0)
                {
                    this.mDatas.add(0, confirm);
                }else
                {
                    this.mDatas.add(1, confirm);
                }
            }
        }


        if (this.style == Style.ActionSheet) {
            if (others != null) {
                this.mOthers = Arrays.asList(others);
                this.mDatas.addAll(mOthers);
            }
        }


    }


    protected void initViews() {
        Context context = contextWeak.get();
        if (context == null) return;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview, decorView, false);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        contentContainer = (ViewGroup) rootView.findViewById(R.id.content_container);
        int margin_alert_left_right = 0;
        if(null == confirmTitle && null == cancelTitle) setCancelable(true);

        switch (style) {
            case ActionSheet:
                params.gravity = Gravity.BOTTOM;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_actionsheet_left_right);
                params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, margin_alert_left_right);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.BOTTOM;
                initActionSheetViews(layoutInflater);
                break;
            case Alert:
                params.gravity = Gravity.CENTER;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
                params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, 0);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.CENTER;
                initAlertViews(layoutInflater);
                break;
        }
    }

    protected void initHeaderView(ViewGroup viewGroup) {
        loAlertHeader = (ViewGroup) viewGroup.findViewById(R.id.loAlertHeader);
        //标题和消息
        TextView tvAlertTitle = (TextView) viewGroup.findViewById(R.id.tvAlertTitle);
        TextView tvAlertMsg = (TextView) viewGroup.findViewById(R.id.tvAlertMsg);
        if (title != null) {
            tvAlertTitle.setText(title);
        } else {
            tvAlertTitle.setVisibility(View.GONE);
            tvAlertMsg.setPadding(0, 53, 0, 0);
        }
        if (msg != null) {
            tvAlertMsg.setText(msg);
        } else {
            tvAlertMsg.setVisibility(View.GONE);
        }
    }

    /**
     * 当Style为AlertSheet时候
     *
     * @time 2017/12/14 17:58
     * @author
     */
    protected void initListView() {
        Context context = contextWeak.get();
        if (context == null) return;

        ListView alertButtonListView = (ListView) contentContainer.findViewById(R.id.alertButtonListView);
        //把cancel作为footerView
        if (cancelTitle != null && style == Style.Alert) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
            TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
            tvAlert.setText(cancelTitle);
            tvAlert.setClickable(true);
            tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
            tvAlert.setTextColor(cancelColor);
            tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
            tvAlert.setOnClickListener(new OnTextClickListener(cancelTitle, CANCELPOSITION));
            alertButtonListView.addFooterView(itemView);
        }
        if (null != mOthers) {
            AlertViewAdapter adapter = null;
            if (null == mOtherColors) {
                adapter = new AlertViewAdapter(mDatas, mOthers, mOtherColor);
            } else {
                adapter = new AlertViewAdapter(mDatas, mOthers, mOtherColors);
            }

            alertButtonListView.setAdapter(adapter);
            alertButtonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    if (OnItemClickListenerTest != null)
                        OnItemClickListenerTest.onItemClick(mOthers.get(position), position);
                    dismiss();
                }
            });
        }

    }

    protected void initActionSheetViews(LayoutInflater layoutInflater) {
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_actionsheet, contentContainer);
        initHeaderView(viewGroup);

        initListView();
        TextView tvAlertCancel = (TextView) contentContainer.findViewById(R.id.tvAlertCancel);
        if (cancelTitle != null) {
            tvAlertCancel.setVisibility(View.VISIBLE);
            tvAlertCancel.setText(cancelTitle);
        }
        tvAlertCancel.setOnClickListener(new OnTextClickListener(cancelTitle, CANCELPOSITION));
    }

    protected void initAlertViews(LayoutInflater layoutInflater) {
        Context context = contextWeak.get();
        if (context == null) return;

        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_alert, contentContainer);
        initHeaderView(viewGroup);


        //如果总数据小于等于HORIZONTAL_BUTTONS_MAXCOUNT，则是横向button
        if (mDatas.size() <= HORIZONTAL_BUTTONS_MAXCOUNT) {
            ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubHorizontal);
            viewStub.inflate();
            LinearLayout loAlertButtons = (LinearLayout) contentContainer.findViewById(R.id.loAlertButtons);

            for (int i = 0; i < mDatas.size(); i++) {
                //如果不是第一个按钮
                if (i != 0) {
                    //添加上按钮之间的分割线
                    View divier = new View(context);
                    divier.setBackgroundColor(context.getResources().getColor(R.color.bgColor_divier));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.size_divier), LinearLayout.LayoutParams.MATCH_PARENT);
                    loAlertButtons.addView(divier, params);
                }
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
                TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
                tvAlert.setClickable(true);
                String data = mDatas.get(i);
                tvAlert.setText(data);


                if (i == 0) {
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_left);
                } else if (i == 1) {
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_right);
                }

                //取消按钮的样式
                if (data.equals(cancelTitle)) {
                    tvAlert.setTextColor(cancelColor);
                    tvAlert.setOnClickListener(new OnTextClickListener(cancelTitle, CANCELPOSITION));
                }

                if (data.equals(confirmTitle)) {
                    tvAlert.setTextColor(confirmColor);
                    tvAlert.setOnClickListener(new OnTextClickListener(confirmTitle, CONFIRMPOSITION));

                }


                loAlertButtons.addView(itemView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            }


        }  else {
            ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubVertical);
            viewStub.inflate();
            initListView();
        }
    }

    protected void init() {
        inAnim = getInAnimation();
        outAnim = getOutAnimation();
    }

    public AlertView addExtView(View extView) {
        loAlertHeader.addView(extView);
        return this;
    }

    /**
     * show的时候调用
     *
     * @param view 这个View
     */
    private void onAttached(View view) {
        isShowing = true;
        decorView.addView(view);
        contentContainer.startAnimation(inAnim);
    }

    /**
     * 添加这个View到Activity的根视图
     */
    public void show() {
        if (isShowing()) {
            return;
        }
        onAttached(rootView);
    }

    /**
     * 检测该View是不是已经添加到根视图
     *
     * @return 如果视图已经存在该View返回true
     */
    public boolean isShowing() {
        return rootView.getParent() != null && isShowing;
    }

    public void dismiss() {
        //消失动画
        outAnim.setAnimationListener(outAnimListener);
        contentContainer.startAnimation(outAnim);
    }

    public void dismissImmediately() {
        decorView.removeView(rootView);
        isShowing = false;
        if (onDismissListener != null) {
            onDismissListener.onDismiss(this);
        }

    }

    public Animation getInAnimation() {
        Context context = contextWeak.get();
        if (context == null) return null;

        int res = AlertAnimateUtil.getAnimationResource(this.gravity, true);
        return AnimationUtils.loadAnimation(context, res);
    }

    public Animation getOutAnimation() {
        Context context = contextWeak.get();
        if (context == null) return null;

        int res = AlertAnimateUtil.getAnimationResource(this.gravity, false);
        return AnimationUtils.loadAnimation(context, res);
    }

    public AlertView setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return this;
    }

    class OnTextClickListener implements View.OnClickListener {

        private int position;
        private String title;

        public OnTextClickListener(String title, int position) {
            this.title = title;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            if (OnItemClickListenerTest != null)
                OnItemClickListenerTest.onItemClick(title, position);
            dismiss();
        }
    }

    private Animation.AnimationListener outAnimListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            dismissImmediately();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    /**
     * 主要用于拓展View的时候有输入框，键盘弹出则设置MarginBottom往上顶，避免输入法挡住界面
     */
    public void setMarginBottom(int marginBottom) {
        Context context = contextWeak.get();
        if (context == null) return;

        int margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
        params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, marginBottom);
        contentContainer.setLayoutParams(params);
    }

    public AlertView setCancelable(boolean isCancelable) {
        View view = rootView.findViewById(R.id.outmost_container);

        if (isCancelable) {
            view.setOnTouchListener(onCancelableTouchListener);
        } else {
            view.setOnTouchListener(null);
        }
        return this;
    }

    /**
     * Called when the user touch on black overlay in order to dismiss the dialog
     */
    private final View.OnTouchListener onCancelableTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dismiss();
            }
            return false;
        }
    };

    /**
     * Builder for arguments
     */
    public static class Builder {
        private Context context;
        private Style style;
        private String title;
        private String msgContent;
        private String cancelTitle;
        private String confirmTitle;
        private int cancelColor = -1234;
        private int confirmColor = -1234;
        private String[] others;
        private int[] othersColors;
        private int othersColor = -1234;


        public Builder(Context context) {
            this.context = context;
        }

        private OnItemClickListener OnItemClickListenerTest;

        public Builder setStyle(Style style) {
            if (style != null) {
                this.style = style;
            }
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String msg) {
            this.msgContent = msg;
            return this;
        }

        public Builder setCancelText(String cancel) {
            this.others = null;
            this.cancelTitle = cancel;
            return this;
        }

        public Builder setConfirmText(String confirmTitle) {
            this.others = null;
            this.confirmTitle = confirmTitle;
            return this;
        }


        public Builder setCancelTextColor(int cancelcolor) {
            this.cancelColor = cancelcolor;
            return this;
        }

        public Builder seConfirmTextColor(int confirmcolor) {
            this.confirmColor = confirmcolor;
            return this;
        }

        public Builder setOthers(String[] others) {
            this.others = others;
            return this;
        }

        public Builder setOthersColor(int color) {
            this.othersColor = color;
            this.othersColors = null;
            return this;
        }

        public Builder setOthersColor(int[] color) {
            this.othersColor = -1234;
            this.othersColors = color;
            return this;
        }


        public Builder setOnItemClickListenerTest(OnItemClickListener OnItemClickListenerTest) {
            this.OnItemClickListenerTest = OnItemClickListenerTest;
            return this;
        }

        public AlertView build() {
            if (-1234 == cancelColor) cancelColor = Color.parseColor("#666666");
            if (-1234 == confirmColor) confirmColor = Color.parseColor("#4996FE");
            if (-1234 == othersColor && null == othersColors)
                othersColor = Color.parseColor("#333333");
            return new AlertView(this);
        }
    }
}
