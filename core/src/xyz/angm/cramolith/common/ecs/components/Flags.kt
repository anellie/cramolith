/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 2/4/21, 12:43 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.common.ecs.components

import xyz.angm.cramolith.common.ecs.network
import xyz.angm.rox.Component
import xyz.angm.rox.Engine
import xyz.angm.rox.Entity


/*
 * This file contains all components with no state, used simply as a flag.
 */

/** Flags an entity to be removed from the engine; happens after the current update cycle. */
class RemoveFlag private constructor() : Component {
    companion object {
        /** Mark an entity to be scheduled for removal.
         * Will also ensure it syncs if needed. */
        fun flag(engine: Engine, entity: Entity) {
            entity.add(engine, RemoveFlag())
            entity.c(network)?.needsSync = true
        }
    }
}

/** Ignores the entity containing it when receiving it over network. */
class IgnoreSyncFlag : Component