/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 2/18/21, 5:43 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.common.world

import com.badlogic.gdx.graphics.Color
import kotlinx.serialization.Serializable

/** A trigger is a rectangular area on the map that causes some sort of effect when intersecting
 * with a player, handled by TriggerSystem.
 * @property idx An index that has a different meaning depending on the type of trigger. See TriggerType for exact info. */
@Serializable
data class Trigger(val type: TriggerType, val x: Int, val y: Int, val width: Int, val height: Int, var idx: Int)

/** The type of a trigger.
 * The color is only for the editor and not actually used in-game.
 * - Collision: Player cannot walk on this trigger, `idx` is always -1
 * - Water: Player cannot walk on this trigger, `idx` is always -1; player can swim in trigger if ability is unlocked
 * - Teleport: Player will be teleported to the corresponding teleport, teleport is taken from the map at `idx`
 * - Actor: Triggers an actor's script when stepped on, `idx` is the actor's index
 * - WildEncounter: Can randomly trigger wild pokemon encounter when walked on, `idx` is encounter table index */
enum class TriggerType(val color: Color) {
    Collision(Color.ROYAL),
    Water(Color.ORANGE),
    Teleport(Color.FOREST),
    Actor(Color.SCARLET),
    WildEncounter(Color.PINK)
}

/** A teleport. Always comes in pairs, each map has a list of them.
 * @property map The index of the map of the other teleport
 * @property target The index of the other teleport in it's map */
@Serializable
data class Teleport(val map: Int, val target: Int)
