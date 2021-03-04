package com.chivas.wipe.onclick;

public interface IOnClick {

    /**
     * _initListeners
     */
    String getInitListenersMethodName();

    /**
     * protected void _initListeners()
     */
    String getInitListenersMethodDescribe();

    /**
     * _initListeners(view)
     */
    String getSuperInitListenersMethod();

    String[] getMethodParameter();

    String getAssembleFormat(boolean hasParameter);

}
