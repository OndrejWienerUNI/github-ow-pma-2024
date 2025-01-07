package com.mitch.fontpicker.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.mitch.fontpicker.lint.compose.designsystem.DesignSystemDetector.Companion.IncorrectDesignSystemCallIssue

class FontPickerIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = listOf(IncorrectDesignSystemCallIssue)

    override val minApi: Int = 14
    override val api: Int = CURRENT_API

    // TODO: Change this when the repo is prepared
    private val repoUrl = "https://github.com/seve-andre/jetpack-compose-template"
    override val vendor: Vendor =
        Vendor(
            vendorName = "FontPicker",
            feedbackUrl = "$repoUrl/issues",
            contact = repoUrl
        )
}
