package com.chivas.wipe.bindview;

public interface IBindView {

    /**
     * _findViews
     */
    String getFindViewsMethodName();

    /**
     * protected void _findViews()
     */
    String getFindViewsMethodDescribe();

    /**
     * _findViews(view)
     */
    String getSuperFindViewsMethod();

    String[] getMethodParameter();

    String getAssembleFormat();

}
