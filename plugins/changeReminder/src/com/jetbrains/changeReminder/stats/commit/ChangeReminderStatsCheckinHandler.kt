// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.changeReminder.stats.commit

import com.intellij.openapi.components.service
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.ChangesUtil
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.jetbrains.changeReminder.predict.PredictionService
import com.jetbrains.changeReminder.stats.ChangeReminderChangesCommittedEvent
import com.jetbrains.changeReminder.stats.logEvent

class ChangeReminderStatsCheckinHandler : CheckinHandlerFactory() {
  override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext) = object : CheckinHandler() {
    override fun beforeCheckin(): ReturnResult {
      val project = panel.project
      val prediction = project.service<PredictionService>().predictionDataToDisplay
      val committedFiles = panel.selectedChanges.map { ChangesUtil.getFilePath(it) }

      val curFiles = ChangeListManager.getInstance(project).defaultChangeList.changes.map { ChangesUtil.getFilePath(it) }
      logEvent(project, ChangeReminderChangesCommittedEvent(curFiles, committedFiles, prediction))
      return ReturnResult.COMMIT
    }
  }
}