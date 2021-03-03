package com.chivas.wipe.bindview;

public class ActivityAdapter implements IBindView {

    @Override
    public String[] getMethodParameter() {
        return new String[0];
    }

    @Override
    public String getFindViewsMethodName() {
        return "_findViews";
    }

    @Override
    public String getFindViewsMethodDescribe() {
        return "protected void _findViews()";
    }

    @Override
    public String getSuperFindViewsMethod() {
        return "super._findViews()";
    }

    @Override
    public String getExtraFindViewsMethodName() {
        return "findViews";
    }

    @Override
    public String getExtraFindViewsMethodDescribe() {
        return "protected void findViews()";
    }

    @Override
    public String getExtraFindViewsMethod() {
        return "super.findViews()";
    }

    @Override
    public String getAssembleFormat() {
        return "%s = (%s) findViewById(%s);";
    }
}
