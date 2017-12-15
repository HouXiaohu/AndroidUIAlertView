package com.hxh.component.ui.alertview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class AlertViewAdapter extends BaseAdapter{
    private List<String> mDatas;
    private List<String> mDestructive;
    private int [] otherscolors;
    private int othercolor;
    public AlertViewAdapter(List<String> datas,List<String> destructive){
        this.mDatas =datas;
        this.mDestructive =destructive;
    }
    public AlertViewAdapter(List<String> datas,List<String> destructive,int [] otherscolor){
        this.mDatas =datas;
        this.otherscolors = otherscolor;
        this.mDestructive =destructive;
    }
    public AlertViewAdapter(List<String> datas,List<String> destructive,int othercolor){
        this.mDatas =datas;
        this.othercolor =othercolor;
        this.mDestructive =destructive;
    }
    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String data= mDatas.get(position);
        Holder holder=null;
        View view =convertView;
        if(view==null){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view=inflater.inflate(R.layout.item_alertbutton, null);
            holder=creatHolder(view);
            view.setTag(holder);
        }
        else{
            holder=(Holder) view.getTag();
        }
        holder.UpdateUI(parent.getContext(),data,position);
        return view;
    }
    public Holder creatHolder(View view){
        return new Holder(view);
    }
    class Holder {
        private TextView tvAlert;

        public Holder(View view){
            tvAlert = (TextView) view.findViewById(R.id.tvAlert);
        }
        public void UpdateUI(Context context,String data,int position){
            tvAlert.setText(data);
            if(null != otherscolors)
            {
                tvAlert.setTextColor(otherscolors[position]);
            }else
            {
                tvAlert.setTextColor(othercolor);
            }

        }
    }
}