package com.chivas.wipe.onclick;

public class ActivityAdapter implements IOnClick {

    @Override
    public String getInitListenersMethodName() {
        return "_initListeners";
    }

    @Override
    public String getInitListenersMethodDescribe() {
        return "protected void _initListeners()";
    }

    @Override
    public String getSuperInitListenersMethod() {
        return "super._initListeners()";
    }

    @Override
    public String[] getMethodParameter() {
        return new String[0];
    }

    @Override
    public String getAssembleFormat(boolean hasParameter) {
        StringBuilder builder = new StringBuilder();
        builder.append("(findViewById(%s)).setOnClickListener(new View.OnClickListener() {\n")
                .append("@Override\n")
                .append("public void onClick(View v) {\n")
                .append("%s(")
                .append(hasParameter ? "v);\n" : ");\n")
                .append("}\n")
                .append("});");
        return builder.toString();
    }
}
