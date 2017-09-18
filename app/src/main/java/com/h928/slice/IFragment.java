package com.h928.slice;

/**
 * Created by xiechunping on 2017/6/26.
 */

public interface IFragment {

   /**
    * 从父级对象调用Fragment的方法
    *
    * @param args 传递参数
    * @return boolean 返回调用结果
    * */
   public boolean doFromParent(String ...args);
}
