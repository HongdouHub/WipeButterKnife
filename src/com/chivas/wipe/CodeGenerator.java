package com.chivas.wipe;

import com.chivas.wipe.bean.FileParseBean;
import com.chivas.wipe.bindview.BindViewFactory;
import com.chivas.wipe.bindview.IBindView;
import com.chivas.wipe.onclick.IOnClick;
import com.chivas.wipe.onclick.OnClickFactory;
import com.chivas.wipe.utils.JavaFileParseUtils;
import com.chivas.wipe.utils.TextUtils;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.chivas.wipe.constants.ButterKnifeConstants.BIND_VIEW;
import static com.chivas.wipe.constants.ButterKnifeConstants.ONCLICK;

public class CodeGenerator {

    private final PsiClass mClass;
    private final IBindView mBindViewAdapter;
    private final IOnClick mOnClickAdapter;

    public CodeGenerator(PsiClass psiMethod) {
        mClass = psiMethod;

        // 1. 确认是Activity还是Fragment
        mBindViewAdapter = BindViewFactory.create(mClass);
        mOnClickAdapter = OnClickFactory.create(mClass);
    }

    public void generate() {
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mClass.getProject());
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(mClass.getProject());

        // 2. 解析文件中的ButterKnife注解
        VirtualFile virtualFile = mClass.getContainingFile().getVirtualFile();
        String filePath = virtualFile.getPath();
        Map<String, List<FileParseBean>> parseMap = JavaFileParseUtils.parse(filePath);

        // 2.1 解析@BindView字段
        List<String> assembleBindViewList = new ArrayList<>();
        List<FileParseBean> list = parseMap.get(BIND_VIEW);
        for (FileParseBean bean : list) {
            assembleBindViewList.add(bean.formatBindView(mBindViewAdapter.getAssembleFormat()));
        }

        // 2.2 解析@OnClick注解
        List<String> assembleOnClickList = new ArrayList<>();
        list = parseMap.get(ONCLICK);
        for (FileParseBean bean : list) {
            assembleOnClickList.addAll(bean.formatOnClick(mOnClickAdapter.getAssembleFormat(bean.isHasParameter())));
        }

        // 3. 优先搜索是否有 _findViews 方法，目的是增量补充
        PsiMethod findViewsMethod = findMethod(mClass, mBindViewAdapter.getFindViewsMethodName(), mBindViewAdapter.getMethodParameter());

        PsiCodeBlock findViewsBody;
        if (findViewsMethod == null || (findViewsBody = findViewsMethod.getBody()) == null) {
            // 4. 首次初始化，将 @BindView 字段改动到 _findViews 方法内实现
            findViewsMethod = elementFactory.createMethodFromText(generateFindViews(assembleBindViewList), mClass);
            styleManager.shortenClassReferences(mClass.addBefore(findViewsMethod, mClass.getLastChild()));

        } else {
            // 5. 增量补充，将 新@BindView 字段补充到 _findViews 方法的首行
            PsiStatement[] statements = findViewsBody.getStatements();
            for (String s : assembleBindViewList) {
                if (statements.length == 0) {
                    findViewsBody.add(elementFactory.createStatementFromText(s, mClass));
                } else {
                    findViewsBody.addAfter(elementFactory.createStatementFromText(s, mClass), statements[0]);
                }
            }
        }
        
        // 6. 优先搜索是否有 _initListeners 方法，目的是增量补充
        PsiMethod initListenersMethod = findMethod(mClass, mOnClickAdapter.getInitListenersMethodName(), mOnClickAdapter.getMethodParameter());

        PsiCodeBlock onClickBody;
        if (initListenersMethod == null || (onClickBody = initListenersMethod.getBody()) == null) {
            // 7. 首次初始化，将 @OnClick 方法改动到 _initListeners 方法内实现
            initListenersMethod = elementFactory.createMethodFromText(generateInitListeners(assembleOnClickList), mClass);
            styleManager.shortenClassReferences(mClass.addBefore(initListenersMethod, mClass.getLastChild()));
            
        } else {
            // 8. 增量补充，将 新@OnClick 字段补充到 _initListeners 方法的首行
            PsiStatement[] statements = onClickBody.getStatements();
            for (String s : assembleOnClickList) {
                if (statements.length == 0) {
                    onClickBody.add(elementFactory.createStatementFromText(s, mClass));
                } else {
                    onClickBody.addAfter(elementFactory.createStatementFromText(s, mClass), statements[0]);
                }
            }
        }
        
        // 9. 移除带 @BindView 注解的成员变量，同时改为私有属性
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

        // 10. 移除带 @OnClick 注解的方法，同时改为私有方法
        PsiMethod[] methods = mClass.getAllMethods();
        for (PsiMethod method : methods) {
            PsiAnnotation[] annotations = method.getAnnotations();
            for (PsiAnnotation annotation : annotations) {
                if (TextUtils.equals(annotation.getQualifiedName(), "butterknife.OnClick")) {
                    annotation.delete();
                    method.getModifierList().setModifierProperty(PsiModifier.PRIVATE, true);
                }
            }
        }

        // 11. 移除 import BindView包
        PsiFile containingFile = mClass.getContainingFile();
        if (containingFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) containingFile;
            PsiImportList importList = psiJavaFile.getImportList();

            if (importList != null) {
                PsiImportStatement[] importStatements = importList.getImportStatements();

                deleteStatementByContains(importStatements, new String[] {
                        "butterknife.BindView",
                        "butterknife.OnClick",
                        ".R2"
                });
            }
        }

        // 12. 将虚拟文件同步到插件中的物理文件
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        Document document = fileDocumentManager.getDocument(virtualFile);
        if (document != null) {
            fileDocumentManager.saveDocument(document);
        }
    }

//    if (extraFindViewsMethod != null) {
//        styleManager.shortenClassReferences(mClass.addBefore(extraFindViewsMethod, mClass.getLastChild()));
//    }
//            PsiStatement extraFindViewsStatement = null;
//            for (PsiStatement statement : statements) {
//                // 查找 findViews 方法
//                if (statement.getFirstChild() instanceof PsiMethodCallExpression) {
//                    PsiReferenceExpression methodExpression = ((PsiMethodCallExpression) statement.getFirstChild()).getMethodExpression();
//
//                    if (TextUtils.equals(methodExpression.getText(), mBindViewAdapter.getExtraFindViewsMethodName())) {
//                        extraFindViewsStatement = statement;
//                    }
//                }
//            }
//
//            // 未找到，则在开始处插入
//            if (extraFindViewsStatement == null) {
//                String text = mBindViewAdapter.getExtraFindViewsMethodName() + "();";
//                extraFindViewsStatement = elementFactory.createStatementFromText(text, mClass);
//
//                if (statements.length == 0) {
//                    body.add(extraFindViewsStatement);
//                } else {
//                    body.addBefore(extraFindViewsStatement, statements[0]);
//                }
//            }

    private void deleteStatementByContains(PsiImportStatement[] importStatements, String[] contains) {
        for (PsiImportStatement statement : importStatements) {
            for (String contain : contains) {
                if (statement.getText().contains(contain)) {
                    statement.delete();
                }
            }
        }
    }

    private static PsiMethod findMethod(PsiClass clazz, String methodName, String... arguments) {
        PsiMethod[] methods = clazz.findMethodsByName(methodName, false);

        for (PsiMethod method : methods) {
            PsiParameterList parameterList = method.getParameterList();

            if (parameterList.getParametersCount() == arguments.length) {
                boolean findSuccess = true;

                PsiParameter[] parameters = parameterList.getParameters();

                for (int i = 0; i < arguments.length; i++) {
                    if (!parameters[i].getType().getCanonicalText().equals(arguments[i])) {
                        findSuccess = false;
                    }
                }

                if (findSuccess) {
                    return method;
                }
            }
        }
        return null;
    }

    private String generateFindViews(List<String> assembleList) {
        StringBuilder sb = new StringBuilder(mBindViewAdapter.getFindViewsMethodDescribe() + " {");
        if (hasSuperMethod(mBindViewAdapter.getFindViewsMethodName())) {
            sb.append(mBindViewAdapter.getSuperFindViewsMethod()).append(";");
        }

        for (String s : assembleList) {
            sb.append(s);
        }
        sb.append("}");
        return sb.toString();
    }

    private String generateInitListeners(List<String> assembleOnClickList) {
        StringBuilder sb = new StringBuilder(mOnClickAdapter.getInitListenersMethodDescribe() + " {");
        if (hasSuperMethod(mOnClickAdapter.getInitListenersMethodName())) {
            sb.append(mOnClickAdapter.getSuperInitListenersMethod()).append(";");
        }

        for (String s : assembleOnClickList) {
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
