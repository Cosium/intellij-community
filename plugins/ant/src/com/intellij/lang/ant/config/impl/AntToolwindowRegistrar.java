// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.ant.config.impl;

import com.intellij.lang.ant.AntBundle;
import com.intellij.lang.ant.config.AntBuildTarget;
import com.intellij.lang.ant.config.AntConfiguration;
import com.intellij.lang.ant.config.AntConfigurationBase;
import com.intellij.lang.ant.config.actions.TargetActionStub;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * @author Eugene Zhuravlev
 */
public class AntToolwindowRegistrar implements ProjectComponent {
  private final Project myProject;

  public AntToolwindowRegistrar(Project project) {
    myProject = project;
  }

  @Override
  public void projectOpened() {
    final String prefix = AntConfiguration.getActionIdPrefix(myProject);
    final ActionManager actionManager = ActionManager.getInstance();

    for (Keymap keymap : KeymapManagerEx.getInstanceEx().getAllKeymaps()) {
      for (String id : keymap.getActionIdList()) {
        if (id.startsWith(prefix) && actionManager.getAction(id) == null) {
          actionManager.registerAction(id, new TargetActionStub(id, myProject));
        }
      }
    }

    final CompilerManager compilerManager = CompilerManager.getInstance(myProject);
    compilerManager.addBeforeTask(new CompileTask() {
      @Override
      public boolean execute(CompileContext context) {
        return initializeAndRun(context, antConfiguration -> antConfiguration.executeTargetBeforeCompile(createDataContext(context)));
      }
    });
    compilerManager.addAfterTask(new CompileTask() {
      @Override
      public boolean execute(CompileContext context) {
        return initializeAndRun(context, antConfiguration -> {
          if (context.getMessageCount(CompilerMessageCategory.ERROR) > 0) {
            final AntBuildTarget target = antConfiguration.getTargetForEvent(ExecuteAfterCompilationEvent.getInstance());
            if (target != null) {
              context.addMessage(CompilerMessageCategory.INFORMATION,
                                 "Skipping ant target \"" + target.getDisplayName() + "\" because of compilation errors", null, -1, -1);
            }
            return true;
          }
          return antConfiguration.executeTargetAfterCompile(createDataContext(context));
        });
      }
    });
  }

  private boolean initializeAndRun(CompileContext context, Processor<? super AntConfigurationBase> action) {
    context.getProgressIndicator().pushState();
    try {
      context.getProgressIndicator().setText(AntBundle.message("loading.ant.config.progress"));
      AntConfigurationBase config = AntConfigurationBase.getInstance(myProject);
      config.ensureInitialized();
      context.getProgressIndicator().setText("Running Ant Tasks...");
      return action.process(config);
    }
    finally {
      context.getProgressIndicator().popState();
    }
  }

  @NotNull
  private static DataContext createDataContext(CompileContext context) {
    final HashMap<String, Object> dataMap = new HashMap<>();
    final Project project = context.getProject();
    dataMap.put(CommonDataKeys.PROJECT.getName(), project);
    final CompileScope scope = context.getCompileScope();
    final Module[] modules = scope.getAffectedModules();
    if (modules.length == 1) {
      dataMap.put(LangDataKeys.MODULE.getName(), modules[0]);
    }
    dataMap.put(LangDataKeys.MODULE_CONTEXT_ARRAY.getName(), modules);
    dataMap.put("COMPILER_CONTEXT_MAKE", context.isMake());
    return SimpleDataContext.getSimpleContext(dataMap, null);
  }

  @Override
  public void projectClosed() {
    final ActionManagerEx actionManager = ActionManagerEx.getInstanceEx();
    final String[] oldIds = actionManager.getActionIds(AntConfiguration.getActionIdPrefix(myProject));
    for (String oldId : oldIds) {
      actionManager.unregisterAction(oldId);
    }
  }

  @Override
  @NonNls
  @NotNull
  public String getComponentName() {
    return "AntToolwindowRegistrar";
  }
}
