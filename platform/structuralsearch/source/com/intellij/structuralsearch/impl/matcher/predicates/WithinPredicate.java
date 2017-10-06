// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.structuralsearch.impl.matcher.predicates;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.structuralsearch.MalformedPatternException;
import com.intellij.structuralsearch.MatchOptions;
import com.intellij.structuralsearch.MatchResult;
import com.intellij.structuralsearch.Matcher;
import com.intellij.structuralsearch.impl.matcher.MatchContext;
import com.intellij.structuralsearch.plugin.ui.Configuration;
import com.intellij.structuralsearch.plugin.ui.ConfigurationManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Maxim.Mossienko
 */
public class WithinPredicate extends MatchPredicate {

  @SuppressWarnings("SSBasedInspection")
  private static final ThreadLocal<Set<String>> ourRecursionGuard = ThreadLocal.withInitial(() -> new HashSet<>());
  private final Matcher matcher;

  public WithinPredicate(String within, FileType fileType, Project project) {
    if (StringUtil.isQuotedString(within)) {
      // keep old configurations working
      final MatchOptions myMatchOptions = new MatchOptions();
      myMatchOptions.setLooseMatching(true);
      myMatchOptions.setFileType(fileType);
      myMatchOptions.fillSearchCriteria(StringUtil.unquoteString(within));
      matcher = new Matcher(project, myMatchOptions);
    }
    else {
      final Set<String> set = ourRecursionGuard.get();
      if (!set.add(within)) {
        throw new MalformedPatternException("Pattern recursively contained within itself");
      }
      try {
        final Configuration configuration = ConfigurationManager.getInstance(project).findConfigurationByName(within);
        if (configuration == null) {
          throw new MalformedPatternException("Configuration '" + within + "' not found");
        }
        matcher = new Matcher(project, configuration.getMatchOptions());
      } finally {
        set.remove(within);
        if (set.isEmpty()) {
          // we're finished with this thread local
          ourRecursionGuard.remove();
        }
      }
    }
  }

  @Override
  public boolean match(PsiElement matchedNode, int start, int end, MatchContext context) {
    final List<MatchResult> results = matcher.matchByDownUp(matchedNode);
    for (MatchResult result : results) {
      if (PsiTreeUtil.isAncestor(result.getMatch(), matchedNode, false)) {
        return true;
      }
    }
    return false;
  }
}