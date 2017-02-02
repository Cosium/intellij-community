/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.mac.foundation.Foundation;
import com.intellij.ui.mac.foundation.ID;

public class MacEmojiAndSymbolsInputAction extends DumbAwareAction {
  public MacEmojiAndSymbolsInputAction() {
    // it's not currently possible to use &, when text is set in resource bundle
    getTemplatePresentation().setText("Emoji & Symbols", false);
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setEnabledAndVisible(SystemInfo.isMac);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    if (!SystemInfo.isMac) return;
    Foundation.executeOnMainThread(false, false, () -> {
      ID app = Foundation.invoke("NSApplication", "sharedApplication");
      Foundation.invoke(app, "orderFrontCharacterPalette:", (Object)null);
    });
  }
}
