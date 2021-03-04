package com.chivas.wipe.onclick;

public class FragmentAdapter implements IOnClick {

    @Override
    public String getInitListenersMethodName() {
        return "_initListeners";
    }

    @Override
    public String getInitListenersMethodDescribe() {
        return "protected void _initListeners(android.view.View view)";
    }

    @Override
    public String getSuperInitListenersMethod() {
        return "super._initListeners(view)";
    }

    @Override
    public String[] getMethodParameter() {
        return new String[] {"android.view.View"};
    }

    @Override
    public String getAssembleFormat(boolean hasParameter) {
        StringBuilder builder = new StringBuilder();
        builder.append("(view.findViewById(%s)).setOnClickListener(new View.OnClickListener() {\n")
                .append("@Override\n")
                .append("public void onClick(View v) {\n")
                .append("%s(")
                .append(hasParameter ? "v);\n" : ");\n")
                .append("}\n")
                .append("});");
        return builder.toString();
    }
}
