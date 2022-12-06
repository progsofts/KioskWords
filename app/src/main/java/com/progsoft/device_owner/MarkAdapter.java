package com.progsoft.device_owner;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by Thinkpad on 2018/7/5.
 * 码云
 */

public class MarkAdapter extends RecyclerView.Adapter<MarkAdapter.MyHolder>{

   Context context;
   List<itemInfo> list;
   MyHolder myHolder;
   int oldv = -1;
   public interface OnTextClickLister {
       void onClick(View v, int postion);
   }

   public OnTextClickLister mOnClickListener;
   public void setOnClickListener(OnTextClickLister l) {
       mOnClickListener = l;
   }

   public MarkAdapter(Context context, List<itemInfo> list){
       this.context = context;
       this.list = list;
   }


    /**
     * 这个viewholder是用来初始化控件的
     */
    class MyHolder extends RecyclerView.ViewHolder{
        TextView questionTv;
        TextView youAnswerTv;
        TextView answerTv;
        public MyHolder(View itemView) {
            super(itemView);
            questionTv = itemView.findViewById(R.id.item_question);
            //youAnswerTv = itemView.findViewById(R.id.item_youranswer);
            //answerTv = itemView.findViewById(R.id.item_answer);
            questionTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v instanceof TextView) {
                        Log.e("setOnClickListener", "Pos: " + getAdapterPosition() + " View:" + ((TextView)v).getText() + v);
                    } else {

                    }
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(v, getAdapterPosition());
                    }
                }
            });
        }
    }

    /**
     * 用这个方法导入布局文件并创建View holder
     *
     */
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.mark_item_layout, parent, false);
        myHolder = new MyHolder(view);

        return myHolder;
    }

    /**
     * 在这里操作item
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        itemInfo info = list.get(position);

        holder.questionTv.setText(info.getQuestion());
        holder.questionTv.setBackgroundColor(info.getColor());
        //myHolder.answerTv.setText(info.getAnswer());
        //myHolder.youAnswerTv.setText(list.get(position).yourAnswer);
        if(info.yourAnswer.equals(info.answer)){
            myHolder.answerTv.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //防止数据错位，非常重要！！！！！
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void changeData(int position, itemInfo item) {
        list.set(position, item);

        notifyItemChanged(position);
    }

    public void changeData(int position, String item) {
        itemInfo info = list.get(position);
        info.setQuestion(item);

        notifyItemChanged(position);
    }

    public void setBColor(@ColorInt int c) {
        itemInfo info = list.get(oldv);
        info.setColor(c);
        notifyItemChanged(oldv);
    }
}
