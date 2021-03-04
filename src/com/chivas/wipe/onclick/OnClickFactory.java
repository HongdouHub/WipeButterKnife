package com.chivas.wipe.onclick;

import com.chivas.wipe.utils.TextUtils;
import com.intellij.psi.PsiClass;

public class OnClickFactory {

    public static IOnClick create(PsiClass psiClass) {
        PsiClass superClass = psiClass;
        String name;

        while (superClass != null) {
            name = superClass.getContainingFile().getVirtualFile().getName();

            if (!TextUtils.isEmpty(name)) {
                if (name.endsWith("Activity.java")) {
                    return new ActivityAdapter();
                }

                if (name.endsWith("Fragment.java")) {
                    return new FragmentAdapter();
                }
            }

            superClass = superClass.getSuperClass();
        }
        return new ActivityAdapter();
    }

}
