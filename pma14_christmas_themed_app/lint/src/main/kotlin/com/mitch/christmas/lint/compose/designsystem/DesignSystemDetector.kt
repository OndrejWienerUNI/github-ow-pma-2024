package com.mitch.christmas.lint.compose.designsystem

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.mitch.christmas.lint.util.Priorities
import com.mitch.christmas.lint.util.fix.FileName
import com.mitch.christmas.lint.util.fix.Fix
import com.mitch.christmas.lint.util.fix.Reportable
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UQualifiedReferenceExpression

/**
 * A detector that checks for incorrect usages of Compose Material APIs over equivalents in
 * the template design system package/module.
 */
class DesignSystemDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(
        UCallExpression::class.java,
        UQualifiedReferenceExpression::class.java
    )

    override fun createUastHandler(context: JavaContext): UElementHandler =
        object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                val methodName = node.methodIdentifier?.name
                val reportable = MethodNames.firstOrNull { it.wrongName == methodName } ?: return
                val fileExceptions = reportable.fix?.fileExceptions.orEmpty().map { it.name }
                if (context.file.name in fileExceptions) return

                reportIssue(context, node, reportable)
            }

            override fun visitQualifiedReferenceExpression(node: UQualifiedReferenceExpression) {
                val name = node.receiver.asRenderString()
                val reportable = ReceiverNames.firstOrNull { it.wrongName == name } ?: return
                val fileExceptions = reportable.fix?.fileExceptions.orEmpty().map { it.name }
                if (context.file.name in fileExceptions) return

                reportIssue(context, node, reportable)
            }
        }

    private fun reportIssue(
        context: JavaContext,
        node: UElement,
        reportable: Reportable
    ) {
        context.report(
            IncorrectDesignSystemCallIssue,
            node,
            context.getLocation(node),
            "Using ${reportable.wrongName} instead of **${reportable.correctName}**",
            reportable.fix?.strategy?.fix(LintFix.create())
        )
    }

    companion object {
        @JvmField
        val IncorrectDesignSystemCallIssue: Issue = Issue.create(
            id = "IncorrectDesignSystemCall",
            briefDescription = "Use own components instead of Compose Material APIs",
            explanation =
            """
            This check highlights calls in code that use Compose Material composables 
            instead of equivalents from the template design system package
            """,
            category = Category.CORRECTNESS,
            priority = Priorities.High,
            severity = Severity.ERROR,
            implementation = Implementation(
                DesignSystemDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        // Unfortunately :lint is a Java module and thus can't depend on the :designsystem
        // Android module, so we can't use composable function references (eg. ::Button.name)
        // instead of hardcoded names.
        val MethodNames = listOf(
            Reportable(
                wrongName = "MaterialTheme",
                correctName = "ChristmasTheme",
                fix = Fix(fileExceptions = listOf(FileName.of("ChristmasTheme"))) {
                    it.replace()
                        .text("MaterialTheme")
                        .with("com.mitch.christmas.ui.designsystem.ChristmasTheme")
                        .shortenNames()
                        .build()
                }
            )
        )
        val ReceiverNames = listOf(
            Reportable(
                wrongName = "Icons",
                correctName = "ChristmasIcons",
                fix = Fix(fileExceptions = listOf(FileName.of("ChristmasIcons"))) {
                    it.replace()
                        .text("Icons")
                        .with("com.mitch.christmas.ui.designsystem.ChristmasIcons")
                        .shortenNames()
                        .build()
                }
            ),
            Reportable(
                wrongName = "MaterialTheme",
                correctName = "ChristmasDesignSystem",
                fix = Fix(fileExceptions = listOf(FileName.of("ChristmasTheme"))) {
                    it.replace()
                        .text("MaterialTheme")
                        .with("com.mitch.christmas.ui.designsystem.ChristmasDesignSystem")
                        .shortenNames()
                        .build()
                }
            )
        )
    }
}
