/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 2/7/21, 1:08 AM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.client.graphics.windows

import com.badlogic.gdx.utils.Scaling
import com.kotcrab.vis.ui.widget.VisTable
import ktx.actors.onClick
import ktx.scene2d.button
import ktx.scene2d.scene2d
import ktx.scene2d.scrollPane
import ktx.scene2d.vis.visImage
import ktx.scene2d.vis.visLabel
import ktx.scene2d.vis.visTable
import xyz.angm.cramolith.client.graphics.click
import xyz.angm.cramolith.client.graphics.screens.GameScreen
import xyz.angm.cramolith.client.resources.I18N
import xyz.angm.cramolith.common.ecs.playerM

class PartyWindow(private val screen: GameScreen) : Window("party") {

    private lateinit var table: VisTable

    init {
        addCloseButton()
        reload()
        add(scene2d.scrollPane { actor = table })
        pack()
    }

    private fun reload() {
        table = scene2d.visTable {
            for (pokemon in screen.player[playerM].pokemon) {
                val button = scene2d.button("list") {
                    isDisabled = true
                    left().pad(5f).click()

                    visImage(pokemon.species.icon) {
                        it.height(54f).width(60f)
                        setScaling(Scaling.fit)
                    }
                    visLabel(pokemon.displayName) { it.expandX().fillX().padRight(30f) }
                    visLabel("${I18N["party.level"]} ${pokemon.level}")
                    pack()

                    onClick { stage.addActor(PokemonSummaryWindow(screen, pokemon)) }
                }
                add(button).expandX().fillX().row()
            }
        }
    }
}