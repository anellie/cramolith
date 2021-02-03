/*
 * Developed as part of the PokeMMO project.
 * This file was last modified at 2/3/21, 2:05 PM.
 * Copyright 2020, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.pokemmo.client.graphics.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import xyz.angm.pokemmo.client.PokeMMO
import xyz.angm.pokemmo.client.graphics.panels.menu.LoadingPanel
import xyz.angm.pokemmo.client.graphics.panels.menu.MainMenuPanel
import xyz.angm.pokemmo.client.resources.ResourceManager

/** The menu screen. It manages the current menu panel stack and draws it on top of a nice background.
 * @param game The game instance. */
class MenuScreen(private val game: PokeMMO) : Screen() {

    private val stage = Stage(viewport)

    override fun show() {
        stage.addActor(panels)
        ResourceManager.init()
        pushPanel(LoadingPanel(this))
        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        stage.act()
        stage.draw()
    }

    /** Connect to server or display error, see method in [PokeMMO] */
    fun connectToServer() = game.connectToServer()

    /** Called when [ResourceManager] has finished loading. Will remove the loading screen
     * and show the main menu. */
    fun doneLoading() {
        panels.popPanel(-1)
        panels.pushPanel(MainMenuPanel(this))
    }

    override fun resize(width: Int, height: Int) = stage.viewport.update(width, height, true)

    override fun dispose() {
        stage.dispose()
        panels.dispose()
    }
}
