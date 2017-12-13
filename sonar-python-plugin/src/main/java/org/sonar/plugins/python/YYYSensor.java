/*
 * SonarQube Python Plugin
 * Copyright (C) 2011-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.python;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.python.Python;
import org.sonar.plugins.python.PythonReportSensor;

public class YYYSensor extends PythonReportSensor {
  private class YYYIssue{
    private final String filepath;
    private final int line;
    private final String ruleid;
    private final String msg;

    YYYIssue(String filepath, int line, String ruleid, String msg) {
      this.filepath = filepath;
      this.line = line;
      this.ruleid = ruleid;
      this.msg = msg;
    }
  }

  public static final String REPORT_PATH_KEY = "sonar.python.yyy.reportPath";
  private static final String DEFAULT_REPORT_PATH = "yyy-report*.txt";

  private static final Logger LOG = LoggerFactory.getLogger(YYYSensor.class);

  private ActiveRules activeRules;
  private ResourcePerspectives resourcePerspectives;

  public YYYSensor(Settings conf, ActiveRules activeRules, FileSystem fileSystem, ResourcePerspectives resourcePerspectives) {
    super(conf, fileSystem);
    this.activeRules = activeRules;
    this.resourcePerspectives = resourcePerspectives;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    // Shortcut ...
    return true;

    // A useful implementation would be something like:
    //
    // FilePredicates p = fileSystem.predicates();
    // boolean hasFiles = fileSystem.hasFiles(p.and(p.hasType(InputFile.Type.MAIN), p.hasLanguage(Python.KEY)));
    // boolean hasRules = !activeRules.findByRepository(YYYRuleRepository.REPOSITORY_KEY).isEmpty();
    // return hasFiles && hasRules && conf.getString(REPORT_PATH_KEY) != null;
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }

  @Override
  protected void processReports(final SensorContext context, List<File> reports)
      throws javax.xml.stream.XMLStreamException {

    List<YYYIssue> issues = parse(reports);

    for (YYYIssue yi : issues) {
      InputFile pyfile = fileSystem.inputFile(fileSystem.predicates().hasPath(yi.filepath));
      if (pyfile != null) {
        RuleKey rulekey = RuleKey.of(YYYRuleRepository.REPOSITORY_KEY, yi.ruleid);
        ActiveRule rule = activeRules.find(rulekey);
        if (rule != null) {
          Issuable issuable = resourcePerspectives.as(Issuable.class, pyfile);
          org.sonar.api.issue.Issue issue = issuable.newIssueBuilder()
            .ruleKey(rulekey)
            .line(yi.line)
            .message(yi.msg)
            .build();
          issuable.addIssue(issue);
        } else {
          LOG.warn("YYY rule '{}' is unknown in SonarQube", yi.ruleid);
        }
      } else {
        LOG.warn("Cannot find the file '{}' in SonarQube, ignoring violation", yi.filepath);
      }
    }
  }

  private List<YYYIssue> parse(List<File> reports) {
    // A dummy implementation

    List<YYYIssue> issues = new LinkedList<>();
    issues.add(new YYYIssue("cs/__init__.py", 1, "yyy_1", "some message"));
    return issues;
  }
}
