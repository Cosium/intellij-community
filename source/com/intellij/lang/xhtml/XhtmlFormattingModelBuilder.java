/*
 * @author max
 */
package com.intellij.lang.xhtml;

import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.PsiBasedFormattingModel;
import com.intellij.psi.formatter.xml.HtmlPolicy;
import com.intellij.psi.formatter.xml.XmlBlock;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.openapi.util.TextRange;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class XhtmlFormattingModelBuilder implements FormattingModelBuilder {
  @NotNull
    public FormattingModel createModel(final PsiElement element, final CodeStyleSettings settings) {
    final PsiFile psiFile = element.getContainingFile();
    final FormattingDocumentModelImpl documentModel = FormattingDocumentModelImpl.createOn(psiFile);
    return new PsiBasedFormattingModel(psiFile,
                                       new XmlBlock(SourceTreeToPsiMap.psiElementToTree(psiFile),
                                                    null, null,
                                                    new HtmlPolicy(settings, documentModel), null, null),
                                       documentModel);
  }

  public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
    return null;
  }
}