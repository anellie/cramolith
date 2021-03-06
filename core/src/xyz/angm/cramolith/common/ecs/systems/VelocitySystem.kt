/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 3/21/21, 11:04 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.common.ecs.systems

import xyz.angm.cramolith.common.ecs.ignoreSync
import xyz.angm.cramolith.common.ecs.position
import xyz.angm.cramolith.common.ecs.velocity
import xyz.angm.rox.Entity
import xyz.angm.rox.Family
import xyz.angm.rox.systems.IteratingSystem

const val SPEED = 100f

class VelocitySystem : IteratingSystem(Family.allOf(position, velocity, ignoreSync)) {
    override fun process(entity: Entity, delta: Float) {
        val vel = entity[velocity]
        val pos = entity[position]
        pos.x += vel.x * delta * SPEED
        pos.y += vel.y * delta * SPEED
    }
}