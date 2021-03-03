package com.chivas.wipe;

import com.chivas.wipe.bindview.BindViewFactory;
import com.chivas.wipe.bindview.IBindView;
import com.chivas.wipe.debug.Log;
import com.chivas.wipe.utils.BindViewUtils;
import com.chivas.wipe.utils.TextUtils;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.util.List;

public class CodeGenerator {

    private final PsiClass mClass;
    private final IBindView mBindViewAdapter;

    public CodeGenerator(PsiClass psiMethod) {
        mClass = psiMethod;

        // 1. 确认是Activity还是Fragment
        mBindViewAdapter = BindViewFactory.create(mClass);
    }

    public void generate() {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(mClass.getProject());

        // 2. 优先搜索是否有待

        // 2. 移除已有的 _findViews 方法
        findAndRemoveMethod(mClass, mBindViewAdapter.getFindViewsMethodName(), mBindViewAdapter.getMethodParameter());

        // 3. 生成额外自定义的 findViews 方法
        PsiMethod extraFindViewsMethod = hasMethod(mBindViewAdapter.getExtraFindViewsMethodName()) ? null :
                elementFactory.createMethodFromText(
                        generateExtraFindViewsContents(mBindViewAdapter.getExtraFindViewsMethodDescribe()),
                        mClass);


        // 4. 将 @BindView 改为 _findViews 方法内的实现
        String filePath = mClass.getContainingFile().getVirtualFile().getPath();
        PsiMethod findViewsMethod = elementFactory.createMethodFromText(generateFindViews(filePath), mClass);

        PsiCodeBlock body = findViewsMethod.getBody();
        PsiStatement[] statements = body.getStatements();
        body.add(new PsiElement());

        PsiMethodCallExpression

        // 5. 移除带 @BindView 注解的成员变量，同时改为私有属性
        PsiField[] fields = mClass.getFields();
        for (PsiField field : fields) {
            PsiAnnotation[] annotations = field.getAnnotations();
            for (PsiAnnotation annotation : annotations) {
                if (TextUtils.equals(annotation.getQualifiedName(), "butterknife.BindView")) {
                    annotation.delete();
                    field.getModifierList().setModifierProperty(PsiModifier.PRIVATE, true);
                }
            }
        }

        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mClass.getProject());
        styleManager.shortenClassReferences(mClass.addBefore(findViewsMethod, mClass.getLastChild()));
        if (extraFindViewsMethod != null) {
            styleManager.shortenClassReferences(mClass.addBefore(extraFindViewsMethod, mClass.getLastChild()));
        }

        // 6. 移除 import BindView包
        PsiFile containingFile = mClass.getContainingFile();
        if (containingFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) containingFile;
            PsiImportList importList = psiJavaFile.getImportList();

            if (importList != null) {
                PsiImportStatement[] importStatements = importList.getImportStatements();

                for (PsiImportStatement statement : importStatements) {
                    if (statement.getText().contains("butterknife.BindView")) {
                        statement.delete();
                    }
                }
            }
        }
    }

    private static void findAndRemoveMethod(PsiClass clazz, String methodName, String... arguments) {
        // Maybe there's an easier way to do this with mClass.findMethodBySignature(), but I'm not an expert on Psi*
        PsiMethod[] methods = clazz.findMethodsByName(methodName, false);

        for (PsiMethod method : methods) {
            PsiParameterList parameterList = method.getParameterList();

            if (parameterList.getParametersCount() == arguments.length) {
                boolean shouldDelete = true;

                PsiParameter[] parameters = parameterList.getParameters();

                for (int i = 0; i < arguments.length; i++) {
                    if (!parameters[i].getType().getCanonicalText().equals(arguments[i])) {
                        shouldDelete = false;
                    }
                }

                if (shouldDelete) {
                    method.delete();
                }
            }
        }
    }

    private String generateExtraFindViewsContents(String methodDescribe) {
        return methodDescribe + " {}";
    }

    private String generateFindViews(String filePath) {
        StringBuilder sb = new StringBuilder(mBindViewAdapter.getFindViewsMethodDescribe() + " {");
        if (hasSuperMethod(mBindViewAdapter.getFindViewsMethodName())) {
            sb.append(mBindViewAdapter.getSuperFindViewsMethod())
                    .append(";");
        }

        sb.append(mBindViewAdapter.getExtraFindViewsMethodName()).append("();");

        List<String> assembleList = BindViewUtils.parse(filePath, mBindViewAdapter.getAssembleFormat());
        Log.d("#assembleList.size = " + assembleList.size());
        for (String s : assembleList) {
            sb.append(s);
        }
        sb.append("}");
        return sb.toString();
    }

//    private boolean hasParcelableSuperclass() {
//        PsiClassType[] superTypes = mClass.getSuperTypes();
//        for (PsiClassType superType : superTypes) {
//            if (PsiUtils.isOfType(superType, "android.os.Parcelable")) {
//                return true;
//            }
//        }
//        return false;
//    }

    private boolean hasMethod(String methodName) {
        if (methodName == null) return false;

        PsiMethod[] methods = mClass.getAllMethods();
        for (PsiMethod method : methods) {
            if (method.getBody() == null) continue;

            String name = method.getName();
            if (TextUtils.equals(name, methodName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSuperMethod(String methodName) {
        if (methodName == null) return false;

        PsiMethod[] superclassMethods = mClass.getSuperClass() != null ? mClass.getAllMethods() : new PsiMethod[0];
        for (PsiMethod superclassMethod : superclassMethods) {
            if (superclassMethod.getBody() == null) continue;

            String name = superclassMethod.getName();
            if (TextUtils.equals(name, methodName)) {
                return true;
            }
        }
        return false;
    }
}
