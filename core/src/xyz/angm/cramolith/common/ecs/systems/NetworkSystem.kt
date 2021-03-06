/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 5/6/21, 7:18 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.common.ecs.systems

import com.badlogic.gdx.utils.IntMap
import xyz.angm.cramolith.common.ecs.*
import xyz.angm.rox.Entity
import xyz.angm.rox.EntityListener
import xyz.angm.rox.Family
import xyz.angm.rox.systems.EntitySystem

/** A system that keeps track of all entities registered and gives each a unique ID.
 *
 * It also automatically manages all entities sent via network by adding them to the engine automatically;
 * entities that request network update are sent automatically as well.
 *
 * REGISTER AS SECOND LAST!. */
class NetworkSystem(private val send: (Entity) -> Unit, private val ignoredPlayerId: Int = -1) : EntitySystem(Int.MAX_VALUE - 1), EntityListener {

    override val family = Family.allOf(network)
    private val entities = IntMap<Entity>()

    /** Send any entities that require updating. */
    override fun update(delta: Float) {
        entities.values().forEach { entity ->
            if (entity[network].needsSync) {
                entity[network].needsSync = false
                send(entity)
            }
        }
    }

    /** Either add the new entity or update the local one.
     * Called when entity was received over network. */
    fun receive(netE: Entity) {
        val network = netE.c(network) ?: return
        val removed = netE has remove
        if (!entities.containsKey(network.id) && !removed) {
            engine.add(netE)
        } else {
            val localEntity = entities[network.id]
            if (ignoredPlayerId == localEntity[playerM].clientUUID) return
            if (removed) engine.remove(localEntity)
            else {
                for (i in 0 until netE.components.size) {
                    if (i != renderable.index && i != ignoreSync.index && netE.components[i] != null) {
                        localEntity.add(engine, netE.components[i]!!)
                    }
                }
            }
            Entity.free(netE)
        }
    }

    /** Keep track of all entities. */
    override fun entityAdded(entity: Entity) {
        entities.put(entity[network].id, entity)
    }

    /** Keep track of all entities. */
    override fun entityRemoved(entity: Entity) {
        entities.remove(entity[network].id)
    }

    fun entityOf(id: Int): Entity? = entities[id]
}