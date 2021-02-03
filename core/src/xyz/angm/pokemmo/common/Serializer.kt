/*
 * Developed as part of the PokeMMO project.
 * This file was last modified at 2/1/21, 5:32 PM.
 * Copyright 2020, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.pokemmo.common

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.IntSet
import com.charleskorn.kaml.Yaml
import org.nustaq.serialization.FSTConfiguration
import xyz.angm.pokemmo.common.ecs.components.*
import xyz.angm.pokemmo.common.ecs.components.specific.PlayerComponent
import xyz.angm.pokemmo.common.ecs.ignoreSync
import xyz.angm.pokemmo.common.ecs.renderable
import xyz.angm.pokemmo.common.networking.ChatMessagePacket
import xyz.angm.pokemmo.common.networking.InitPacket
import xyz.angm.pokemmo.common.networking.JoinPacket
import xyz.angm.rox.Component
import xyz.angm.rox.Entity
import xyz.angm.rox.FSTEntitySerializer
import kotlin.reflect.KClass

/** A simple YAML serializer used for configuration files and some game data. */
val yaml = Yaml()

/** A FST serializer used for network communication and world storage. */
val fst = createFST(
    // Packets
    JoinPacket::class, InitPacket::class, ChatMessagePacket::class,

    // Components
    Component::class, VectoredComponent::class,
    PositionComponent::class, VelocityComponent::class, PlayerComponent::class,
    RemoveFlag::class, NetworkSyncComponent::class,

    // Various
    Vector2::class, Entity::class
)

private fun createFST(vararg classes: KClass<out Any>): FSTConfiguration {
    val fst = FSTConfiguration.createDefaultConfiguration()
    classes.forEach { fst.registerClass(it.java) }

    val ignore = IntSet()
    ignore.add(ignoreSync.index)
    ignore.add(renderable.index)
    fst.registerSerializer(Entity::class.java, FSTEntitySerializer(ignore), true)

    return fst
}
