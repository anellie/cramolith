/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 3/21/21, 5:13 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.client.graphics

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.kotcrab.vis.ui.widget.VisImage
import xyz.angm.cramolith.client.resources.ResourceManager
import xyz.angm.cramolith.common.ecs.playerM
import xyz.angm.rox.Entity

class PlayerSprite(private val player: Entity) : VisImage() {

    private val region = TextureRegion(ResourceManager.get<Texture>("sprites/player.png"))
    private var counter = 0f

    init {
        drawable = TextureRegionDrawable(region)
        setSize(20f, 26f)
        act(0f)
    }

    override fun act(delta: Float) {
        super.act(delta)
        counter += delta
        val dir = player[playerM].sprite / 2
        val isRunning = player[playerM].sprite % 2
        val runSprite = isRunning * (((counter * 4).toInt() % 3) + 1)
        region.setRegion(runSprite * 20, dir * 26, 20, 26)
    }
}