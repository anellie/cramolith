/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 2/4/21, 12:43 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.rox

/** An entity listener can be used for reacting to entity additions and removals
 * inside an [Engine].
 * Override the interface and add it with [Engine.add]. */
interface EntityListener {

    /** The family to listen to. Events will only fire for entities in this family. */
    val family: Family

    /** Called when an entity was added to the ECS, right after addition.
     * @param entity The new entity. */
    fun entityAdded(entity: Entity)

    /** Called when an entity was removed from the ECS, right after deletion.
     * @param entity The removed entity. */
    fun entityRemoved(entity: Entity)
}
