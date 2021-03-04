package com.chivas.wipe;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class WipeButterKnifeAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        wipe(getPsiClassFromContext(anActionEvent));
    }

    private PsiClass getPsiClassFromContext(AnActionEvent event) {
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            event.getPresentation().setEnabled(false);
            return null;
        }

        // 将虚拟文件同步到插件中的物理文件
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        Document document = fileDocumentManager.getDocument(psiFile.getVirtualFile());
        if (document != null) {
            fileDocumentManager.saveDocument(document);
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);

        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    private void wipe(PsiClass psiClass) {
        if (psiClass == null) return;
        new WriteCommandAction.Simple(psiClass.getProject(), new PsiFile[]{
                psiClass.getContainingFile()
        }) {
            @Override
            protected void run() throws Throwable {
                new CodeGenerator(psiClass).generate();
            }
        }.execute();
    }
}
