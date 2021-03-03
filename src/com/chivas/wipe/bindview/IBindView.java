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

    /**
     * findViews
     */
    String getExtraFindViewsMethodName();

    /**
     * protected void findViews()
     */
    String getExtraFindViewsMethodDescribe();

    /**
     * findViews(view)
     */
    String getExtraFindViewsMethod();

    String[] getMethodParameter();

    String getAssembleFormat();

}
