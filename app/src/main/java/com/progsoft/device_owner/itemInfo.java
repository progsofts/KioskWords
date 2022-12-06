package com.progsoft.device_owner;

import androidx.annotation.ColorInt;

import java.io.Serializable;

/**
 * Created by Thinkpad on 2018/7/5.
 */

public class itemInfo implements Serializable{
    String question;
    String answer;
    String yourAnswer;
    int total,right,weight,number,delta,max;
    boolean selected = false;
    @ColorInt  int color;
    public void setColor(@ColorInt int c) {
        this.color = c;
    }

    public void setQuestion(String question){
        this.question = question;
    }
    public void setAnswer(String answer){
        this.answer = answer;
    }
    public void setYourAnswer(String yourAnswer){this.yourAnswer = yourAnswer;}

    public void setDelta(int d) {
        if (d > 0 && this.delta < 0)
            return;
        this.delta = d;
    }

    public void update() {
        calcTotal(1);
        if (this.delta > 0) {
            calcRight(1);
        }
        calcWeight(this.delta);
    }

    public String getQuestion(){
        return question;
    }
    public String getAnswer(){
        return answer;
    }
    public String getYourAnswer() {return yourAnswer;}
    public @ColorInt int getColor() {
        return color;
    }

    public void calcTotal(int delta) {
        this.total += delta;
    }
    public void calcRight(int delta) {
        this.right += delta;
    }
    public void calcWeight(int delta) {
        this.weight += delta;
        if (KioskModeApp.getTestNum() == 0) // 测试模式，加/减分更多
            this.weight += delta;
        if (this.weight < 0) this.weight = 0;
        if (this.weight > 10) this.weight = 10;
        if (this.max < this.weight) this.max = this.weight; //记录最大的记录权重
    }

    public int getWeight() {
        return weight;
    }

    public itemInfo(String question,String answer,String yourAnswer, int x, int y, int z,@ColorInt int c){
        this.question = question;
        this.answer = answer;
        this.yourAnswer = yourAnswer;
        this.total = x;
        this.right = y;
        this.weight = z;
        this.color = c;
        this.number = 0;
        this.max = 0;
    }

    public itemInfo(int number, String question,String answer,String yourAnswer, int x, int y, int z, int max, @ColorInt int c){
        this.question = question;
        this.answer = answer;
        this.yourAnswer = yourAnswer;
        this.total = x;
        this.right = y;
        this.weight = z;
        this.color = c;
        this.number = number;
        this.max = max;
    }


    public itemInfo(String question,String answer,String yourAnswer,@ColorInt int c){
        this.question = question;
        this.answer = answer;
        this.yourAnswer = yourAnswer;
        this.color = c;
    }
}
