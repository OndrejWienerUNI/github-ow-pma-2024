package com.mitch.christmas.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.mitch.christmas.lint.compose.designsystem.DesignSystemDetector.Companion.IncorrectDesignSystemCallIssue

class ChristmasIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = listOf(IncorrectDesignSystemCallIssue)

    override val minApi: Int = 14
    override val api: Int = CURRENT_API

    // Not changing vendor info for now - should be done for more complete apps
    private val repoUrl = "https://github.com/seve-andre/jetpack-compose-template"
    override val vendor: Vendor =
        Vendor(
            vendorName = "Template",
            feedbackUrl = "$repoUrl/issues",
            contact = repoUrl
        )
}
