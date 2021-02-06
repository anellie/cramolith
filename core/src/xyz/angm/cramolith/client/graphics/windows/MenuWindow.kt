/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 2/6/21, 6:26 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.client.graphics.windows

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.actors.onClick
import ktx.scene2d.tooltip
import ktx.scene2d.vis.visLabel
import xyz.angm.cramolith.client.actions.PlayerActions
import xyz.angm.cramolith.client.graphics.screens.GameScreen
import xyz.angm.cramolith.client.resources.I18N

class MenuWindow(screen: GameScreen) : VisWindow(I18N["menu-window"]) {

    init {
        fun addButton(name: String, action: String) {
            val btn = VisTextButton(name.substring(0, 1).toUpperCase())
            btn.onClick { PlayerActions[action]?.keyDown?.invoke(screen) }
            btn.tooltip { visLabel(I18N[name]) }
            add(btn).left().pad(5f)
        }

        addButton("chat", "chat")
        addButton("players-online", "onlinePlayers")
        addButton("debug", "debugInfo")
        addButton("pause", "pauseMenu")
        pack()
    }

    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        setPosition(stage?.width ?: return, 0f, Align.bottomRight)
    }
}