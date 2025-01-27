package com.flipperdevices.archive.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.value.Value
import com.flipperdevices.archive.api.ArchiveDecomposeComponent
import com.flipperdevices.archive.api.CategoryDecomposeComponent
import com.flipperdevices.archive.api.SearchDecomposeComponent
import com.flipperdevices.archive.impl.model.ArchiveNavigationConfig
import com.flipperdevices.archive.impl.model.toArchiveNavigationStack
import com.flipperdevices.bottombar.handlers.ResetTabDecomposeHandler
import com.flipperdevices.core.di.AppGraph
import com.flipperdevices.deeplink.model.Deeplink
import com.flipperdevices.ui.decompose.DecomposeComponent
import com.flipperdevices.ui.decompose.findComponentByConfig
import com.flipperdevices.ui.decompose.popToRoot
import com.squareup.anvil.annotations.ContributesBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ArchiveDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted deeplink: Deeplink.BottomBar.ArchiveTab?,
    private val openCategoryFactory: CategoryDecomposeComponent.Factory,
    private val searchFactory: SearchDecomposeComponent.Factory,
    private val archiveScreenFactory: ArchiveScreenDecomposeComponentImpl.Factory
) : ArchiveDecomposeComponent, ComponentContext by componentContext, ResetTabDecomposeHandler {
    private val navigation = StackNavigation<ArchiveNavigationConfig>()

    private val stack: Value<ChildStack<*, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = ArchiveNavigationConfig.serializer(),
        initialStack = {
            deeplink.toArchiveNavigationStack()
        },
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(
        config: ArchiveNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        ArchiveNavigationConfig.ArchiveObject -> archiveScreenFactory(
            componentContext = componentContext,
            navigation = navigation
        )

        is ArchiveNavigationConfig.OpenCategory -> openCategoryFactory(
            componentContext = componentContext,
            categoryType = config.categoryType
        )

        ArchiveNavigationConfig.OpenSearch -> searchFactory(
            componentContext = componentContext,
            onItemSelected = null
        )
    }

    override fun handleDeeplink(deeplink: Deeplink.BottomBar.ArchiveTab) {
        navigation.navigate { deeplink.toArchiveNavigationStack() }
    }

    override fun onResetTab() {
        navigation.popToRoot()
        val instance = stack.findComponentByConfig(ArchiveNavigationConfig.ArchiveObject::class)
        if (instance is ResetTabDecomposeHandler) {
            instance.onResetTab()
        }
    }

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val childStack by stack.subscribeAsState()

        Children(
            stack = childStack,
        ) {
            it.instance.Render()
        }
    }

    @AssistedFactory
    @ContributesBinding(AppGraph::class, ArchiveDecomposeComponent.Factory::class)
    interface Factory : ArchiveDecomposeComponent.Factory {
        override fun invoke(
            componentContext: ComponentContext,
            deeplink: Deeplink.BottomBar.ArchiveTab?
        ): ArchiveDecomposeComponentImpl
    }
}
