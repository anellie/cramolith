/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 2/4/21, 12:43 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.client.graphics.panels.game

import com.kotcrab.vis.ui.widget.VisTextButton
import ktx.actors.onClick
import xyz.angm.cramolith.client.graphics.Skin
import xyz.angm.cramolith.client.graphics.click
import xyz.angm.cramolith.client.graphics.panels.Panel
import xyz.angm.cramolith.client.graphics.panels.menu.options.OptionsPanel
import xyz.angm.cramolith.client.graphics.screens.GameScreen
import xyz.angm.cramolith.client.resources.I18N

/** In-game pause screen. */
class PausePanel(screen: GameScreen) : Panel(screen) {

    init {
        val continueButton = VisTextButton(I18N["pause.continue"])
        add(continueButton).height(Skin.textButtonHeight).width(Skin.textButtonWidth).pad(20f).row()
        continueButton.click()

        val optionsButton = VisTextButton(I18N["pause.options"])
        add(optionsButton).height(Skin.textButtonHeight).width(Skin.textButtonWidth).pad(20f).row()
        optionsButton.click()

        val exitButton = VisTextButton(I18N["pause.exit"])
        add(exitButton).height(Skin.textButtonHeight).width(Skin.textButtonWidth).pad(20f).row()
        exitButton.click()

        continueButton.onClick { screen.popPanel() }
        optionsButton.onClick { screen.pushPanel(OptionsPanel(screen)) }
        exitButton.onClick { screen.returnToMenu() }
    }
}