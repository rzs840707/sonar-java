/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.checks;

import org.sonar.api.rule.RuleKey;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

import java.util.List;

@Rule(
  key = ClassVariableVisibilityCheck.RULE_KEY,
  priority = Priority.MAJOR)
@BelongsToProfile(title = "Sonar way", priority = Priority.MAJOR)
public class ClassVariableVisibilityCheck extends BaseTreeVisitor implements JavaFileScanner {

  public static final String RULE_KEY = "ClassVariableVisibilityCheck";
  private final RuleKey ruleKey = RuleKey.of(CheckList.REPOSITORY_KEY, RULE_KEY);

  private boolean isClass = false;

  private JavaFileScannerContext context;

  @Override
  public void scanFile(JavaFileScannerContext context) {
    this.context = context;
    scan(context.getTree());
  }

  @Override
  public void visitClass(ClassTree tree) {

    if (tree.is(Tree.Kind.CLASS)) {
      isClass = true;
    }
    else {
      isClass = false;
    }
    super.visitClass(tree);
  }

  @Override
  public void visitVariable(VariableTree tree) {

    List<Modifier> modifiers = tree.modifiers().modifiers();
    List<AnnotationTree> annotations = tree.modifiers().annotations();

    if (isClass && isPublic(modifiers) && !(isConstant(modifiers) || isAnnotated(annotations))) {
      context.addIssue(tree, ruleKey, "Make this class field a static final constant or non-public and provide accessors if needed.");
    }

    super.visitVariable(tree);
  }

  private static boolean isConstant(List<Modifier> modifiers) {
    return hasModifier(modifiers) && modifiers.contains(Modifier.FINAL) && modifiers.contains(Modifier.STATIC);
  }

  private static boolean isPublic(List<Modifier> modifiers) {
    return hasModifier(modifiers) && modifiers.contains(Modifier.PUBLIC);
  }

  private static boolean hasModifier(List<Modifier> modifiers) {
    return (modifiers != null && modifiers.size() > 0);
  }

  private static boolean isAnnotated(List<AnnotationTree> annotations) {
    return (annotations != null && annotations.size() > 0);
  }

}
