package com.chivas.wipe.bindview;

public class FragmentAdapter implements IBindView {

    @Override
    public String[] getMethodParameter() {
        return new String[] {"android.view.View"};
    }

    @Override
    public String getFindViewsMethodName() {
        return "_findViews";
    }

    @Override
    public String getFindViewsMethodDescribe() {
        return "protected void _findViews(android.view.View view)";
    }

    @Override
    public String getSuperFindViewsMethod() {
        return "super._findViews(view)";
    }

    @Override
    public String getAssembleFormat() {
        return "%s = (%s) view.findViewById(%s);";
    }
}
